package com.bloominggrace.governance.governance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VoteType enum 테스트")
class VoteTypeTest {

    @Test
    @DisplayName("VoteType enum의 모든 값이 올바르게 정의되어 있다")
    void voteTypeEnumValues() {
        // when
        VoteType[] types = VoteType.values();

        // then
        assertThat(types).hasSize(3);
        assertThat(types).contains(VoteType.YES);
        assertThat(types).contains(VoteType.NO);
        assertThat(types).contains(VoteType.ABSTAIN);
    }

    @Test
    @DisplayName("VoteType enum의 각 값이 올바른 순서로 정의되어 있다")
    void voteTypeEnumOrder() {
        // when
        VoteType[] types = VoteType.values();

        // then
        assertThat(types[0]).isEqualTo(VoteType.YES);
        assertThat(types[1]).isEqualTo(VoteType.NO);
        assertThat(types[2]).isEqualTo(VoteType.ABSTAIN);
    }

    @Test
    @DisplayName("VoteType enum의 각 값이 올바른 이름을 가진다")
    void voteTypeEnumNames() {
        // when & then
        assertThat(VoteType.YES.name()).isEqualTo("YES");
        assertThat(VoteType.NO.name()).isEqualTo("NO");
        assertThat(VoteType.ABSTAIN.name()).isEqualTo("ABSTAIN");
    }

    @Test
    @DisplayName("VoteType enum의 valueOf 메서드가 올바르게 작동한다")
    void voteTypeValueOf() {
        // when & then
        assertThat(VoteType.valueOf("YES")).isEqualTo(VoteType.YES);
        assertThat(VoteType.valueOf("NO")).isEqualTo(VoteType.NO);
        assertThat(VoteType.valueOf("ABSTAIN")).isEqualTo(VoteType.ABSTAIN);
    }

    @Test
    @DisplayName("VoteType enum의 각 값이 고유하다")
    void voteTypeEnumUniqueness() {
        // when
        VoteType yes = VoteType.YES;
        VoteType no = VoteType.NO;
        VoteType abstain = VoteType.ABSTAIN;

        // then
        assertThat(yes).isNotEqualTo(no);
        assertThat(yes).isNotEqualTo(abstain);
        assertThat(no).isNotEqualTo(abstain);
    }

    @Test
    @DisplayName("VoteType enum의 toString이 올바르게 작동한다")
    void voteTypeToString() {
        // when & then
        assertThat(VoteType.YES.toString()).isEqualTo("YES");
        assertThat(VoteType.NO.toString()).isEqualTo("NO");
        assertThat(VoteType.ABSTAIN.toString()).isEqualTo("ABSTAIN");
    }

    @Test
    @DisplayName("VoteType enum의 ordinal이 올바르게 작동한다")
    void voteTypeOrdinal() {
        // when & then
        assertThat(VoteType.YES.ordinal()).isEqualTo(0);
        assertThat(VoteType.NO.ordinal()).isEqualTo(1);
        assertThat(VoteType.ABSTAIN.ordinal()).isEqualTo(2);
    }

    @Test
    @DisplayName("VoteType enum의 description이 올바르게 설정되어 있다")
    void voteTypeDescription() {
        // when & then
        assertThat(VoteType.YES.getDescription()).isEqualTo("찬성");
        assertThat(VoteType.NO.getDescription()).isEqualTo("반대");
        assertThat(VoteType.ABSTAIN.getDescription()).isEqualTo("기권");
    }

    @Test
    @DisplayName("찬성 투표 타입을 확인할 수 있다")
    void positiveVoteType() {
        // when & then
        assertThat(VoteType.YES.name()).contains("YES");
        assertThat(VoteType.YES.getDescription()).contains("찬성");
    }

    @Test
    @DisplayName("반대 투표 타입을 확인할 수 있다")
    void negativeVoteType() {
        // when & then
        assertThat(VoteType.NO.name()).contains("NO");
        assertThat(VoteType.NO.getDescription()).contains("반대");
    }

    @Test
    @DisplayName("기권 투표 타입을 확인할 수 있다")
    void abstainVoteType() {
        // when & then
        assertThat(VoteType.ABSTAIN.name()).contains("ABSTAIN");
        assertThat(VoteType.ABSTAIN.getDescription()).contains("기권");
    }
} 