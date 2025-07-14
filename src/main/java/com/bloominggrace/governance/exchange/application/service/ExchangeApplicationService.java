package com.bloominggrace.governance.exchange.application.service;

import com.bloominggrace.governance.exchange.domain.model.ExchangeRequest;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.exchange.infrastructure.repository.ExchangeRequestRepository;
import com.bloominggrace.governance.point.application.service.PointManagementService;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ExchangeApplicationService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final PointManagementService pointManagementService;
    private final DomainEventPublisher eventPublisher;

    // 교환 요청 생성
    public ExchangeRequestId requestExchange(UUID userId, PointAmount pointAmount, String walletAddress) {
        // 교환 요청 생성
        ExchangeRequest request = new ExchangeRequest(userId, pointAmount, walletAddress);
        exchangeRequestRepository.save(request);

        // 포인트 동결
        pointManagementService.freezePoints(userId, pointAmount, request.getId().toString());

        eventPublisher.publishAll(request.getDomainEvents());
        return request.getId();
    }

    // 교환 처리 시작
    public void processExchange(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest request = exchangeRequestRepository.findById(exchangeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("교환 요청을 찾을 수 없습니다"));
        
        request.process();
        exchangeRequestRepository.save(request);

        eventPublisher.publishAll(request.getDomainEvents());
    }

    // 교환 완료
    public void completeExchange(ExchangeRequestId exchangeRequestId, String transactionSignature) {
        ExchangeRequest request = exchangeRequestRepository.findById(exchangeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("교환 요청을 찾을 수 없습니다"));
        
        request.complete(transactionSignature);
        exchangeRequestRepository.save(request);

        eventPublisher.publishAll(request.getDomainEvents());
    }

    // 교환 취소
    public void cancelExchange(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest request = exchangeRequestRepository.findById(exchangeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("교환 요청을 찾을 수 없습니다"));
        
        request.cancel();
        exchangeRequestRepository.save(request);

        // 포인트 해제
        pointManagementService.unfreezePoints(request.getUserId(), request.getPointAmount(), exchangeRequestId.getValue().toString());

        eventPublisher.publishAll(request.getDomainEvents());
    }

    // 교환 요청 조회
    @Transactional(readOnly = true)
    public ExchangeRequest getExchangeRequest(ExchangeRequestId exchangeRequestId) {
        return exchangeRequestRepository.findById(exchangeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("교환 요청을 찾을 수 없습니다"));
    }

    // 사용자별 교환 내역 조회
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getUserExchangeHistory(UUID userId) {
        return exchangeRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
} 