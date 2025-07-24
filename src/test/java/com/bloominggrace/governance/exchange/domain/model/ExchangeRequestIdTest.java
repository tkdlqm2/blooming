package com.bloominggrace.governance.exchange.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExchangeRequestId 테스트")
class ExchangeRequestIdTest {

    @Test
    @DisplayName("UUID로 ExchangeRequestId를 생성할 수 있다")
    void createExchangeRequestIdWithUUID() {
        // given
        UUID uuid = UUID.randomUUID();

        // when
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId(uuid);

        // then
        assertThat(exchangeRequestId).isNotNull();
        assertThat(exchangeRequestId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("ExchangeRequestId.of 팩토리 메서드로 생성할 수 있다")
    void createExchangeRequestIdWithOf() {
        // given
        UUID uuid = UUID.randomUUID();

        // when
        ExchangeRequestId exchangeRequestId = ExchangeRequestId.of(uuid);

        // then
        assertThat(exchangeRequestId).isNotNull();
        assertThat(exchangeRequestId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("ExchangeRequestId.generate 팩토리 메서드로 생성할 수 있다")
    void createExchangeRequestIdWithGenerate() {
        // when
        ExchangeRequestId exchangeRequestId = ExchangeRequestId.generate();

        // then
        assertThat(exchangeRequestId).isNotNull();
        assertThat(exchangeRequestId.getValue()).isNotNull();
    }

    @Test
    @DisplayName("ExchangeRequestId.generate로 생성된 ID들이 서로 다르다")
    void generateCreatesUniqueIds() {
        // when
        ExchangeRequestId id1 = ExchangeRequestId.generate();
        ExchangeRequestId id2 = ExchangeRequestId.generate();

        // then
        assertThat(id1.getValue()).isNotEqualTo(id2.getValue());
    }

    @Test
    @DisplayName("ExchangeRequestId의 기본 생성자가 작동한다")
    void createExchangeRequestIdWithDefaultConstructor() {
        // when
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId();

        // then
        assertThat(exchangeRequestId).isNotNull();
        assertThat(exchangeRequestId.getValue()).isNull();
    }

    @Test
    @DisplayName("ExchangeRequestId의 setter와 getter가 올바르게 작동한다")
    void exchangeRequestIdSetterAndGetter() {
        // given
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId();
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        // when
        exchangeRequestId.setValue(uuid1);
        UUID result1 = exchangeRequestId.getValue();
        exchangeRequestId.setValue(uuid2);
        UUID result2 = exchangeRequestId.getValue();

        // then
        assertThat(result1).isEqualTo(uuid1);
        assertThat(result2).isEqualTo(uuid2);
    }

    @Test
    @DisplayName("ExchangeRequestId의 equals가 올바르게 작동한다")
    void exchangeRequestIdEquals() {
        // given
        UUID uuid = UUID.randomUUID();
        ExchangeRequestId id1 = new ExchangeRequestId(uuid);
        ExchangeRequestId id2 = new ExchangeRequestId(uuid);
        ExchangeRequestId id3 = new ExchangeRequestId(UUID.randomUUID());

        // when & then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
    }

    @Test
    @DisplayName("ExchangeRequestId의 hashCode가 올바르게 작동한다")
    void exchangeRequestIdHashCode() {
        // given
        UUID uuid = UUID.randomUUID();
        ExchangeRequestId id1 = new ExchangeRequestId(uuid);
        ExchangeRequestId id2 = new ExchangeRequestId(uuid);

        // when & then
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("null UUID로 ExchangeRequestId를 생성할 수 있다")
    void createExchangeRequestIdWithNullUUID() {
        // when
        ExchangeRequestId exchangeRequestId = new ExchangeRequestId(null);

        // then
        assertThat(exchangeRequestId).isNotNull();
        assertThat(exchangeRequestId.getValue()).isNull();
    }

    @Test
    @DisplayName("ExchangeRequestId.of으로 null UUID를 처리할 수 있다")
    void createExchangeRequestIdWithOfNull() {
        // when
        ExchangeRequestId exchangeRequestId = ExchangeRequestId.of(null);

        // then
        assertThat(exchangeRequestId).isNotNull();
        assertThat(exchangeRequestId.getValue()).isNull();
    }

    @Test
    @DisplayName("동일한 UUID로 생성된 ExchangeRequestId들이 같다")
    void exchangeRequestIdEqualityWithSameUUID() {
        // given
        UUID uuid = UUID.randomUUID();
        ExchangeRequestId id1 = new ExchangeRequestId(uuid);
        ExchangeRequestId id2 = ExchangeRequestId.of(uuid);

        // when & then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
} 