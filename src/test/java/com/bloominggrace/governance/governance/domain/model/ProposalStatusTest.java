package com.bloominggrace.governance.governance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProposalStatus enum 테스트")
class ProposalStatusTest {

    @Test
    @DisplayName("ProposalStatus enum의 모든 값이 올바르게 정의되어 있다")
    void proposalStatusEnumValues() {
        // when
        ProposalStatus[] statuses = ProposalStatus.values();

        // then
        assertThat(statuses).hasSize(6);
        assertThat(statuses).contains(ProposalStatus.DRAFT);
        assertThat(statuses).contains(ProposalStatus.ACTIVE);
        assertThat(statuses).contains(ProposalStatus.VOTING);
        assertThat(statuses).contains(ProposalStatus.PASSED);
        assertThat(statuses).contains(ProposalStatus.REJECTED);
        assertThat(statuses).contains(ProposalStatus.EXPIRED);
    }

    @Test
    @DisplayName("ProposalStatus enum의 각 값이 올바른 순서로 정의되어 있다")
    void proposalStatusEnumOrder() {
        // when
        ProposalStatus[] statuses = ProposalStatus.values();

        // then
        assertThat(statuses[0]).isEqualTo(ProposalStatus.DRAFT);
        assertThat(statuses[1]).isEqualTo(ProposalStatus.ACTIVE);
        assertThat(statuses[2]).isEqualTo(ProposalStatus.VOTING);
        assertThat(statuses[3]).isEqualTo(ProposalStatus.PASSED);
        assertThat(statuses[4]).isEqualTo(ProposalStatus.REJECTED);
        assertThat(statuses[5]).isEqualTo(ProposalStatus.EXPIRED);
    }

    @Test
    @DisplayName("ProposalStatus enum의 각 값이 올바른 이름을 가진다")
    void proposalStatusEnumNames() {
        // when & then
        assertThat(ProposalStatus.DRAFT.name()).isEqualTo("DRAFT");
        assertThat(ProposalStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        assertThat(ProposalStatus.VOTING.name()).isEqualTo("VOTING");
        assertThat(ProposalStatus.PASSED.name()).isEqualTo("PASSED");
        assertThat(ProposalStatus.REJECTED.name()).isEqualTo("REJECTED");
        assertThat(ProposalStatus.EXPIRED.name()).isEqualTo("EXPIRED");
    }

    @Test
    @DisplayName("ProposalStatus enum의 valueOf 메서드가 올바르게 작동한다")
    void proposalStatusValueOf() {
        // when & then
        assertThat(ProposalStatus.valueOf("DRAFT")).isEqualTo(ProposalStatus.DRAFT);
        assertThat(ProposalStatus.valueOf("ACTIVE")).isEqualTo(ProposalStatus.ACTIVE);
        assertThat(ProposalStatus.valueOf("VOTING")).isEqualTo(ProposalStatus.VOTING);
        assertThat(ProposalStatus.valueOf("PASSED")).isEqualTo(ProposalStatus.PASSED);
        assertThat(ProposalStatus.valueOf("REJECTED")).isEqualTo(ProposalStatus.REJECTED);
        assertThat(ProposalStatus.valueOf("EXPIRED")).isEqualTo(ProposalStatus.EXPIRED);
    }

    @Test
    @DisplayName("ProposalStatus enum의 각 값이 고유하다")
    void proposalStatusEnumUniqueness() {
        // when
        ProposalStatus draft = ProposalStatus.DRAFT;
        ProposalStatus active = ProposalStatus.ACTIVE;
        ProposalStatus voting = ProposalStatus.VOTING;
        ProposalStatus passed = ProposalStatus.PASSED;
        ProposalStatus rejected = ProposalStatus.REJECTED;
        ProposalStatus expired = ProposalStatus.EXPIRED;

        // then
        assertThat(draft).isNotEqualTo(active);
        assertThat(draft).isNotEqualTo(voting);
        assertThat(draft).isNotEqualTo(passed);
        assertThat(draft).isNotEqualTo(rejected);
        assertThat(draft).isNotEqualTo(expired);
        assertThat(active).isNotEqualTo(voting);
        assertThat(active).isNotEqualTo(passed);
        assertThat(active).isNotEqualTo(rejected);
        assertThat(active).isNotEqualTo(expired);
        assertThat(voting).isNotEqualTo(passed);
        assertThat(voting).isNotEqualTo(rejected);
        assertThat(voting).isNotEqualTo(expired);
        assertThat(passed).isNotEqualTo(rejected);
        assertThat(passed).isNotEqualTo(expired);
        assertThat(rejected).isNotEqualTo(expired);
    }

    @Test
    @DisplayName("ProposalStatus enum의 toString이 올바르게 작동한다")
    void proposalStatusToString() {
        // when & then
        assertThat(ProposalStatus.DRAFT.toString()).isEqualTo("DRAFT");
        assertThat(ProposalStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
        assertThat(ProposalStatus.VOTING.toString()).isEqualTo("VOTING");
        assertThat(ProposalStatus.PASSED.toString()).isEqualTo("PASSED");
        assertThat(ProposalStatus.REJECTED.toString()).isEqualTo("REJECTED");
        assertThat(ProposalStatus.EXPIRED.toString()).isEqualTo("EXPIRED");
    }

    @Test
    @DisplayName("ProposalStatus enum의 ordinal이 올바르게 작동한다")
    void proposalStatusOrdinal() {
        // when & then
        assertThat(ProposalStatus.DRAFT.ordinal()).isEqualTo(0);
        assertThat(ProposalStatus.ACTIVE.ordinal()).isEqualTo(1);
        assertThat(ProposalStatus.VOTING.ordinal()).isEqualTo(2);
        assertThat(ProposalStatus.PASSED.ordinal()).isEqualTo(3);
        assertThat(ProposalStatus.REJECTED.ordinal()).isEqualTo(4);
        assertThat(ProposalStatus.EXPIRED.ordinal()).isEqualTo(5);
    }

    @Test
    @DisplayName("ProposalStatus enum의 description이 올바르게 설정되어 있다")
    void proposalStatusDescription() {
        // when & then
        assertThat(ProposalStatus.DRAFT.getDescription()).isEqualTo("초안");
        assertThat(ProposalStatus.ACTIVE.getDescription()).isEqualTo("활성");
        assertThat(ProposalStatus.VOTING.getDescription()).isEqualTo("투표 중");
        assertThat(ProposalStatus.PASSED.getDescription()).isEqualTo("통과");
        assertThat(ProposalStatus.REJECTED.getDescription()).isEqualTo("거부");
        assertThat(ProposalStatus.EXPIRED.getDescription()).isEqualTo("만료");
    }

    @Test
    @DisplayName("제안 초기 상태를 확인할 수 있다")
    void proposalInitialStatuses() {
        // when & then
        assertThat(ProposalStatus.DRAFT.name()).contains("DRAFT");
        assertThat(ProposalStatus.ACTIVE.name()).contains("ACTIVE");
    }

    @Test
    @DisplayName("제안 진행 상태를 확인할 수 있다")
    void proposalProgressStatuses() {
        // when & then
        assertThat(ProposalStatus.VOTING.name()).contains("VOTING");
    }

    @Test
    @DisplayName("제안 완료 상태를 확인할 수 있다")
    void proposalCompletionStatuses() {
        // when & then
        assertThat(ProposalStatus.PASSED.name()).contains("PASSED");
        assertThat(ProposalStatus.REJECTED.name()).contains("REJECTED");
        assertThat(ProposalStatus.EXPIRED.name()).contains("EXPIRED");
    }
} 