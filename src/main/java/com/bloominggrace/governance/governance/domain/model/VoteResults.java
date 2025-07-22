package com.bloominggrace.governance.governance.domain.model;

import com.bloominggrace.governance.shared.domain.ValueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
public class VoteResults extends ValueObject {
    @Column(name = "total_votes")
    private long totalVotes;
    
    @Column(name = "yes_votes")
    private long yesVotes;
    
    @Column(name = "no_votes")
    private long noVotes;
    
    @Column(name = "abstain_votes")
    private long abstainVotes;
    
    @Column(name = "yes_percentage", precision = 5, scale = 2)
    private BigDecimal yesPercentage;
    
    @Column(name = "no_percentage", precision = 5, scale = 2)
    private BigDecimal noPercentage;
    
    @Column(name = "abstain_percentage", precision = 5, scale = 2)
    private BigDecimal abstainPercentage;

    // Hibernate를 위한 기본 생성자
    protected VoteResults() {
        this.totalVotes = 0;
        this.yesVotes = 0;
        this.noVotes = 0;
        this.abstainVotes = 0;
        this.yesPercentage = BigDecimal.ZERO;
        this.noPercentage = BigDecimal.ZERO;
        this.abstainPercentage = BigDecimal.ZERO;
    }

    public VoteResults(long totalVotes, long yesVotes, long noVotes, long abstainVotes) {
        if (totalVotes < 0 || yesVotes < 0 || noVotes < 0 || abstainVotes < 0) {
            throw new IllegalArgumentException("Vote counts cannot be negative");
        }
        if (yesVotes + noVotes + abstainVotes != totalVotes) {
            throw new IllegalArgumentException("Total votes must equal sum of individual vote types");
        }
        
        this.totalVotes = totalVotes;
        this.yesVotes = yesVotes;
        this.noVotes = noVotes;
        this.abstainVotes = abstainVotes;
        
        if (totalVotes == 0) {
            this.yesPercentage = BigDecimal.ZERO;
            this.noPercentage = BigDecimal.ZERO;
            this.abstainPercentage = BigDecimal.ZERO;
        } else {
            this.yesPercentage = BigDecimal.valueOf(yesVotes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalVotes), 2, RoundingMode.HALF_UP);
            this.noPercentage = BigDecimal.valueOf(noVotes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalVotes), 2, RoundingMode.HALF_UP);
            this.abstainPercentage = BigDecimal.valueOf(abstainVotes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalVotes), 2, RoundingMode.HALF_UP);
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
        return yesVotes > noVotes;
    }

    public boolean isRejected() {
        return noVotes >= yesVotes;
    }

    public boolean hasQuorum(long requiredQuorum) {
        return totalVotes >= requiredQuorum;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VoteResults that = (VoteResults) obj;
        return totalVotes == that.totalVotes &&
               yesVotes == that.yesVotes &&
               noVotes == that.noVotes &&
               abstainVotes == that.abstainVotes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalVotes, yesVotes, noVotes, abstainVotes);
    }

    @Override
    public String toString() {
        return String.format("VoteResults{total=%d, yes=%d(%.2f%%), no=%d(%.2f%%), abstain=%d(%.2f%%)}",
                           totalVotes, yesVotes, yesPercentage, noVotes, noPercentage, abstainVotes, abstainPercentage);
    }
} 