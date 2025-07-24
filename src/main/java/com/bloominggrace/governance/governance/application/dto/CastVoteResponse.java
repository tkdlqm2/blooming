package com.bloominggrace.governance.governance.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CastVoteResponse {
    
    private UUID voteId;
    private UUID proposalId;
    private UUID voterId;
    private String voteType;
    private BigDecimal votingPower;
    private String reason;
    private String transactionHash;
    private String status; // PENDING, CONFIRMED, FAILED
    private LocalDateTime votedAt;
    private String errorMessage; // 실패 시에만
    
    public static CastVoteResponse success(
        UUID voteId,
        UUID proposalId,
        UUID voterId,
        String voteType,
        BigDecimal votingPower,
        String reason,
        String transactionHash) {
        
        return CastVoteResponse.builder()
            .voteId(voteId)
            .proposalId(proposalId)
            .voterId(voterId)
            .voteType(voteType)
            .votingPower(votingPower)
            .reason(reason)
            .transactionHash(transactionHash)
            .status("CONFIRMED")
            .votedAt(LocalDateTime.now())
            .build();
    }
    
    public static CastVoteResponse failure(
        UUID proposalId,
        UUID voterId,
        String voteType,
        String errorMessage) {
        
        return CastVoteResponse.builder()
            .proposalId(proposalId)
            .voterId(voterId)
            .voteType(voteType)
            .status("FAILED")
            .errorMessage(errorMessage)
            .votedAt(LocalDateTime.now())
            .build();
    }
} 