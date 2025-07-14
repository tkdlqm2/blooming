package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.VotingPeriod;

import java.time.LocalDateTime;

public class VotingPeriodDto {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final boolean isVotingActive;
    private final boolean isVotingEnded;
    private final boolean isVotingNotStarted;
    private final long remainingDays;

    public VotingPeriodDto(
            LocalDateTime startDate,
            LocalDateTime endDate,
            boolean isVotingActive,
            boolean isVotingEnded,
            boolean isVotingNotStarted,
            long remainingDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isVotingActive = isVotingActive;
        this.isVotingEnded = isVotingEnded;
        this.isVotingNotStarted = isVotingNotStarted;
        this.remainingDays = remainingDays;
    }

    public static VotingPeriodDto from(VotingPeriod votingPeriod) {
        return new VotingPeriodDto(
            votingPeriod.getStartDate(),
            votingPeriod.getEndDate(),
            votingPeriod.isVotingActive(),
            votingPeriod.isVotingEnded(),
            votingPeriod.isVotingNotStarted(),
            votingPeriod.getRemainingDays()
        );
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public boolean isVotingActive() {
        return isVotingActive;
    }

    public boolean isVotingEnded() {
        return isVotingEnded;
    }

    public boolean isVotingNotStarted() {
        return isVotingNotStarted;
    }

    public long getRemainingDays() {
        return remainingDays;
    }
} 