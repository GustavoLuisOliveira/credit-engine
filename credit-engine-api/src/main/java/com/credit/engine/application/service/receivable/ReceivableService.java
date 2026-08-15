package com.credit.engine.application.service.receivable;

import com.credit.engine.application.dto.receivable.ReceivableRequest;
import com.credit.engine.application.dto.receivable.ReceivableResponse;

import java.util.List;
import java.util.UUID;

public interface ReceivableService {

    ReceivableResponse create(ReceivableRequest request);

    ReceivableResponse update(UUID id, ReceivableRequest request);

    ReceivableResponse findById(UUID id);

    List<ReceivableResponse> findByAssignor(UUID assignorId);

    List<ReceivableResponse> findAll();

}
