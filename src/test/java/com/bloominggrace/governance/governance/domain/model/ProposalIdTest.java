package com.bloominggrace.governance.governance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProposalId 테스트")
class ProposalIdTest {

    @Test
    @DisplayName("기본 생성자로 ProposalId를 생성할 수 있다")
    void createProposalIdWithDefaultConstructor() {
        // when
        ProposalId proposalId = new ProposalId();

        // then
        assertThat(proposalId).isNotNull();
        assertThat(proposalId.getValue()).isNotNull();
    }

    @Test
    @DisplayName("UUID로 ProposalId를 생성할 수 있다")
    void createProposalIdWithUUID() {
        // given
        UUID uuid = UUID.randomUUID();

        // when
        ProposalId proposalId = new ProposalId(uuid);

        // then
        assertThat(proposalId).isNotNull();
        assertThat(proposalId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("문자열로 ProposalId를 생성할 수 있다")
    void createProposalIdWithString() {
        // given
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        // when
        ProposalId proposalId = new ProposalId(uuidString);

        // then
        assertThat(proposalId).isNotNull();
        assertThat(proposalId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("기본 생성자로 생성된 ProposalId들이 서로 다르다")
    void defaultConstructorCreatesUniqueIds() {
        // when
        ProposalId id1 = new ProposalId();
        ProposalId id2 = new ProposalId();

        // then
        assertThat(id1.getValue()).isNotEqualTo(id2.getValue());
    }

    @Test
    @DisplayName("동일한 UUID로 생성된 ProposalId들이 같다")
    void proposalIdEqualityWithSameUUID() {
        // given
        UUID uuid = UUID.randomUUID();
        ProposalId id1 = new ProposalId(uuid);
        ProposalId id2 = new ProposalId(uuid);

        // when & then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("다른 UUID로 생성된 ProposalId들이 다르다")
    void proposalIdInequalityWithDifferentUUID() {
        // given
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        ProposalId id1 = new ProposalId(uuid1);
        ProposalId id2 = new ProposalId(uuid2);

        // when & then
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.hashCode()).isNotEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("ProposalId의 toString이 올바르게 작동한다")
    void proposalIdToString() {
        // given
        UUID uuid = UUID.randomUUID();
        ProposalId proposalId = new ProposalId(uuid);

        // when
        String toString = proposalId.toString();

        // then
        assertThat(toString).isNotNull();
        assertThat(toString).contains(uuid.toString());
    }

    @Test
    @DisplayName("문자열과 UUID 생성자가 동일한 결과를 만든다")
    void stringAndUUIDConstructorEquivalence() {
        // given
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        // when
        ProposalId idFromUUID = new ProposalId(uuid);
        ProposalId idFromString = new ProposalId(uuidString);

        // then
        assertThat(idFromUUID).isEqualTo(idFromString);
        assertThat(idFromUUID.getValue()).isEqualTo(idFromString.getValue());
    }

    @Test
    @DisplayName("null UUID로 ProposalId를 생성할 수 있다")
    void createProposalIdWithNullUUID() {
        // when
        ProposalId proposalId = new ProposalId((UUID) null);

        // then
        assertThat(proposalId).isNotNull();
        assertThat(proposalId.getValue()).isNull();
    }

    @Test
    @DisplayName("ProposalId의 equals가 null과 다른 타입을 올바르게 처리한다")
    void proposalIdEqualsWithNullAndDifferentType() {
        // given
        ProposalId proposalId = new ProposalId();

        // when & then
        assertThat(proposalId).isNotEqualTo(null);
        assertThat(proposalId).isNotEqualTo("string");
        assertThat(proposalId).isNotEqualTo(new Object());
    }
} 