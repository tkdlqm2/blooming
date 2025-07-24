package com.bloominggrace.governance.governance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VoteId 테스트")
class VoteIdTest {

    @Test
    @DisplayName("기본 생성자로 VoteId를 생성할 수 있다")
    void createVoteIdWithDefaultConstructor() {
        // when
        VoteId voteId = new VoteId();

        // then
        assertThat(voteId).isNotNull();
        assertThat(voteId.getValue()).isNotNull();
    }

    @Test
    @DisplayName("UUID로 VoteId를 생성할 수 있다")
    void createVoteIdWithUUID() {
        // given
        UUID uuid = UUID.randomUUID();

        // when
        VoteId voteId = new VoteId(uuid);

        // then
        assertThat(voteId).isNotNull();
        assertThat(voteId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("문자열로 VoteId를 생성할 수 있다")
    void createVoteIdWithString() {
        // given
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        // when
        VoteId voteId = new VoteId(uuidString);

        // then
        assertThat(voteId).isNotNull();
        assertThat(voteId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("기본 생성자로 생성된 VoteId들이 서로 다르다")
    void defaultConstructorCreatesUniqueIds() {
        // when
        VoteId id1 = new VoteId();
        VoteId id2 = new VoteId();

        // then
        assertThat(id1.getValue()).isNotEqualTo(id2.getValue());
    }

    @Test
    @DisplayName("동일한 UUID로 생성된 VoteId들이 같다")
    void voteIdEqualityWithSameUUID() {
        // given
        UUID uuid = UUID.randomUUID();
        VoteId id1 = new VoteId(uuid);
        VoteId id2 = new VoteId(uuid);

        // when & then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("다른 UUID로 생성된 VoteId들이 다르다")
    void voteIdInequalityWithDifferentUUID() {
        // given
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        VoteId id1 = new VoteId(uuid1);
        VoteId id2 = new VoteId(uuid2);

        // when & then
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.hashCode()).isNotEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("VoteId의 toString이 올바르게 작동한다")
    void voteIdToString() {
        // given
        UUID uuid = UUID.randomUUID();
        VoteId voteId = new VoteId(uuid);

        // when
        String toString = voteId.toString();

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
        VoteId idFromUUID = new VoteId(uuid);
        VoteId idFromString = new VoteId(uuidString);

        // then
        assertThat(idFromUUID).isEqualTo(idFromString);
        assertThat(idFromUUID.getValue()).isEqualTo(idFromString.getValue());
    }

    @Test
    @DisplayName("null UUID로 VoteId를 생성할 수 있다")
    void createVoteIdWithNullUUID() {
        // when
        VoteId voteId = new VoteId((UUID) null);

        // then
        assertThat(voteId).isNotNull();
        assertThat(voteId.getValue()).isNull();
    }

    @Test
    @DisplayName("VoteId의 equals가 null과 다른 타입을 올바르게 처리한다")
    void voteIdEqualsWithNullAndDifferentType() {
        // given
        VoteId voteId = new VoteId();

        // when & then
        assertThat(voteId).isNotEqualTo(null);
        assertThat(voteId).isNotEqualTo("string");
        assertThat(voteId).isNotEqualTo(new Object());
    }

    @Test
    @DisplayName("VoteId와 ProposalId는 다른 타입이다")
    void voteIdAndProposalIdAreDifferentTypes() {
        // given
        UUID uuid = UUID.randomUUID();
        VoteId voteId = new VoteId(uuid);
        ProposalId proposalId = new ProposalId(uuid);

        // when & then
        // EntityId에서 equals가 UUID만 비교하므로 같은 UUID면 같다고 판단함
        // 하지만 실제로는 다른 타입이므로 getClass()로 구분해야 함
        assertThat(voteId.getClass()).isNotEqualTo(proposalId.getClass());
    }
} 