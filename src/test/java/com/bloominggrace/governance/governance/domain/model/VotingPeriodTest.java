package com.bloominggrace.governance.governance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VotingPeriod 테스트")
class VotingPeriodTest {

    @Test
    @DisplayName("유효한 시작일과 종료일로 VotingPeriod를 생성할 수 있다")
    void createVotingPeriodWithValidDates() {
        // given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);

        // when
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // then
        assertThat(votingPeriod).isNotNull();
        assertThat(votingPeriod.getStartDate()).isEqualTo(startDate);
        assertThat(votingPeriod.getEndDate()).isEqualTo(endDate);
    }

    @Test
    @DisplayName("null 시작일로 VotingPeriod를 생성할 수 없다")
    void createVotingPeriodWithNullStartDate() {
        // given
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);

        // when & then
        assertThatThrownBy(() -> new VotingPeriod(null, endDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start date and end date cannot be null");
    }

    @Test
    @DisplayName("null 종료일로 VotingPeriod를 생성할 수 없다")
    void createVotingPeriodWithNullEndDate() {
        // given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);

        // when & then
        assertThatThrownBy(() -> new VotingPeriod(startDate, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start date and end date cannot be null");
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 VotingPeriod를 생성할 수 없다")
    void createVotingPeriodWithStartDateAfterEndDate() {
        // given
        LocalDateTime startDate = LocalDateTime.now().plusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // when & then
        assertThatThrownBy(() -> new VotingPeriod(startDate, endDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start date cannot be after end date");
    }

    @Test
    @DisplayName("시작일과 종료일이 같으면 VotingPeriod를 생성할 수 있다")
    void createVotingPeriodWithSameStartAndEndDate() {
        // given
        LocalDateTime date = LocalDateTime.now().plusDays(1);

        // when
        VotingPeriod votingPeriod = new VotingPeriod(date, date);

        // then
        assertThat(votingPeriod).isNotNull();
        assertThat(votingPeriod.getStartDate()).isEqualTo(date);
        assertThat(votingPeriod.getEndDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("과거 날짜로 VotingPeriod를 생성할 수 있다")
    void createVotingPeriodWithPastDates() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        // when
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // then
        assertThat(votingPeriod).isNotNull();
        assertThat(votingPeriod.getStartDate()).isEqualTo(startDate);
        assertThat(votingPeriod.getEndDate()).isEqualTo(endDate);
    }

    @Test
    @DisplayName("투표 기간이 활성 상태인지 확인할 수 있다")
    void isVotingActive() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // when
        boolean isActive = votingPeriod.isVotingActive();

        // then
        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("투표 기간이 종료되었는지 확인할 수 있다")
    void isVotingEnded() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // when
        boolean isEnded = votingPeriod.isVotingEnded();

        // then
        assertThat(isEnded).isTrue();
    }

    @Test
    @DisplayName("투표가 시작되지 않았는지 확인할 수 있다")
    void isVotingNotStarted() {
        // given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // when
        boolean isNotStarted = votingPeriod.isVotingNotStarted();

        // then
        assertThat(isNotStarted).isTrue();
    }

    @Test
    @DisplayName("남은 일수를 계산할 수 있다")
    void getRemainingDays() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // when
        long remainingDays = votingPeriod.getRemainingDays();

        // then
        assertThat(remainingDays).isGreaterThanOrEqualTo(4);
        assertThat(remainingDays).isLessThanOrEqualTo(6);
    }

    @Test
    @DisplayName("투표가 종료된 경우 남은 일수는 0이다")
    void getRemainingDaysWhenVotingEnded() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // when
        long remainingDays = votingPeriod.getRemainingDays();

        // then
        assertThat(remainingDays).isEqualTo(0);
    }

    @Test
    @DisplayName("VotingPeriod의 equals가 올바르게 작동한다")
    void votingPeriodEquality() {
        // given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        VotingPeriod period1 = new VotingPeriod(startDate, endDate);
        VotingPeriod period2 = new VotingPeriod(startDate, endDate);

        // when & then
        assertThat(period1).isEqualTo(period2);
        assertThat(period1.hashCode()).isEqualTo(period2.hashCode());
    }

    @Test
    @DisplayName("다른 날짜를 가진 VotingPeriod는 같지 않다")
    void votingPeriodInequality() {
        // given
        LocalDateTime startDate1 = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate1 = LocalDateTime.now().plusDays(7);
        LocalDateTime startDate2 = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate2 = LocalDateTime.now().plusDays(8);
        VotingPeriod period1 = new VotingPeriod(startDate1, endDate1);
        VotingPeriod period2 = new VotingPeriod(startDate2, endDate2);

        // when & then
        assertThat(period1).isNotEqualTo(period2);
        assertThat(period1.hashCode()).isNotEqualTo(period2.hashCode());
    }

    @Test
    @DisplayName("VotingPeriod의 toString이 올바르게 작동한다")
    void votingPeriodToString() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 7, 18, 0);
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // when
        String toString = votingPeriod.toString();

        // then
        assertThat(toString).contains("VotingPeriod");
        assertThat(toString).contains(startDate.toString());
        assertThat(toString).contains(endDate.toString());
    }

    @Test
    @DisplayName("기본 생성자로 VotingPeriod를 생성할 수 있다")
    void createVotingPeriodWithDefaultConstructor() {
        // when
        VotingPeriod votingPeriod = new VotingPeriod();

        // then
        assertThat(votingPeriod).isNotNull();
        // 기본 생성자는 JPA용이므로 필드들이 null일 수 있음
    }

    @Test
    @DisplayName("VotingPeriod의 equals가 null과 다른 타입을 올바르게 처리한다")
    void votingPeriodEqualsWithNullAndDifferentType() {
        // given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        VotingPeriod votingPeriod = new VotingPeriod(startDate, endDate);

        // when & then
        assertThat(votingPeriod).isNotEqualTo(null);
        assertThat(votingPeriod).isNotEqualTo("string");
        assertThat(votingPeriod).isNotEqualTo(new Object());
    }
} 