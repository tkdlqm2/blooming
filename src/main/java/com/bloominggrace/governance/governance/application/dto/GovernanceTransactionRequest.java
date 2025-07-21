package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 거버넌스 트랜잭션 생성을 위한 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceTransactionRequest {
    
    // 공통 필드
    private String walletAddress;
    private NetworkType networkType;
    
    // 제안 생성용 필드
    private UUID proposalId;
    private String title;
    private String description;
    private BigDecimal proposalFee;
    
    // 투표용 필드
    private VoteType voteType;
    private BigDecimal votingPower;
    private String reason;
    
    // 토큰 관련 필드
    private BigDecimal amount;
    private String recipientWalletAddress; // 민팅 시 수령자
    private String adminWalletAddress; // 민팅 시 관리자
    
    // 트랜잭션 타입
    private TransactionType transactionType;
    
    public enum TransactionType {
        CREATE_PROPOSAL,
        VOTE,
        EXECUTE_PROPOSAL,
        MINT_GOVERNANCE_TOKENS,
        BURN_GOVERNANCE_TOKENS
    }
    
    // ===== 정적 팩토리 메서드들 =====
    
    /**
     * 제안 생성 트랜잭션 요청 생성
     */
    public static GovernanceTransactionRequest createProposalRequest(
        String walletAddress,
        UUID proposalId,
        String title,
        String description,
        BigDecimal proposalFee,
        NetworkType networkType) {
        
        GovernanceTransactionRequest request = new GovernanceTransactionRequest();
        request.setWalletAddress(walletAddress);
        request.setNetworkType(networkType);
        request.setProposalId(proposalId);
        request.setTitle(title);
        request.setDescription(description);
        request.setProposalFee(proposalFee);
        request.setTransactionType(TransactionType.CREATE_PROPOSAL);
        return request;
    }
    
    /**
     * 투표 트랜잭션 요청 생성
     */
    public static GovernanceTransactionRequest createVoteRequest(
        String walletAddress,
        UUID proposalId,
        VoteType voteType,
        BigDecimal votingPower,
        String reason,
        NetworkType networkType) {
        
        GovernanceTransactionRequest request = new GovernanceTransactionRequest();
        request.setWalletAddress(walletAddress);
        request.setNetworkType(networkType);
        request.setProposalId(proposalId);
        request.setVoteType(voteType);
        request.setVotingPower(votingPower);
        request.setReason(reason);
        request.setTransactionType(TransactionType.VOTE);
        return request;
    }
    
    /**
     * 제안 실행 트랜잭션 요청 생성
     */
    public static GovernanceTransactionRequest createExecuteProposalRequest(
        String walletAddress,
        UUID proposalId,
        NetworkType networkType) {
        
        GovernanceTransactionRequest request = new GovernanceTransactionRequest();
        request.setWalletAddress(walletAddress);
        request.setNetworkType(networkType);
        request.setProposalId(proposalId);
        request.setTransactionType(TransactionType.EXECUTE_PROPOSAL);
        return request;
    }
    
    /**
     * 거버넌스 토큰 민팅 트랜잭션 요청 생성 (관리자 전용)
     */
    public static GovernanceTransactionRequest createMintGovernanceTokensRequest(
        String adminWalletAddress,
        String recipientWalletAddress,
        BigDecimal amount,
        NetworkType networkType) {
        
        GovernanceTransactionRequest request = new GovernanceTransactionRequest();
        request.setWalletAddress(adminWalletAddress);
        request.setNetworkType(networkType);
        request.setRecipientWalletAddress(recipientWalletAddress);
        request.setAmount(amount);
        request.setTransactionType(TransactionType.MINT_GOVERNANCE_TOKENS);
        return request;
    }
    
    /**
     * 거버넌스 토큰 소각 트랜잭션 요청 생성
     */
    public static GovernanceTransactionRequest createBurnGovernanceTokensRequest(
        String walletAddress,
        BigDecimal amount,
        NetworkType networkType) {
        
        GovernanceTransactionRequest request = new GovernanceTransactionRequest();
        request.setWalletAddress(walletAddress);
        request.setNetworkType(networkType);
        request.setAmount(amount);
        request.setTransactionType(TransactionType.BURN_GOVERNANCE_TOKENS);
        return request;
    }
} 