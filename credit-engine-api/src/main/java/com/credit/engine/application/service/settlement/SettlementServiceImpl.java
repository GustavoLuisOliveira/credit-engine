package com.credit.engine.application.service.settlement;

import com.credit.engine.application.dto.currency.ExchangeRateResponse;
import com.credit.engine.application.dto.settlement.SettlementExtractResponse;
import com.credit.engine.application.dto.settlement.SettlementRequest;
import com.credit.engine.application.dto.settlement.SettlementResponse;
import com.credit.engine.application.service.currency.ExchangeRateService;
import com.credit.engine.domain.model.pricing.PricingParameter;
import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.model.receivable.ReceivableStatus;
import com.credit.engine.domain.model.settlement.Settlement;
import com.credit.engine.domain.model.settlement.SettlementItem;
import com.credit.engine.domain.princing.PricingResult;
import com.credit.engine.domain.princing.strategy.PricingStrategyResolver;
import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.domain.shared.money.Money;
import com.credit.engine.infrastructure.persistence.entity.settlement.SettlementItemEntity;
import com.credit.engine.infrastructure.persistence.mapper.pricing.PricingParameterMapper;
import com.credit.engine.infrastructure.persistence.mapper.receivable.ReceivableMapper;
import com.credit.engine.infrastructure.persistence.mapper.settlement.SettlementItemMapper;
import com.credit.engine.infrastructure.persistence.mapper.settlement.SettlementMapper;
import com.credit.engine.infrastructure.persistence.repository.assignor.AssignorRepository;
import com.credit.engine.infrastructure.persistence.repository.currency.CurrencyRepository;
import com.credit.engine.infrastructure.persistence.repository.pricing.PricingParameterRepository;
import com.credit.engine.infrastructure.persistence.repository.receivable.ReceivableRepository;
import com.credit.engine.infrastructure.persistence.repository.settlement.SettlementItemRepository;
import com.credit.engine.infrastructure.persistence.repository.settlement.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orquestra a execução de um lote de liquidação (Settlement + N SettlementItem) numa
 * única transação ACID: ou o lote inteiro é persistido (cabeçalho, itens e mudança de
 * status dos recebíveis para SETTLED), ou nada é ("Nenhuma liquidação pode ficar pela
 * metade").
 *
 * A proteção definitiva contra liquidação em duplicidade sob concorrência é a
 * UNIQUE constraint em settlement_item.receivable_id.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementServiceImpl implements SettlementService {

    private final AssignorRepository assignorRepository;
    private final CurrencyRepository currencyRepository;
    private final ReceivableRepository receivableRepository;
    private final ReceivableMapper receivableMapper;
    private final PricingParameterRepository pricingParameterRepository;
    private final PricingParameterMapper pricingParameterMapper;
    private final PricingStrategyResolver pricingStrategyResolver;
    private final ExchangeRateService exchangeRateService;
    private final SettlementRepository settlementRepository;
    private final SettlementMapper settlementMapper;
    private final SettlementItemRepository settlementItemRepository;
    private final SettlementItemMapper settlementItemMapper;


    @Override
    @Transactional
    public SettlementResponse execute(SettlementRequest request) {
        UUID assignorId = request.assignorId();
        LocalDate valuationDate = request.valuationDate();
        String targetCurrency = request.targetCurrencyCode().toUpperCase();

        // Validações de pré-condição: falha rápido antes de calcular qualquer coisa
        if (!assignorRepository.existsById(assignorId))
            throw new DomainNotFoundException("Cedente não encontrado: " + assignorId);

        if (!currencyRepository.existsById(targetCurrency))
            throw new DomainNotFoundException("Moeda alvo não encontrada: " + targetCurrency);

        List<ItemCalculation> calculations = new ArrayList<>();
        // Totais acumulados do lote, sempre na moeda alvo (targetCurrency)
        Money totalFaceValue = Money.zero(targetCurrency);
        Money totalDiscountAmount = Money.zero(targetCurrency);
        Money totalNetAmount = Money.zero(targetCurrency);

        // 1ª passada: calcula o preço de cada recebível sem persistir nada ainda
        for (UUID receivableId : request.receivableIds()) {
            ItemCalculation calculation = calculateItem(receivableId, assignorId, valuationDate, targetCurrency);
            calculations.add(calculation);

            // Soma os valores já convertidos para a moeda alvo
            totalFaceValue = totalFaceValue.add(calculation.convertedFaceValue());
            totalDiscountAmount = totalDiscountAmount.add(calculation.convertedDiscountAmount());
            totalNetAmount = totalNetAmount.add(calculation.settlementAmount());
        }

        // Persiste o cabeçalho primeiro: os itens precisam do settlementId gerado aqui
        Settlement settlement = Settlement.create(assignorId, Instant.now(), valuationDate, totalFaceValue, totalDiscountAmount, totalNetAmount);
        var savedSettlement = settlementRepository.save(settlementMapper.toEntity(settlement));
        UUID settlementId = savedSettlement.getId();

        // 2ª passada: persiste cada item e marca o recebível correspondente como liquidado
        List<SettlementItem> items = new ArrayList<>();
        for (ItemCalculation calculation : calculations)
            items.add(persistItemAndSettleReceivable(settlementId, calculation));

        return SettlementResponse.toResponse(
                settlementMapper.toDomain(savedSettlement), items
        );
    }

    /** Resolve preço + conversão cambial de um único recebível, sem persistir nada ainda. */
    private ItemCalculation calculateItem(UUID receivableId, UUID assignorId, LocalDate valuationDate, String targetCurrency) {
        // Busca o recebível e já converte para o modelo de domínio
        Receivable receivable = receivableMapper.toDomain(
                receivableRepository.findById(receivableId)
                        .orElseThrow(() -> new DomainNotFoundException("Recebível não encontrado: " + receivableId))
        );

        // Garante que o recebível realmente pertence ao cedente informado na requisição
        if (!receivable.getAssignorId().equals(assignorId))
            throw new DomainConflictException("Recebível " + receivableId + " não pertence ao cedente informado: " + assignorId);

        // Só é possível liquidar um recebível que ainda está pendente
        if (receivable.getStatus() != ReceivableStatus.UNSETTLED)
            throw new DomainConflictException("Recebível já liquidado ou cancelado: " + receivableId);

        // Pré-checagem otimista fail-fast (economiza o cálculo de precificação e a conversão
        // cambial quando já dá para saber de antemão que o item vai ser rejeitado). A garantia
        // definitiva contra concorrência continua sendo a UNIQUE constraint no INSERT.
        if (settlementItemRepository.existsByReceivableId(receivableId))
            throw new DomainConflictException("Recebível já liquidado: " + receivableId);

        // Busca a taxa (base + spread) vigente para o tipo do recebível na data de valoração
        PricingParameter parameter = pricingParameterMapper.toDomain(
                pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        receivable.getType(), valuationDate
                ).orElseThrow(() -> new DomainNotFoundException("Nenhum parâmetro de precificação vigente para: " + receivable.getType()))
        );

        // Aplica o Strategy Pattern: resolve a fórmula certa pelo tipo do recebível e calcula o deságio
        PricingResult result = pricingStrategyResolver
                .resolve(receivable.getType())
                .calculate(receivable, parameter.baseRateAsFraction(), parameter.spreadRateAsFraction(), valuationDate);

        // Prazo em dias corridos: fato de auditoria legível.
        // O prazo fracionário em meses efetivamente usado no expoente da fórmula já vem
        // calculado em result.getTerm(), ambos são congelados no SettlementItem.
        int termDays = (int) ChronoUnit.DAYS.between(valuationDate, receivable.getDueDate());

        String originalCurrency = receivable.getFaceValue().getCurrencyCode();

        BigDecimal exchangeRateUsed;
        Money settlementAmount;
        Money convertedFaceValue;
        Money convertedDiscountAmount;

        if (originalCurrency.equals(targetCurrency)) {
            // Mesma moeda: nenhuma conversão real acontece, taxa neutra 1.0
            exchangeRateUsed = BigDecimal.ONE.setScale(8, RoundingMode.UNNECESSARY);
            settlementAmount = result.getPresentValue();
            convertedFaceValue = receivable.getFaceValue();
            convertedDiscountAmount = result.getDiscountAmount();
        } else {
            // Moedas diferentes: busca a cotação mais recente e converte
            ExchangeRateResponse rate = exchangeRateService.findLatestRate(originalCurrency, targetCurrency);
            exchangeRateUsed = rate.rate();
            // A mesma cotação é aplicada a faceValue e discountAmount, além do presentValue:
            // é o que garante que os totais do Settlement (sempre na targetCurrency) somem de
            // forma coerente, já que faceValue - discountAmount = presentValue por construção.
            settlementAmount = convert(result.getPresentValue(), exchangeRateUsed, targetCurrency);
            convertedFaceValue = convert(receivable.getFaceValue(), exchangeRateUsed, targetCurrency);
            convertedDiscountAmount = convert(result.getDiscountAmount(), exchangeRateUsed, targetCurrency);
        }

        return new ItemCalculation(receivable, result, termDays, exchangeRateUsed, settlementAmount, convertedFaceValue, convertedDiscountAmount);
    }

    /** Persiste o item (protegido pela UNIQUE constraint) e marca o recebível como liquidado. */
    private SettlementItem persistItemAndSettleReceivable(UUID settlementId, ItemCalculation calculation) {
        Receivable receivable = calculation.receivable();
        PricingResult result = calculation.pricingResult();

        // Monta a fotografia de auditoria imutável do item
        SettlementItem item = SettlementItem.create(
                settlementId, receivable.getId(), calculation.termDays(), result.getTermMonths(),
                result.getBaseRate(), result.getSpreadRate(),
                receivable.getFaceValue(), result.getDiscountAmount(), result.getPresentValue(),
                calculation.exchangeRateUsed(), calculation.settlementAmount()
        );

        SettlementItemEntity savedItem;
        try {
            // saveAndFlush força o INSERT agora, para a violação da UNIQUE estourar aqui
            savedItem = settlementItemRepository.saveAndFlush(settlementItemMapper.toEntity(item));
        } catch (DataIntegrityViolationException ex) {
            // Alguém liquidou esse recebível entre a pré-checagem e este INSERT
            throw new DomainConflictException("Recebível já liquidado (conflito de concorrência): " + receivable.getId());
        }

        // marca o recebível como liquidado
        receivable.markAsSettled();
        receivableRepository.save(receivableMapper.toEntity(receivable));

        return settlementItemMapper.toDomain(savedItem);
    }

    private Money convert(Money original, BigDecimal exchangeRate, String targetCurrency) {
        // Converte o valor pela cotação e arredonda na escala monetária padrão (4 casas)
        BigDecimal convertedAmount = original.getAmount().multiply(exchangeRate).setScale(4, RoundingMode.HALF_EVEN);
        return Money.of(convertedAmount, targetCurrency);
    }

    @Override
    public SettlementResponse findById(UUID id) {
        return SettlementResponse.toResponse(
                findDomainById(id), findItemsDomainBySettlementId(id)
        );
    }

    private Settlement findDomainById(UUID id) {
        return settlementMapper.toDomain(
                settlementRepository.findById(id)
                        .orElseThrow(() -> new DomainNotFoundException("Liquidação não encontrada: " + id))
        );
    }

    private List<SettlementItem> findItemsDomainBySettlementId(UUID id) {
        return settlementItemRepository.findBySettlementId(id).stream()
                .map(settlementItemMapper::toDomain)
                .toList();
    }

    @Override
    public List<SettlementResponse> findByAssignor(UUID assignorId) {
        return settlementRepository.findByAssignorId(assignorId).stream()
                .map(entity ->
                    SettlementResponse.toResponse(
                            settlementMapper.toDomain(entity), findItemsDomainBySettlementId(entity.getId())
                    )
                )
                .toList();
    }

    @Override
    public Page<SettlementExtractResponse> extract(UUID assignorId, String currencyCode, LocalDate valuationDateFrom, LocalDate valuationDateTo, Pageable pageable) {
        String normalizedCurrencyCode = currencyCode != null ? currencyCode.toUpperCase() : null;
        return settlementRepository
                .findExtract(assignorId, normalizedCurrencyCode, valuationDateFrom, valuationDateTo, pageable)
                .map(SettlementExtractResponse::toResponse);
    }
}

/** Resultado intermediário do cálculo de um item, antes de qualquer persistência. */
record ItemCalculation(
        Receivable receivable,
        PricingResult pricingResult,
        int termDays,
        BigDecimal exchangeRateUsed,
        Money settlementAmount,
        Money convertedFaceValue,
        Money convertedDiscountAmount
) {
}
