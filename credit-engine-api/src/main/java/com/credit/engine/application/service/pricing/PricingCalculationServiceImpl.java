package com.credit.engine.application.service.pricing;


import com.credit.engine.application.dto.currency.ExchangeRateResponse;
import com.credit.engine.application.dto.pricing.PricingSimulationResponse;
import com.credit.engine.application.service.currency.ExchangeRateService;
import com.credit.engine.domain.model.pricing.PricingParameter;
import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.pricing.PricingResult;
import com.credit.engine.domain.pricing.strategy.PricingStrategyResolver;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.mapper.pricing.PricingParameterMapper;
import com.credit.engine.infrastructure.persistence.mapper.receivable.ReceivableMapper;
import com.credit.engine.infrastructure.persistence.repository.pricing.PricingParameterRepository;
import com.credit.engine.infrastructure.persistence.repository.receivable.ReceivableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingCalculationServiceImpl implements PricingCalculationService {

    private final ReceivableRepository receivableRepository;
    private final ReceivableMapper receivableMapper;
    private final PricingParameterRepository pricingParameterRepository;
    private final PricingParameterMapper pricingParameterMapper;
    private final PricingStrategyResolver pricingStrategyResolver;
    private final ExchangeRateService exchangeRateService;

    @Override
    public PricingSimulationResponse simulate(UUID receivableId, LocalDate valuationDate, String targetCurrencyCode) {
        // Busca o recebível e converte para o modelo de domínio
        Receivable receivable = receivableMapper.toDomain(
                receivableRepository.findById(receivableId)
                        .orElseThrow(() -> new DomainNotFoundException("Recebível não encontrado: " + receivableId))
        );

        // Busca a taxa (base + spread) vigente para o tipo do recebível na data de liquidação
        PricingParameter parameter = pricingParameterMapper.toDomain(
                pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                                receivable.getType(), valuationDate
                        ).orElseThrow(() -> new DomainNotFoundException("Nenhum parâmetro de precificação vigente para: " + receivable.getType()))
        );

        // Resolve a estratégia de precificação pelo tipo do recebível e calcula o deságio
        PricingResult result = pricingStrategyResolver
                .resolve(receivable.getType())
                .calculate(receivable, parameter.baseRateAsFraction(), parameter.spreadRateAsFraction(), valuationDate);

        String originalCurrency = receivable.getFaceValue().getCurrencyCode();

        // Conversão cambial é opcional e sempre aplicada DEPOIS do deságio, sobre o presentValue
        String appliedTargetCurrency = null;
        BigDecimal exchangeRateUsed = null;
        BigDecimal convertedAmount = null;

        // Só converte se uma moeda alvo foi informada e é diferente da moeda original do título
        if (targetCurrencyCode != null && !targetCurrencyCode.equalsIgnoreCase(originalCurrency)) {
            // Busca a cotação mais recente entre a moeda original e a moeda alvo
            ExchangeRateResponse rate = exchangeRateService.findLatestRate(originalCurrency, targetCurrencyCode);
            exchangeRateUsed = rate.rate();

            // Converte o valor presente (já com deságio) para a moeda alvo
            convertedAmount = result.getPresentValue().getAmount().multiply(exchangeRateUsed).setScale(4, RoundingMode.HALF_EVEN);
            appliedTargetCurrency = targetCurrencyCode;
        }

        // Monta a resposta com o resultado da precificação e, se houver, a conversão cambial
        return new PricingSimulationResponse(
                receivableId, valuationDate, parameter.getBaseRate(), parameter.getSpreadRate(), result.getTermMonths(),
                receivable.getFaceValue().getAmount(), result.getDiscountAmount().getAmount(),
                result.getPresentValue().getAmount(), originalCurrency,
                appliedTargetCurrency, exchangeRateUsed, convertedAmount
        );
    }

}
