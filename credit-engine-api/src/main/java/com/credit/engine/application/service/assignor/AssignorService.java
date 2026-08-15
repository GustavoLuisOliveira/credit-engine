package com.credit.engine.application.service.assignor;

import com.credit.engine.application.dto.assignor.AssignorRequest;
import com.credit.engine.application.dto.assignor.AssignorResponse;

import java.util.UUID;

public interface AssignorService {

    AssignorResponse create(AssignorRequest request);

    AssignorResponse update(UUID id, AssignorRequest request);

    AssignorResponse findById(UUID id);

    AssignorResponse findByDocumentNumber(String documentNumber);

}
