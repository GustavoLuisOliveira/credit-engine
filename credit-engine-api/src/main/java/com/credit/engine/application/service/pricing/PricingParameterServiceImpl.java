package com.credit.engine.application.service.pricing;

import com.credit.engine.application.dto.pricing.PricingParameterRequest;
import com.credit.engine.application.dto.pricing.PricingParameterResponse;
import com.credit.engine.domain.model.pricing.PricingParameter;
import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.mapper.pricing.PricingParameterMapper;
import com.credit.engine.infrastructure.persistence.repository.pricing.PricingParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingParameterServiceImpl implements PricingParameterService {

    private final PricingParameterRepository pricingParameterRepository;
    private final PricingParameterMapper pricingParameterMapper;

    @Override
    @Transactional
    public PricingParameterResponse create(PricingParameterRequest request) {
        PricingParameter domain = PricingParameter.create(
                request.receivableType(), request.baseRate(), request.spreadRate(), request.effectiveDate()
        );

        var saved = pricingParameterRepository.save(pricingParameterMapper.toEntity(domain));
        return PricingParameterResponse.toResponse(pricingParameterMapper.toDomain(saved));
    }

    @Override
    public PricingParameterResponse findCurrent(ReceivableType receivableType) {
        var entity = pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                receivableType, LocalDate.now()
        ).orElseThrow(() -> new DomainNotFoundException("Nenhum parâmetro de precificação vigente para: " + receivableType));

        return PricingParameterResponse.toResponse(pricingParameterMapper.toDomain(entity));
    }

    @Override
    public List<PricingParameterResponse> findHistory(ReceivableType receivableType) {
        return pricingParameterRepository.findByReceivableTypeOrderByEffectiveDateDesc(receivableType).stream()
                .map(pricingParameterMapper::toDomain)
                .map(PricingParameterResponse::toResponse)
                .toList();
    }
}
