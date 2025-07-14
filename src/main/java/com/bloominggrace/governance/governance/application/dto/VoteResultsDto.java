package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.VoteResults;

import java.math.BigDecimal;

public class VoteResultsDto {
    private final long totalVotes;
    private final long yesVotes;
    private final long noVotes;
    private final long abstainVotes;
    private final BigDecimal yesPercentage;
    private final BigDecimal noPercentage;
    private final BigDecimal abstainPercentage;
    private final boolean isPassed;
    private final boolean isRejected;

    public VoteResultsDto(
            long totalVotes,
            long yesVotes,
            long noVotes,
            long abstainVotes,
            BigDecimal yesPercentage,
            BigDecimal noPercentage,
            BigDecimal abstainPercentage,
            boolean isPassed,
            boolean isRejected) {
        this.totalVotes = totalVotes;
        this.yesVotes = yesVotes;
        this.noVotes = noVotes;
        this.abstainVotes = abstainVotes;
        this.yesPercentage = yesPercentage;
        this.noPercentage = noPercentage;
        this.abstainPercentage = abstainPercentage;
        this.isPassed = isPassed;
        this.isRejected = isRejected;
    }

    public static VoteResultsDto from(VoteResults voteResults) {
        return new VoteResultsDto(
            voteResults.getTotalVotes(),
            voteResults.getYesVotes(),
            voteResults.getNoVotes(),
            voteResults.getAbstainVotes(),
            voteResults.getYesPercentage(),
            voteResults.getNoPercentage(),
            voteResults.getAbstainPercentage(),
            voteResults.isPassed(),
            voteResults.isRejected()
        );
    }

    public long getTotalVotes() {
        return totalVotes;
    }

    public long getYesVotes() {
        return yesVotes;
    }

    public long getNoVotes() {
        return noVotes;
    }

    public long getAbstainVotes() {
        return abstainVotes;
    }

    public BigDecimal getYesPercentage() {
        return yesPercentage;
    }

    public BigDecimal getNoPercentage() {
        return noPercentage;
    }

    public BigDecimal getAbstainPercentage() {
        return abstainPercentage;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public boolean isRejected() {
        return isRejected;
    }
} 