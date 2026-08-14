package com.credit.engine.web.controller.currency;

import com.credit.engine.application.dto.currency.CurrencyRequest;
import com.credit.engine.application.dto.currency.CurrencyResponse;
import com.credit.engine.application.service.currency.CurrencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @PostMapping
    public ResponseEntity<CurrencyResponse> create(@Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse response = currencyService.create(request);
        return ResponseEntity.created(URI.create("/api/currencies/" + response.code())).body(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<CurrencyResponse> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(currencyService.findByCode(code));
    }

    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> findAll() {
        return ResponseEntity.ok(currencyService.findAll());
    }

}
