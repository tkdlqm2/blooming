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
    
    public static VoteResultsDtoBuilder builder() {
        return new VoteResultsDtoBuilder();
    }
    
    public static class VoteResultsDtoBuilder {
        private long totalVotes;
        private long yesVotes;
        private long noVotes;
        private long abstainVotes;
        private BigDecimal yesPercentage;
        private BigDecimal noPercentage;
        private BigDecimal abstainPercentage;
        private boolean isPassed;
        private boolean isRejected;
        
        public VoteResultsDtoBuilder totalVotes(long totalVotes) {
            this.totalVotes = totalVotes;
            return this;
        }
        
        public VoteResultsDtoBuilder yesVotes(long yesVotes) {
            this.yesVotes = yesVotes;
            return this;
        }
        
        public VoteResultsDtoBuilder noVotes(long noVotes) {
            this.noVotes = noVotes;
            return this;
        }
        
        public VoteResultsDtoBuilder abstainVotes(long abstainVotes) {
            this.abstainVotes = abstainVotes;
            return this;
        }
        
        public VoteResultsDtoBuilder yesPercentage(BigDecimal yesPercentage) {
            this.yesPercentage = yesPercentage;
            return this;
        }
        
        public VoteResultsDtoBuilder noPercentage(BigDecimal noPercentage) {
            this.noPercentage = noPercentage;
            return this;
        }
        
        public VoteResultsDtoBuilder abstainPercentage(BigDecimal abstainPercentage) {
            this.abstainPercentage = abstainPercentage;
            return this;
        }
        
        public VoteResultsDtoBuilder isPassed(boolean isPassed) {
            this.isPassed = isPassed;
            return this;
        }
        
        public VoteResultsDtoBuilder isRejected(boolean isRejected) {
            this.isRejected = isRejected;
            return this;
        }
        
        public VoteResultsDto build() {
            return new VoteResultsDto(totalVotes, yesVotes, noVotes, abstainVotes, yesPercentage, noPercentage, abstainPercentage, isPassed, isRejected);
        }
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