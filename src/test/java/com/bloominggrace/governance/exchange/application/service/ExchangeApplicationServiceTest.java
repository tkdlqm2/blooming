package com.bloominggrace.governance.exchange.application.service;

import com.bloominggrace.governance.exchange.domain.model.ExchangeRequest;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.exchange.domain.model.ExchangeStatus;
import com.bloominggrace.governance.exchange.infrastructure.repository.ExchangeRequestRepository;
import com.bloominggrace.governance.point.application.service.PointManagementService;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.infrastructure.service.AdminWalletService;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator;
import com.bloominggrace.governance.token.application.service.TokenAccountApplicationService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeApplicationService ERC20 Transfer Tests")
class ExchangeApplicationServiceTest {

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private PointManagementService pointManagementService;

    @Mock
    private TokenAccountApplicationService tokenAccountApplicationService;

    // AdminWalletService는 static 메서드로 변경되어 mock 제거

    @Mock
    private TransactionOrchestrator transactionOrchestrator;

    @InjectMocks
    private ExchangeApplicationService exchangeApplicationService;

    private static final String USER_ID = "f2aec616-1dcb-4e56-923d-16e07a58ae3c";
    private static final String WALLET_ADDRESS = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6";
    private static final String ADMIN_WALLET_ADDRESS = "0x55D5c49e36f8A89111687C9DC8355121068f0cD8";
    private static final BigDecimal POINT_AMOUNT = new BigDecimal("100.0");
    private static final String TRANSACTION_HASH = "0x" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

    @BeforeEach
    void setUp() {
        // AdminWalletService는 static 메서드로 변경되어 mock 불필요
    }

    @Test
    @DisplayName("교환 요청 처리 - ERC20 전송 성공")
    void processExchangeRequest_ERC20TransferSuccess() throws Exception {
        // Given
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId(UUID.randomUUID());
        ExchangeRequest exchangeRequest = createMockExchangeRequest(exchangeRequestId);

        when(exchangeRequestRepository.findById(exchangeRequestId)).thenReturn(java.util.Optional.of(exchangeRequest));
        when(tokenAccountApplicationService.findOrCreateDefaultTokenAccount(
            anyString(), eq(WALLET_ADDRESS), eq(NetworkType.ETHEREUM)
        )).thenReturn(null);

        TransactionOrchestrator.TransactionResult mockResult = TransactionOrchestrator.TransactionResult.success(
            UUID.randomUUID(), TRANSACTION_HASH, ADMIN_WALLET_ADDRESS, NetworkType.ETHEREUM.name(), "ERC20 transfer: " + POINT_AMOUNT
        );

        when(transactionOrchestrator.executeTransfer(
            eq(ADMIN_WALLET_ADDRESS), eq(WALLET_ADDRESS), eq(NetworkType.ETHEREUM), any(BigDecimal.class), anyString()
        )).thenReturn(mockResult);

        // When
        exchangeApplicationService.processExchangeRequest(exchangeRequestId);

        // Then
        verify(exchangeRequestRepository).findById(exchangeRequestId);
        verify(pointManagementService).freezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        verify(transactionOrchestrator).executeTransfer(
            eq(ADMIN_WALLET_ADDRESS), eq(WALLET_ADDRESS), eq(NetworkType.ETHEREUM), any(BigDecimal.class), anyString()
        );
        verify(tokenAccountApplicationService).findOrCreateDefaultTokenAccount(
            anyString(), eq(WALLET_ADDRESS), eq(NetworkType.ETHEREUM)
        );
        verify(exchangeRequestRepository, times(2)).save(exchangeRequest); // process() and complete()
    }

    @Test
    @DisplayName("교환 요청 처리 - ERC20 전송 실패")
    void processExchangeRequest_ERC20TransferFailure() throws Exception {
        // Given
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId(UUID.randomUUID());
        ExchangeRequest exchangeRequest = createMockExchangeRequest(exchangeRequestId);

        when(exchangeRequestRepository.findById(exchangeRequestId)).thenReturn(java.util.Optional.of(exchangeRequest));

        TransactionOrchestrator.TransactionResult mockResult = TransactionOrchestrator.TransactionResult.failure(
            UUID.randomUUID(), ADMIN_WALLET_ADDRESS, NetworkType.ETHEREUM.name(), "Transaction failed"
        );

        when(transactionOrchestrator.executeTransfer(
            eq(ADMIN_WALLET_ADDRESS), eq(WALLET_ADDRESS), eq(NetworkType.ETHEREUM), any(BigDecimal.class), anyString()
        )).thenReturn(mockResult);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exchangeApplicationService.processExchangeRequest(exchangeRequestId);
        });

        assertTrue(exception.getMessage().contains("Token transfer failed: No transaction hash returned"));

        // Verify interactions
        verify(exchangeRequestRepository).findById(exchangeRequestId);
        verify(pointManagementService).freezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        verify(transactionOrchestrator).executeTransfer(
            eq(ADMIN_WALLET_ADDRESS), eq(WALLET_ADDRESS), eq(NetworkType.ETHEREUM), any(BigDecimal.class), anyString()
        );
        verify(pointManagementService).unfreezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        verify(exchangeRequestRepository, times(2)).save(exchangeRequest); // process() and fail()
    }

    @Test
    @DisplayName("교환 요청 처리 - 트랜잭션 오케스트레이터 예외")
    void processExchangeRequest_OrchestratorException() throws Exception {
        // Given
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId(UUID.randomUUID());
        ExchangeRequest exchangeRequest = createMockExchangeRequest(exchangeRequestId);

        when(exchangeRequestRepository.findById(exchangeRequestId)).thenReturn(java.util.Optional.of(exchangeRequest));

        when(transactionOrchestrator.executeTransfer(
            eq(ADMIN_WALLET_ADDRESS), eq(WALLET_ADDRESS), eq(NetworkType.ETHEREUM), any(BigDecimal.class), anyString()
        )).thenThrow(new RuntimeException("Orchestrator error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exchangeApplicationService.processExchangeRequest(exchangeRequestId);
        });

        assertTrue(exception.getMessage().contains("Failed to process exchange request: Orchestrator error"));

        // Verify interactions
        verify(exchangeRequestRepository).findById(exchangeRequestId);
        verify(pointManagementService).freezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        verify(transactionOrchestrator).executeTransfer(
            eq(ADMIN_WALLET_ADDRESS), eq(WALLET_ADDRESS), eq(NetworkType.ETHEREUM), any(BigDecimal.class), anyString()
        );
        verify(pointManagementService).unfreezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        verify(exchangeRequestRepository, times(2)).save(exchangeRequest); // process() and fail()
    }

    @Test
    @DisplayName("교환 요청 처리 - Admin 지갑 없음")
    void processExchangeRequest_AdminWalletNotFound() throws Exception {
        // Given
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId(UUID.randomUUID());
        ExchangeRequest exchangeRequest = createMockExchangeRequest(exchangeRequestId);

        when(exchangeRequestRepository.findById(exchangeRequestId)).thenReturn(java.util.Optional.of(exchangeRequest));
        // AdminWalletService는 static 메서드로 변경되어 mock 불필요

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exchangeApplicationService.processExchangeRequest(exchangeRequestId);
        });

        assertTrue(exception.getMessage().contains("Admin wallet not found for network: ETHEREUM"));

        // Verify interactions
        verify(exchangeRequestRepository).findById(exchangeRequestId);
        verify(pointManagementService).freezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        // AdminWalletService는 static 메서드로 변경되어 verify 불필요
        verify(pointManagementService).unfreezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        verify(exchangeRequestRepository, times(2)).save(exchangeRequest); // process() and fail()
    }

    @Test
    @DisplayName("교환 요청 처리 - 지원하지 않는 네트워크")
    void processExchangeRequest_UnsupportedNetwork() throws Exception {
        // Given
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId(UUID.randomUUID());
        ExchangeRequest exchangeRequest = createMockExchangeRequestWithSolanaAddress(exchangeRequestId);

        when(exchangeRequestRepository.findById(exchangeRequestId)).thenReturn(java.util.Optional.of(exchangeRequest));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exchangeApplicationService.processExchangeRequest(exchangeRequestId);
        });

        assertTrue(exception.getMessage().contains("Unsupported network type: SOLANA"));

        // Verify interactions
        verify(exchangeRequestRepository).findById(exchangeRequestId);
        verify(pointManagementService).freezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        verify(pointManagementService).unfreezePoints(
            eq(UUID.fromString(USER_ID)), 
            any(PointAmount.class), 
            eq(exchangeRequestId.getValue().toString())
        );
        verify(exchangeRequestRepository, times(2)).save(exchangeRequest); // process() and fail()
    }

    private ExchangeRequest createMockExchangeRequest(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest exchangeRequest = new ExchangeRequest(
            UUID.fromString(USER_ID), 
            new PointAmount(POINT_AMOUNT), 
            WALLET_ADDRESS
        );
        // Set the ID manually for testing
        exchangeRequest.getId().setValue(exchangeRequestId.getValue());
        return exchangeRequest;
    }

    private ExchangeRequest createMockExchangeRequestWithSolanaAddress(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest exchangeRequest = new ExchangeRequest(
            UUID.fromString(USER_ID), 
            new PointAmount(POINT_AMOUNT), 
            "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v" // Solana address
        );
        // Set the ID manually for testing
        exchangeRequest.getId().setValue(exchangeRequestId.getValue());
        return exchangeRequest;
    }
} 