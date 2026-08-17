package com.credit.engine.application.service.settlement;

import com.credit.engine.application.dto.settlement.SettlementExtractResponse;
import com.credit.engine.application.dto.settlement.SettlementRequest;
import com.credit.engine.application.dto.settlement.SettlementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SettlementService {

    SettlementResponse execute(SettlementRequest request);

    SettlementResponse findById(UUID id);

    List<SettlementResponse> findByAssignor(UUID assignorId);

    Page<SettlementExtractResponse> extract(UUID assignorId, String currencyCode, LocalDate valuationDateFrom, LocalDate valuationDateTo, Pageable pageable);

}
