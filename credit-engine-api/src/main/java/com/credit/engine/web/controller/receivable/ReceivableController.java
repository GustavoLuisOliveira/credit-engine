package com.credit.engine.web.controller.receivable;

import com.credit.engine.application.dto.receivable.ReceivableRequest;
import com.credit.engine.application.dto.receivable.ReceivableResponse;
import com.credit.engine.application.service.receivable.ReceivableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/receivables")
@RequiredArgsConstructor
public class ReceivableController {

    private final ReceivableService receivableService;

    @PostMapping
    public ResponseEntity<ReceivableResponse> create(@Valid @RequestBody ReceivableRequest request) {
        ReceivableResponse response = receivableService.create(request);
        return ResponseEntity.created(URI.create("/api/receivables/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceivableResponse> update(@PathVariable UUID id, @Valid @RequestBody ReceivableRequest request) {
        return ResponseEntity.ok(receivableService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceivableResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(receivableService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReceivableResponse>> findAll() {
        return ResponseEntity.ok(receivableService.findAll());
    }

    @GetMapping(params = "assignorId")
    public ResponseEntity<List<ReceivableResponse>> findByAssignor(@RequestParam UUID assignorId) {
        return ResponseEntity.ok(receivableService.findByAssignor(assignorId));
    }

}
