package com.bloominggrace.governance.exchange.infrastructure.controller;

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
    
    /**
     * 포인트를 토큰으로 교환 요청
     */
    @PostMapping("/request")
    public ResponseEntity<ExchangeRequestResponse> requestExchange(@RequestBody ExchangeRequestRequest request) {
        try {
            PointAmount pointAmount = new PointAmount(request.pointAmount());
            ExchangeRequestId exchangeRequestId = exchangeApplicationService.requestExchange(
                request.userId(), 
                pointAmount, 
                request.walletAddress()
            );
            
            return ResponseEntity.ok(new ExchangeRequestResponse(
                exchangeRequestId.getValue(),
                "교환 요청이 생성되었습니다"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExchangeRequestResponse(
                null,
                "교환 요청 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 교환 처리
     */
    @PostMapping("/{exchangeRequestId}/process")
    public ResponseEntity<String> processExchange(@PathVariable UUID exchangeRequestId) {
        try {
            exchangeApplicationService.processExchange(new ExchangeRequestId(exchangeRequestId));
            return ResponseEntity.ok("교환이 처리되었습니다");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("교환 처리 실패: " + e.getMessage());
        }
    }
    
    /**
     * 교환 완료
     */
    @PostMapping("/{exchangeRequestId}/complete")
    public ResponseEntity<String> completeExchange(@PathVariable UUID exchangeRequestId) {
        try {
            exchangeApplicationService.completeExchange(new ExchangeRequestId(exchangeRequestId));
            return ResponseEntity.ok("교환이 완료되었습니다");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("교환 완료 실패: " + e.getMessage());
        }
    }
    
    /**
     * 교환 취소
     */
    @PostMapping("/{exchangeRequestId}/cancel")
    public ResponseEntity<String> cancelExchange(@PathVariable UUID exchangeRequestId) {
        try {
            exchangeApplicationService.cancelExchange(new ExchangeRequestId(exchangeRequestId));
            return ResponseEntity.ok("교환이 취소되었습니다");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("교환 취소 실패: " + e.getMessage());
        }
    }
    
    /**
     * 사용자의 교환 요청 목록 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExchangeRequest>> getExchangeRequests(@PathVariable UUID userId) {
        try {
            List<ExchangeRequest> requests = exchangeApplicationService.getExchangeRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 특정 교환 요청 조회
     */
    @GetMapping("/{exchangeRequestId}")
    public ResponseEntity<ExchangeRequest> getExchangeRequest(@PathVariable UUID exchangeRequestId) {
        try {
            return exchangeApplicationService.getExchangeRequest(new ExchangeRequestId(exchangeRequestId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== DTO 클래스들 =====
    
    public record ExchangeRequestRequest(
        UUID userId,
        BigDecimal pointAmount,
        String walletAddress
    ) {}
    
    public record ExchangeRequestResponse(
        UUID exchangeRequestId,
        String message
    ) {}
} 