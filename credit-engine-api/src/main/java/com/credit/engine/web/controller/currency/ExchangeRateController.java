package com.credit.engine.web.controller.currency;

import com.credit.engine.application.dto.currency.ExchangeRateRequest;
import com.credit.engine.application.dto.currency.ExchangeRateResponse;
import com.credit.engine.application.service.currency.ExchangeRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping
    public ResponseEntity<ExchangeRateResponse> create(@Valid @RequestBody ExchangeRateRequest request) {
        return ResponseEntity.ok(exchangeRateService.create(request));
    }

    @GetMapping("/latest")
    public ResponseEntity<ExchangeRateResponse> findLatest(@RequestParam String origin, @RequestParam String destination) {
        return ResponseEntity.ok(exchangeRateService.findLatestRate(origin, destination));
    }

}
