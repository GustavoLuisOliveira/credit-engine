package com.credit.engine.application.service.settlement;

import com.credit.engine.application.dto.settlement.SettlementRequest;
import com.credit.engine.application.dto.settlement.SettlementResponse;

import java.util.List;
import java.util.UUID;

public interface SettlementService {

    SettlementResponse execute(SettlementRequest request);

    SettlementResponse findById(UUID id);

    List<SettlementResponse> findByAssignor(UUID assignorId);

}
