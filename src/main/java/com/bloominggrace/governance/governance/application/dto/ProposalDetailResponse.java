package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.Proposal;
import com.bloominggrace.governance.governance.domain.model.Vote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalDetailResponse {
    
    // 제안 기본 정보
    private UUID proposalId;
    private String title;
    private String description;
    private String status;
    private UUID creatorId;
    private String creatorWalletAddress;
    private long requiredQuorum;
    
    // 투표 기간
    private LocalDateTime votingStartDate;
    private LocalDateTime votingEndDate;
    private boolean isVotingActive;
    private long daysRemaining;
    
    // 투표 현황
    private BigDecimal totalVotingPower;
    private BigDecimal forVotes;
    private BigDecimal againstVotes;
    private BigDecimal abstainVotes;
    private BigDecimal participationRate;
    private boolean isQuorumMet;
    
    // 블록체인 정보
    private String transactionHash;
    private String networkType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 투표자 목록 (선택적)
    private List<VoteDetailDto> votes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoteDetailDto {
        private UUID voterId;
        private String voterWalletAddress;
        private String voteType; // FOR, AGAINST, ABSTAIN
        private BigDecimal votingPower;
        private String reason;
        private LocalDateTime votedAt;
        private String transactionHash;
    }
    
    public static ProposalDetailResponse from(Proposal proposal, List<Vote> votes, String transactionHash, String networkType) {
        LocalDateTime now = LocalDateTime.now();
        // 수정: 올바른 메서드명 사용
        boolean isVotingActive = proposal.getVotingPeriod().isVotingActive();
        long daysRemaining = proposal.getVotingPeriod().getRemainingDays();
        
        BigDecimal totalVotingPower = votes.stream()
            .map(vote -> BigDecimal.valueOf(vote.getVotingPower()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal forVotes = votes.stream()
            .filter(vote -> vote.getVoteType().name().equals("FOR"))
            .map(vote -> BigDecimal.valueOf(vote.getVotingPower()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal againstVotes = votes.stream()
            .filter(vote -> vote.getVoteType().name().equals("AGAINST"))
            .map(vote -> BigDecimal.valueOf(vote.getVotingPower()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal abstainVotes = votes.stream()
            .filter(vote -> vote.getVoteType().name().equals("ABSTAIN"))
            .map(vote -> BigDecimal.valueOf(vote.getVotingPower()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal participationRate = totalVotingPower.compareTo(BigDecimal.ZERO) > 0 
            ? totalVotingPower.divide(BigDecimal.valueOf(proposal.getRequiredQuorum()), 4, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
            
        boolean isQuorumMet = totalVotingPower.compareTo(BigDecimal.valueOf(proposal.getRequiredQuorum())) >= 0;
        
        List<VoteDetailDto> voteDetails = votes.stream()
            .map(vote -> VoteDetailDto.builder()
                .voterId(vote.getVoterId().getValue())
                .voterWalletAddress(null) // TODO: 지갑 주소 조회 로직 추가
                .voteType(vote.getVoteType().name())
                .votingPower(BigDecimal.valueOf(vote.getVotingPower()))
                .reason(vote.getReason())
                .votedAt(vote.getCreatedAt())
                .transactionHash(null) // TODO: 트랜잭션 해시 조회 로직 추가
                .build())
            .toList();
        
        return ProposalDetailResponse.builder()
            .proposalId(proposal.getId().getValue())
            .title(proposal.getTitle())
            .description(proposal.getDescription())
            .status(proposal.getStatus().name())
            .creatorId(proposal.getCreatorId().getValue())
            .creatorWalletAddress(null) // TODO: 제안자 지갑 주소 조회 로직 추가
            .requiredQuorum(proposal.getRequiredQuorum())
            .votingStartDate(proposal.getVotingPeriod().getStartDate())
            .votingEndDate(proposal.getVotingPeriod().getEndDate())
            .isVotingActive(isVotingActive)
            .daysRemaining(daysRemaining)
            .totalVotingPower(totalVotingPower)
            .forVotes(forVotes)
            .againstVotes(againstVotes)
            .abstainVotes(abstainVotes)
            .participationRate(participationRate)
            .isQuorumMet(isQuorumMet)
            .transactionHash(transactionHash)
            .networkType(networkType)
            .createdAt(proposal.getCreatedAt())
            .updatedAt(proposal.getUpdatedAt())
            .votes(voteDetails)
            .build();
    }
} 