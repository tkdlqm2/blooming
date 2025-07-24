package com.bloominggrace.governance.exchange.application.service;

import com.bloominggrace.governance.exchange.domain.model.ExchangeRequest;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.exchange.domain.model.ExchangeStatus;
import com.bloominggrace.governance.exchange.infrastructure.repository.ExchangeRequestRepository;
import com.bloominggrace.governance.point.application.service.PointManagementService;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.security.infrastructure.service.AdminWalletService;
import com.bloominggrace.governance.shared.blockchain.infrastructure.service.TransactionOrchestrator;
import com.bloominggrace.governance.token.application.service.TokenAccountApplicationService;
import com.bloominggrace.governance.token.infrastructure.repository.TokenAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeApplicationService 테스트")
class ExchangeApplicationServiceTest {

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private PointManagementService pointManagementService;

    @Mock
    private TokenAccountApplicationService tokenAccountApplicationService;

    @Mock
    private TransactionOrchestrator transactionOrchestrator;

    @Mock
    private TokenAccountRepository tokenAccountRepository;

    @Mock
    private AdminWalletService adminWalletService;

    @InjectMocks
    private ExchangeApplicationService exchangeApplicationService;

    private UserId testUserId;
    private ExchangeRequestId testExchangeRequestId;
    private ExchangeRequest testExchangeRequest;
    private PointManagementService.PointBalance testPointBalance;
    private BigDecimal testPointAmount;
    private String testWalletAddress;

    @BeforeEach
    void setUp() {
        testUserId = new UserId(UUID.randomUUID());
        testExchangeRequestId = ExchangeRequestId.generate();
        testPointAmount = new BigDecimal("1000.00");
        testWalletAddress = "0x1234567890abcdef";
        
        testExchangeRequest = new ExchangeRequest(
            testUserId.getValue(),
            PointAmount.of(testPointAmount),
            testWalletAddress
        );
        
        testPointBalance = new PointManagementService.PointBalance(
            PointAmount.of(new BigDecimal("2000.00")),
            PointAmount.of(new BigDecimal("0.00")),
            PointAmount.of(new BigDecimal("2000.00"))
        );
    }

    @Test
    @DisplayName("교환 요청을 조회할 수 있다")
    void getExchangeRequest() {
        // given
        when(exchangeRequestRepository.findById(testExchangeRequestId))
            .thenReturn(Optional.of(testExchangeRequest));

        // when
        ExchangeRequest result = exchangeApplicationService.getExchangeRequest(testExchangeRequestId);

        // then
        assertThat(result).isEqualTo(testExchangeRequest);
        verify(exchangeRequestRepository).findById(testExchangeRequestId);
    }

    @Test
    @DisplayName("존재하지 않는 교환 요청 조회 시 예외가 발생한다")
    void getExchangeRequestNotFound() {
        // given
        when(exchangeRequestRepository.findById(testExchangeRequestId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> exchangeApplicationService.getExchangeRequest(testExchangeRequestId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Exchange request not found: " + testExchangeRequestId.getValue());

        verify(exchangeRequestRepository).findById(testExchangeRequestId);
    }

    @Test
    @DisplayName("유효한 정보로 교환 요청을 생성할 수 있다")
    void createExchangeRequestWithValidInfo() {
        // given
        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(testPointBalance);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class)))
            .thenReturn(testExchangeRequest);

        // when
        ExchangeRequest result = exchangeApplicationService.createExchangeRequest(
            testUserId, testPointAmount, testWalletAddress
        );

        // then
        assertThat(result).isEqualTo(testExchangeRequest);
        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("포인트 잔액이 부족한 경우 교환 요청 생성 시 예외가 발생한다")
    void createExchangeRequestWithInsufficientBalance() {
        // given
        BigDecimal insufficientPointAmount = new BigDecimal("3000.00");
        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(testPointBalance);

        // when & then
        assertThatThrownBy(() -> exchangeApplicationService.createExchangeRequest(
            testUserId, insufficientPointAmount, testWalletAddress
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Insufficient point balance");

        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository, never()).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("null 지갑 주소로 교환 요청 생성 시 예외가 발생한다")
    void createExchangeRequestWithNullWalletAddress() {
        // given
        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(testPointBalance);

        // when & then
        assertThatThrownBy(() -> exchangeApplicationService.createExchangeRequest(
            testUserId, testPointAmount, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Wallet address is required");

        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository, never()).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("빈 지갑 주소로 교환 요청 생성 시 예외가 발생한다")
    void createExchangeRequestWithEmptyWalletAddress() {
        // given
        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(testPointBalance);

        // when & then
        assertThatThrownBy(() -> exchangeApplicationService.createExchangeRequest(
            testUserId, testPointAmount, ""
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Wallet address is required");

        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository, never()).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("공백만 있는 지갑 주소로 교환 요청 생성 시 예외가 발생한다")
    void createExchangeRequestWithBlankWalletAddress() {
        // given
        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(testPointBalance);

        // when & then
        assertThatThrownBy(() -> exchangeApplicationService.createExchangeRequest(
            testUserId, testPointAmount, "   "
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Wallet address is required");

        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository, never()).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("큰 소수점 자릿수의 포인트로 교환 요청을 생성할 수 있다")
    void createExchangeRequestWithLargeDecimalPoints() {
        // given
        BigDecimal largePointAmount = new BigDecimal("123456789.123456789");
        PointManagementService.PointBalance largeBalance = new PointManagementService.PointBalance(
            PointAmount.of(new BigDecimal("200000000.000000000")),
            PointAmount.of(new BigDecimal("0.00")),
            PointAmount.of(new BigDecimal("200000000.000000000"))
        );

        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(largeBalance);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class)))
            .thenReturn(testExchangeRequest);

        // when
        ExchangeRequest result = exchangeApplicationService.createExchangeRequest(
            testUserId, largePointAmount, testWalletAddress
        );

        // then
        assertThat(result).isEqualTo(testExchangeRequest);
        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("긴 지갑 주소로 교환 요청을 생성할 수 있다")
    void createExchangeRequestWithLongWalletAddress() {
        // given
        String longWalletAddress = "0x1234567890abcdef1234567890abcdef12345678";
        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(testPointBalance);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class)))
            .thenReturn(testExchangeRequest);

        // when
        ExchangeRequest result = exchangeApplicationService.createExchangeRequest(
            testUserId, testPointAmount, longWalletAddress
        );

        // then
        assertThat(result).isEqualTo(testExchangeRequest);
        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("0 포인트로 교환 요청을 생성할 수 있다")
    void createExchangeRequestWithZeroPoints() {
        // given
        BigDecimal zeroPointAmount = BigDecimal.ZERO;
        PointManagementService.PointBalance zeroBalance = new PointManagementService.PointBalance(
            PointAmount.of(new BigDecimal("1000.00")),
            PointAmount.of(new BigDecimal("0.00")),
            PointAmount.of(new BigDecimal("1000.00"))
        );

        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(zeroBalance);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class)))
            .thenReturn(testExchangeRequest);

        // when
        ExchangeRequest result = exchangeApplicationService.createExchangeRequest(
            testUserId, zeroPointAmount, testWalletAddress
        );

        // then
        assertThat(result).isEqualTo(testExchangeRequest);
        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository).save(any(ExchangeRequest.class));
    }

    @Test
    @DisplayName("정확히 사용 가능한 포인트 잔액으로 교환 요청을 생성할 수 있다")
    void createExchangeRequestWithExactAvailableBalance() {
        // given
        BigDecimal exactPointAmount = new BigDecimal("2000.00");
        when(pointManagementService.getPointBalance(testUserId.getValue()))
            .thenReturn(testPointBalance);
        when(exchangeRequestRepository.save(any(ExchangeRequest.class)))
            .thenReturn(testExchangeRequest);

        // when
        ExchangeRequest result = exchangeApplicationService.createExchangeRequest(
            testUserId, exactPointAmount, testWalletAddress
        );

        // then
        assertThat(result).isEqualTo(testExchangeRequest);
        verify(pointManagementService).getPointBalance(testUserId.getValue());
        verify(exchangeRequestRepository).save(any(ExchangeRequest.class));
    }
} 