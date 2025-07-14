package com.bloominggrace.governance.governance.domain.model;

import com.bloominggrace.governance.shared.domain.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

public class VotingPeriod extends ValueObject {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public VotingPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
        
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public boolean isVotingActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    public boolean isVotingEnded() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean isVotingNotStarted() {
        return LocalDateTime.now().isBefore(startDate);
    }

    public long getRemainingDays() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDate)) {
            return 0;
        }
        return java.time.Duration.between(now, endDate).toDays();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VotingPeriod that = (VotingPeriod) obj;
        return Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return String.format("VotingPeriod{startDate=%s, endDate=%s}", startDate, endDate);
    }
} 