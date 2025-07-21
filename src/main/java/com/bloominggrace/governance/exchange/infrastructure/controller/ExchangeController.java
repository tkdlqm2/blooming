package com.bloominggrace.governance.exchange.infrastructure.controller;

import com.bloominggrace.governance.exchange.application.service.ExchangeApplicationService;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequest;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.infrastructure.service.JwtService;
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
    private final JwtService jwtService;
    
    /**
     * 포인트를 토큰으로 교환 요청
     */
    @PostMapping("/request")
    public ResponseEntity<ExchangeRequestResponse> requestExchange(
            @RequestBody ExchangeRequestRequest request,
            @RequestHeader("Authorization") String authorization) {
        try {
            // JWT 토큰에서 Bearer 제거
            String token = authorization.replace("Bearer ", "");
            
            // JWT 토큰에서 사용자 ID 추출
            UUID userId = jwtService.getUserIdFromToken(token);
            
            ExchangeRequest exchangeRequest = exchangeApplicationService.createExchangeRequest(
                new UserId(userId), 
                request.pointAmount(), 
                request.walletAddress()
            );
            
            return ResponseEntity.ok(new ExchangeRequestResponse(
                exchangeRequest.getId().getValue(),
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
            exchangeApplicationService.processExchangeRequest(new ExchangeRequestId(exchangeRequestId));
            return ResponseEntity.ok("교환이 처리되었습니다");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("교환 처리 실패: " + e.getMessage());
        }
    }
    
    /**
     * 교환 요청 조회
     */
    @GetMapping("/{exchangeRequestId}")
    public ResponseEntity<ExchangeRequest> getExchangeRequest(@PathVariable UUID exchangeRequestId) {
        try {
            ExchangeRequest exchangeRequest = exchangeApplicationService.getExchangeRequest(new ExchangeRequestId(exchangeRequestId));
            return ResponseEntity.ok(exchangeRequest);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 테스트용 교환 요청 생성 (JWT 인증 없음)
     */
    @PostMapping("/test-request")
    public ResponseEntity<ExchangeRequestResponse> testRequestExchange(
            @RequestBody ExchangeRequestRequest request) {
        try {
            // 테스트용 사용자 ID 사용
            UUID testUserId = UUID.randomUUID();
            
            ExchangeRequest exchangeRequest = exchangeApplicationService.createExchangeRequest(
                new UserId(testUserId), 
                request.pointAmount(), 
                request.walletAddress()
            );
            
            return ResponseEntity.ok(new ExchangeRequestResponse(
                exchangeRequest.getId().getValue(),
                "테스트 교환 요청이 생성되었습니다"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExchangeRequestResponse(
                null,
                "테스트 교환 요청 실패: " + e.getMessage()
            ));
        }
    }

    // ===== DTO 클래스들 =====
    
    public record ExchangeRequestRequest(
        BigDecimal pointAmount,
        String walletAddress
    ) {}
    
    public record ExchangeRequestResponse(
        UUID exchangeRequestId,
        String message
    ) {}
} 