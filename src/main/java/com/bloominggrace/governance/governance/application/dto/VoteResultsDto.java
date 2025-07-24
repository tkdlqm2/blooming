package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.VoteResults;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
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

    public static VoteResultsDto from(VoteResults voteResults) {
        if (voteResults == null) {
            return null;
        }
        return VoteResultsDto.builder()
            .totalVotes(voteResults.getTotalVotes())
            .yesVotes(voteResults.getYesVotes())
            .noVotes(voteResults.getNoVotes())
            .abstainVotes(voteResults.getAbstainVotes())
            .yesPercentage(voteResults.getYesPercentage())
            .noPercentage(voteResults.getNoPercentage())
            .abstainPercentage(voteResults.getAbstainPercentage())
            .isPassed(voteResults.isPassed())
            .isRejected(voteResults.isRejected())
            .build();
    }
} 