package com.bloominggrace.governance.exchange.application.controller;

import com.bloominggrace.governance.exchange.application.service.ExchangeApplicationService;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequest;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeApplicationService exchangeApplicationService;

    @PostMapping("/request")
    public ResponseEntity<ExchangeRequestResponse> requestExchange(@RequestBody ExchangeRequestRequest request) {
        PointAmount pointAmount = PointAmount.of(request.getPointAmount());
        ExchangeRequestId exchangeRequestId = exchangeApplicationService.requestExchange(
            request.getUserId(), pointAmount, request.getWalletAddress()
        );
        
        return ResponseEntity.ok(new ExchangeRequestResponse(exchangeRequestId.getValue()));
    }

    @PostMapping("/{exchangeRequestId}/process")
    public ResponseEntity<Void> processExchange(@PathVariable UUID exchangeRequestId) {
        exchangeApplicationService.processExchange(ExchangeRequestId.of(exchangeRequestId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{exchangeRequestId}/complete")
    public ResponseEntity<Void> completeExchange(@PathVariable UUID exchangeRequestId, @RequestBody CompleteExchangeRequest request) {
        exchangeApplicationService.completeExchange(ExchangeRequestId.of(exchangeRequestId), request.getTransactionSignature());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{exchangeRequestId}/cancel")
    public ResponseEntity<Void> cancelExchange(@PathVariable UUID exchangeRequestId) {
        exchangeApplicationService.cancelExchange(ExchangeRequestId.of(exchangeRequestId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{exchangeRequestId}")
    public ResponseEntity<ExchangeRequest> getExchangeRequest(@PathVariable UUID exchangeRequestId) {
        ExchangeRequest request = exchangeApplicationService.getExchangeRequest(ExchangeRequestId.of(exchangeRequestId));
        return ResponseEntity.ok(request);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExchangeRequest>> getUserExchangeHistory(@PathVariable UUID userId) {
        List<ExchangeRequest> history = exchangeApplicationService.getUserExchangeHistory(userId);
        return ResponseEntity.ok(history);
    }

    public static class ExchangeRequestRequest {
        private UUID userId;
        private BigDecimal pointAmount;
        private String walletAddress;

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public BigDecimal getPointAmount() { return pointAmount; }
        public void setPointAmount(BigDecimal pointAmount) { this.pointAmount = pointAmount; }
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
    }

    public static class ExchangeRequestResponse {
        private final UUID exchangeRequestId;

        public ExchangeRequestResponse(UUID exchangeRequestId) {
            this.exchangeRequestId = exchangeRequestId;
        }

        public UUID getExchangeRequestId() { return exchangeRequestId; }
    }

    public static class CompleteExchangeRequest {
        private String transactionSignature;

        public String getTransactionSignature() { return transactionSignature; }
        public void setTransactionSignature(String transactionSignature) { this.transactionSignature = transactionSignature; }
    }
} 