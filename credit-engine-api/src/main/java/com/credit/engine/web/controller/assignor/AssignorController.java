package com.credit.engine.web.controller.assignor;

import com.credit.engine.application.dto.assignor.AssignorRequest;
import com.credit.engine.application.dto.assignor.AssignorResponse;
import com.credit.engine.application.service.assignor.AssignorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignors")
@RequiredArgsConstructor
public class AssignorController {

    private final AssignorService assignorService;

    @PostMapping
    public ResponseEntity<AssignorResponse> create(@Valid @RequestBody AssignorRequest request) {
        AssignorResponse created = assignorService.create(request);
        return ResponseEntity.created(URI.create("/api/assignors/" + created.id()))
                .body(created);
    }

    @PutMapping("{id}")
    public ResponseEntity<AssignorResponse> update(@PathVariable UUID id, @Valid @RequestBody AssignorRequest request) {
        return ResponseEntity.ok(assignorService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(assignorService.findById(id));
    }

    @GetMapping
    public ResponseEntity<AssignorResponse> findByDocumentNumber(@RequestParam String documentNumber) {
        return ResponseEntity.ok(assignorService.findByDocumentNumber(documentNumber));
    }

}
