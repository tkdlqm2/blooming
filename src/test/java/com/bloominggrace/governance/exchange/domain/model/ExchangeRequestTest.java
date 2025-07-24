package com.bloominggrace.governance.exchange.domain.model;

import com.bloominggrace.governance.point.domain.model.PointAmount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ExchangeRequest 도메인 모델 테스트")
class ExchangeRequestTest {

    private UUID testUserId;
    private PointAmount testPointAmount;
    private String testWalletAddress;
    private ExchangeRequest testExchangeRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPointAmount = PointAmount.of(new BigDecimal("1000.00"));
        testWalletAddress = "0x1234567890abcdef";
        testExchangeRequest = new ExchangeRequest(testUserId, testPointAmount, testWalletAddress);
    }

    @Test
    @DisplayName("유효한 정보로 ExchangeRequest를 생성할 수 있다")
    void createExchangeRequestWithValidInfo() {
        // when
        ExchangeRequest exchangeRequest = new ExchangeRequest(testUserId, testPointAmount, testWalletAddress);

        // then
        assertThat(exchangeRequest).isNotNull();
        assertThat(exchangeRequest.getId()).isNotNull();
        assertThat(exchangeRequest.getUserId()).isEqualTo(testUserId);
        assertThat(exchangeRequest.getPointAmount()).isEqualTo(testPointAmount);
        assertThat(exchangeRequest.getWalletAddress()).isEqualTo(testWalletAddress);
        assertThat(exchangeRequest.getStatus()).isEqualTo(ExchangeStatus.REQUESTED);
        assertThat(exchangeRequest.getTransactionSignature()).isNull();
        assertThat(exchangeRequest.getCreatedAt()).isNotNull();
        assertThat(exchangeRequest.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("ExchangeRequest 생성 시 고유한 ID가 생성된다")
    void createExchangeRequestWithUniqueId() {
        // when
        ExchangeRequest exchangeRequest1 = new ExchangeRequest(testUserId, testPointAmount, testWalletAddress);
        ExchangeRequest exchangeRequest2 = new ExchangeRequest(testUserId, testPointAmount, testWalletAddress);

        // then
        assertThat(exchangeRequest1.getId()).isNotNull();
        assertThat(exchangeRequest2.getId()).isNotNull();
        assertThat(exchangeRequest1.getId()).isNotEqualTo(exchangeRequest2.getId());
    }

    @Test
    @DisplayName("ExchangeRequest의 기본 생성자가 작동한다")
    void createExchangeRequestWithDefaultConstructor() {
        // when
        ExchangeRequest exchangeRequest = new ExchangeRequest();

        // then
        assertThat(exchangeRequest).isNotNull();
        assertThat(exchangeRequest.getId()).isNull();
        assertThat(exchangeRequest.getUserId()).isNull();
        assertThat(exchangeRequest.getPointAmount()).isNull();
        assertThat(exchangeRequest.getWalletAddress()).isNull();
        assertThat(exchangeRequest.getStatus()).isNull();
        assertThat(exchangeRequest.getTransactionSignature()).isNull();
        assertThat(exchangeRequest.getCreatedAt()).isNull();
        assertThat(exchangeRequest.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("REQUESTED 상태에서 process를 호출할 수 있다")
    void processFromRequestedStatus() {
        // given
        ExchangeStatus originalStatus = testExchangeRequest.getStatus();

        // when
        testExchangeRequest.process();

        // then
        assertThat(originalStatus).isEqualTo(ExchangeStatus.REQUESTED);
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.PROCESSING);
    }

    @Test
    @DisplayName("REQUESTED가 아닌 상태에서 process를 호출하면 예외가 발생한다")
    void processFromNonRequestedStatus() {
        // given
        testExchangeRequest.process(); // PROCESSING 상태로 변경

        // when & then
        assertThatThrownBy(() -> testExchangeRequest.process())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("처리할 수 없는 상태입니다: " + ExchangeStatus.PROCESSING);
    }

    @Test
    @DisplayName("PROCESSING 상태에서 complete를 호출할 수 있다")
    void completeFromProcessingStatus() {
        // given
        testExchangeRequest.process(); // PROCESSING 상태로 변경
        String transactionSignature = "0xabcdef123456";

        // when
        testExchangeRequest.complete(transactionSignature);

        // then
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.COMPLETED);
        assertThat(testExchangeRequest.getTransactionSignature()).isEqualTo(transactionSignature);
        assertThat(testExchangeRequest.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("PROCESSING이 아닌 상태에서 complete를 호출하면 예외가 발생한다")
    void completeFromNonProcessingStatus() {
        // given
        String transactionSignature = "0xabcdef123456";

        // when & then
        assertThatThrownBy(() -> testExchangeRequest.complete(transactionSignature))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("완료할 수 없는 상태입니다: " + ExchangeStatus.REQUESTED);
    }

    @Test
    @DisplayName("COMPLETED 상태에서 cancel을 호출하면 예외가 발생한다")
    void cancelFromCompletedStatus() {
        // given
        testExchangeRequest.process();
        testExchangeRequest.complete("0xabcdef123456");

        // when & then
        assertThatThrownBy(() -> testExchangeRequest.cancel())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 완료된 교환은 취소할 수 없습니다");
    }

    @Test
    @DisplayName("REQUESTED 상태에서 cancel을 호출할 수 있다")
    void cancelFromRequestedStatus() {
        // when
        testExchangeRequest.cancel();

        // then
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.CANCELLED);
    }

    @Test
    @DisplayName("PROCESSING 상태에서 cancel을 호출할 수 있다")
    void cancelFromProcessingStatus() {
        // given
        testExchangeRequest.process();

        // when
        testExchangeRequest.cancel();

        // then
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.CANCELLED);
    }

    @Test
    @DisplayName("FAILED 상태에서 cancel을 호출할 수 있다")
    void cancelFromFailedStatus() {
        // given
        testExchangeRequest.fail();

        // when
        testExchangeRequest.cancel();

        // then
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.CANCELLED);
    }

    @Test
    @DisplayName("어떤 상태에서든 fail을 호출할 수 있다")
    void failFromAnyStatus() {
        // when
        testExchangeRequest.fail();

        // then
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.FAILED);
    }

    @Test
    @DisplayName("PROCESSING 상태에서 fail을 호출할 수 있다")
    void failFromProcessingStatus() {
        // given
        testExchangeRequest.process();

        // when
        testExchangeRequest.fail();

        // then
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.FAILED);
    }

    @Test
    @DisplayName("COMPLETED 상태에서 fail을 호출할 수 있다")
    void failFromCompletedStatus() {
        // given
        testExchangeRequest.process();
        testExchangeRequest.complete("0xabcdef123456");

        // when
        testExchangeRequest.fail();

        // then
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.FAILED);
    }

    @Test
    @DisplayName("CANCELLED 상태에서 fail을 호출할 수 있다")
    void failFromCancelledStatus() {
        // given
        testExchangeRequest.cancel();

        // when
        testExchangeRequest.fail();

        // then
        assertThat(testExchangeRequest.getStatus()).isEqualTo(ExchangeStatus.FAILED);
    }

    @Test
    @DisplayName("큰 소수점 자릿수의 포인트로 ExchangeRequest를 생성할 수 있다")
    void createExchangeRequestWithLargeDecimalPoints() {
        // given
        PointAmount largePointAmount = PointAmount.of(new BigDecimal("123456789.123456789"));

        // when
        ExchangeRequest exchangeRequest = new ExchangeRequest(testUserId, largePointAmount, testWalletAddress);

        // then
        assertThat(exchangeRequest.getPointAmount()).isEqualTo(largePointAmount);
    }

    @Test
    @DisplayName("긴 지갑 주소로 ExchangeRequest를 생성할 수 있다")
    void createExchangeRequestWithLongWalletAddress() {
        // given
        String longWalletAddress = "0x1234567890abcdef1234567890abcdef12345678";

        // when
        ExchangeRequest exchangeRequest = new ExchangeRequest(testUserId, testPointAmount, longWalletAddress);

        // then
        assertThat(exchangeRequest.getWalletAddress()).isEqualTo(longWalletAddress);
    }

    @Test
    @DisplayName("ExchangeRequest의 createdAt이 현재 시간과 가깝다")
    void exchangeRequestCreatedAtIsCloseToCurrentTime() {
        // when
        ExchangeRequest exchangeRequest = new ExchangeRequest(testUserId, testPointAmount, testWalletAddress);

        // then
        LocalDateTime now = LocalDateTime.now();
        assertThat(exchangeRequest.getCreatedAt()).isBeforeOrEqualTo(now);
        assertThat(exchangeRequest.getCreatedAt()).isAfter(now.minusSeconds(1));
    }

    @Test
    @DisplayName("ExchangeRequest의 completedAt이 완료 시점에 설정된다")
    void exchangeRequestCompletedAtIsSetOnCompletion() {
        // given
        testExchangeRequest.process();

        // when
        testExchangeRequest.complete("0xabcdef123456");

        // then
        LocalDateTime now = LocalDateTime.now();
        assertThat(testExchangeRequest.getCompletedAt()).isBeforeOrEqualTo(now);
        assertThat(testExchangeRequest.getCompletedAt()).isAfter(now.minusSeconds(1));
    }
} 