package com.credit.engine.application.service.assignor;

import com.credit.engine.application.dto.assignor.AssignorRequest;
import com.credit.engine.application.dto.assignor.AssignorResponse;
import com.credit.engine.domain.model.assignor.Assignor;
import com.credit.engine.domain.shared.cnpj.Cnpj;
import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.mapper.assignor.AssignorMapper;
import com.credit.engine.infrastructure.persistence.repository.assignor.AssignorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignorServiceImpl implements AssignorService {

    private final AssignorRepository assignorRepository;
    private final AssignorMapper assignorMapper;

    @Override
    @Transactional
    public AssignorResponse create(AssignorRequest request) {
        Cnpj cnpj = Cnpj.of(request.documentNumber());

        if (assignorRepository.existsByDocumentNumber(cnpj.digits()))
            throw new DomainConflictException("Já existe um cedente cadastrado com o CNPJ " + cnpj.formatted());

        Assignor assignor = Assignor.create(
                cnpj, request.name(), request.email(), request.phone()
        );

        var saved = assignorRepository.save(assignorMapper.toEntity(assignor));

        return AssignorResponse.toResponse(assignorMapper.toDomain(saved));
    }

    @Override
    @Transactional
    public AssignorResponse update(UUID id, AssignorRequest request) {
        Assignor currentAssignor = findDomainById(id);

        Cnpj newCnpj = Cnpj.of(request.documentNumber());
        if (!currentAssignor.getDocumentNumber().equals(newCnpj))
            throw new DomainConflictException("CNPJ do cedente não pode ser alterado após o cadastro.");

        Assignor updatedAssignor = currentAssignor.update(
                request.name(),
                request.email(),
                request.phone()
        );

        var saved = assignorRepository.save(assignorMapper.toEntity(updatedAssignor));

        return AssignorResponse.toResponse(assignorMapper.toDomain(saved));
    }

    @Override
    public AssignorResponse findById(UUID id) {
        return AssignorResponse.toResponse(
                findDomainById(id)
        );
    }

    private Assignor findDomainById(UUID id) {
        return assignorMapper.toDomain(
                assignorRepository.findById(id)
                        .orElseThrow(() -> new DomainNotFoundException("Cedente não encontrado: " + id))
        );
    }

    @Override
    public AssignorResponse findByDocumentNumber(String documentNumber) {
        Cnpj cnpj = Cnpj.of(documentNumber);

        var entity = assignorRepository.findByDocumentNumber(cnpj.digits())
                .orElseThrow(() -> new DomainNotFoundException("Cedente não encontrado para o CNPJ: " + cnpj.formatted()));

        return AssignorResponse.toResponse(assignorMapper.toDomain(entity));
    }

}
