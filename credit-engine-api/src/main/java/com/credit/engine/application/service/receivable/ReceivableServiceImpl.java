package com.credit.engine.application.service.receivable;

import com.credit.engine.application.dto.receivable.ReceivableRequest;
import com.credit.engine.application.dto.receivable.ReceivableResponse;
import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.domain.shared.money.Money;
import com.credit.engine.infrastructure.persistence.mapper.receivable.ReceivableMapper;
import com.credit.engine.infrastructure.persistence.repository.assignor.AssignorRepository;
import com.credit.engine.infrastructure.persistence.repository.receivable.ReceivableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceivableServiceImpl implements ReceivableService {

    private final ReceivableRepository receivableRepository;
    private final AssignorRepository assignorRepository;
    private final ReceivableMapper receivableMapper;


    @Override
    @Transactional
    public ReceivableResponse create(ReceivableRequest request) {
        if (!assignorRepository.existsById(request.assignorId()))
            throw new DomainNotFoundException("Cedente não encontrado: " + request.assignorId());

        Money faceValue = Money.of(request.faceValue(), request.currencyCode());
        Receivable domain = Receivable.create(
                request.assignorId(), request.type(), request.documentNumber(), faceValue, request.dueDate()
        );

        var saved = receivableRepository.save(receivableMapper.toEntity(domain));
        return ReceivableResponse.toResponse(receivableMapper.toDomain(saved));
    }

    @Override
    @Transactional
    public ReceivableResponse update(UUID id, ReceivableRequest request) {
        Receivable currentReceivable = findDomainById(id);

        if (!currentReceivable.getAssignorId().equals(request.assignorId()))
            throw new DomainConflictException("Cedente do título não pode ser alterado após o cadastro.");

        if (currentReceivable.getType() != request.type())
            throw new DomainConflictException("Tipo do título não pode ser alterado após o cadastro.");

        Money faceValue = Money.of(request.faceValue(), request.currencyCode());
        Receivable updatedReceivable = currentReceivable.update(
                request.documentNumber(), faceValue, request.dueDate());

        var saved = receivableRepository.save(receivableMapper.toEntity(updatedReceivable));
        return ReceivableResponse.toResponse(receivableMapper.toDomain(saved));
    }

    @Override
    public ReceivableResponse findById(UUID id) {
        return ReceivableResponse.toResponse(
                findDomainById(id)
        );
    }

    private Receivable findDomainById(UUID id) {
        return receivableMapper.toDomain(
                receivableRepository.findById(id)
                        .orElseThrow(() -> new DomainNotFoundException("Título não encontrado: " + id))
        );
    }

    @Override
    public List<ReceivableResponse> findByAssignor(UUID assignorId) {
        return receivableRepository.findByAssignorId(assignorId).stream()
                .map(receivableMapper::toDomain)
                .map(ReceivableResponse::toResponse)
                .toList();
    }

    @Override
    public List<ReceivableResponse> findAll() {
        return receivableRepository.findAll().stream()
                .map(receivableMapper::toDomain)
                .map(ReceivableResponse::toResponse)
                .toList();
    }
}
