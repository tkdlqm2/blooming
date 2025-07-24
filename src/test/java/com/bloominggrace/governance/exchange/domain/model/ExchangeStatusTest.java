package com.bloominggrace.governance.exchange.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExchangeStatus enum 테스트")
class ExchangeStatusTest {

    @Test
    @DisplayName("ExchangeStatus enum의 모든 값이 올바르게 정의되어 있다")
    void exchangeStatusEnumValues() {
        // when
        ExchangeStatus[] statuses = ExchangeStatus.values();

        // then
        assertThat(statuses).hasSize(5);
        assertThat(statuses).contains(ExchangeStatus.REQUESTED);
        assertThat(statuses).contains(ExchangeStatus.PROCESSING);
        assertThat(statuses).contains(ExchangeStatus.COMPLETED);
        assertThat(statuses).contains(ExchangeStatus.CANCELLED);
        assertThat(statuses).contains(ExchangeStatus.FAILED);
    }

    @Test
    @DisplayName("ExchangeStatus enum의 각 값이 올바른 순서로 정의되어 있다")
    void exchangeStatusEnumOrder() {
        // when
        ExchangeStatus[] statuses = ExchangeStatus.values();

        // then
        assertThat(statuses[0]).isEqualTo(ExchangeStatus.REQUESTED);
        assertThat(statuses[1]).isEqualTo(ExchangeStatus.PROCESSING);
        assertThat(statuses[2]).isEqualTo(ExchangeStatus.COMPLETED);
        assertThat(statuses[3]).isEqualTo(ExchangeStatus.CANCELLED);
        assertThat(statuses[4]).isEqualTo(ExchangeStatus.FAILED);
    }

    @Test
    @DisplayName("ExchangeStatus enum의 각 값이 올바른 이름을 가진다")
    void exchangeStatusEnumNames() {
        // when & then
        assertThat(ExchangeStatus.REQUESTED.name()).isEqualTo("REQUESTED");
        assertThat(ExchangeStatus.PROCESSING.name()).isEqualTo("PROCESSING");
        assertThat(ExchangeStatus.COMPLETED.name()).isEqualTo("COMPLETED");
        assertThat(ExchangeStatus.CANCELLED.name()).isEqualTo("CANCELLED");
        assertThat(ExchangeStatus.FAILED.name()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("ExchangeStatus enum의 valueOf 메서드가 올바르게 작동한다")
    void exchangeStatusValueOf() {
        // when & then
        assertThat(ExchangeStatus.valueOf("REQUESTED")).isEqualTo(ExchangeStatus.REQUESTED);
        assertThat(ExchangeStatus.valueOf("PROCESSING")).isEqualTo(ExchangeStatus.PROCESSING);
        assertThat(ExchangeStatus.valueOf("COMPLETED")).isEqualTo(ExchangeStatus.COMPLETED);
        assertThat(ExchangeStatus.valueOf("CANCELLED")).isEqualTo(ExchangeStatus.CANCELLED);
        assertThat(ExchangeStatus.valueOf("FAILED")).isEqualTo(ExchangeStatus.FAILED);
    }

    @Test
    @DisplayName("ExchangeStatus enum의 각 값이 고유하다")
    void exchangeStatusEnumUniqueness() {
        // when
        ExchangeStatus requested = ExchangeStatus.REQUESTED;
        ExchangeStatus processing = ExchangeStatus.PROCESSING;
        ExchangeStatus completed = ExchangeStatus.COMPLETED;
        ExchangeStatus cancelled = ExchangeStatus.CANCELLED;
        ExchangeStatus failed = ExchangeStatus.FAILED;

        // then
        assertThat(requested).isNotEqualTo(processing);
        assertThat(requested).isNotEqualTo(completed);
        assertThat(requested).isNotEqualTo(cancelled);
        assertThat(requested).isNotEqualTo(failed);
        assertThat(processing).isNotEqualTo(completed);
        assertThat(processing).isNotEqualTo(cancelled);
        assertThat(processing).isNotEqualTo(failed);
        assertThat(completed).isNotEqualTo(cancelled);
        assertThat(completed).isNotEqualTo(failed);
        assertThat(cancelled).isNotEqualTo(failed);
    }

    @Test
    @DisplayName("ExchangeStatus enum의 toString이 올바르게 작동한다")
    void exchangeStatusToString() {
        // when & then
        assertThat(ExchangeStatus.REQUESTED.toString()).isEqualTo("REQUESTED");
        assertThat(ExchangeStatus.PROCESSING.toString()).isEqualTo("PROCESSING");
        assertThat(ExchangeStatus.COMPLETED.toString()).isEqualTo("COMPLETED");
        assertThat(ExchangeStatus.CANCELLED.toString()).isEqualTo("CANCELLED");
        assertThat(ExchangeStatus.FAILED.toString()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("ExchangeStatus enum의 ordinal이 올바르게 작동한다")
    void exchangeStatusOrdinal() {
        // when & then
        assertThat(ExchangeStatus.REQUESTED.ordinal()).isEqualTo(0);
        assertThat(ExchangeStatus.PROCESSING.ordinal()).isEqualTo(1);
        assertThat(ExchangeStatus.COMPLETED.ordinal()).isEqualTo(2);
        assertThat(ExchangeStatus.CANCELLED.ordinal()).isEqualTo(3);
        assertThat(ExchangeStatus.FAILED.ordinal()).isEqualTo(4);
    }

    @Test
    @DisplayName("교환 요청 관련 상태를 확인할 수 있다")
    void exchangeRequestRelatedStatuses() {
        // when & then
        assertThat(ExchangeStatus.REQUESTED.name()).contains("REQUESTED");
        assertThat(ExchangeStatus.PROCESSING.name()).contains("PROCESSING");
    }

    @Test
    @DisplayName("교환 완료 관련 상태를 확인할 수 있다")
    void exchangeCompletionRelatedStatuses() {
        // when & then
        assertThat(ExchangeStatus.COMPLETED.name()).contains("COMPLETED");
        assertThat(ExchangeStatus.CANCELLED.name()).contains("CANCELLED");
        assertThat(ExchangeStatus.FAILED.name()).contains("FAILED");
    }
} 