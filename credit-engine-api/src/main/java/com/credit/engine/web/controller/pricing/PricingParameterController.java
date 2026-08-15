package com.credit.engine.web.controller.pricing;

import com.credit.engine.application.dto.pricing.PricingParameterRequest;
import com.credit.engine.application.dto.pricing.PricingParameterResponse;
import com.credit.engine.application.service.pricing.PricingParameterService;
import com.credit.engine.domain.model.receivable.ReceivableType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing-parameters")
@RequiredArgsConstructor
public class PricingParameterController {

    private final PricingParameterService pricingParameterService;

    @PostMapping
    public ResponseEntity<PricingParameterResponse> create(@Valid @RequestBody PricingParameterRequest request) {
        return ResponseEntity.ok(pricingParameterService.create(request));
    }

    @GetMapping("/{receivableType}/current")
    public ResponseEntity<PricingParameterResponse> findCurrent(@PathVariable ReceivableType receivableType) {
        return ResponseEntity.ok(pricingParameterService.findCurrent(receivableType));
    }

    @GetMapping("/{receivableType}/history")
    public ResponseEntity<List<PricingParameterResponse>> findHistory(@PathVariable ReceivableType receivableType) {
        return ResponseEntity.ok(pricingParameterService.findHistory(receivableType));
    }

}
