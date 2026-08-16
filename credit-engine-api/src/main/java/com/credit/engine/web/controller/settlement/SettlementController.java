package com.credit.engine.web.controller.settlement;

import com.credit.engine.application.dto.settlement.SettlementRequest;
import com.credit.engine.application.dto.settlement.SettlementResponse;
import com.credit.engine.application.service.settlement.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<SettlementResponse> execute(@Valid @RequestBody SettlementRequest request) {
        SettlementResponse response = settlementService.execute(request);
        return ResponseEntity.created(URI.create("/api/settlements/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SettlementResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(settlementService.findById(id));
    }

    @GetMapping(params = "assignorId")
    public ResponseEntity<List<SettlementResponse>> findByAssignor(@RequestParam UUID assignorId) {
        return ResponseEntity.ok(settlementService.findByAssignor(assignorId));
    }

}
