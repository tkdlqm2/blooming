package com.bloominggrace.governance.governance.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 거버넌스 트랜잭션 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceTransactionResponse {
    
    private UUID transactionId;
    private String transactionHash;
    private String status;
    private String walletAddress;
    private String networkType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    
    // 제안 관련 정보
    private UUID proposalId;
    private String proposalTitle;
    
    // 투표 관련 정보
    private String voteType;
    private BigDecimal votingPower;
    private String reason;
    
    // 에러 정보
    private String errorMessage;
    private String errorCode;
    
    /**
     * 성공 응답 생성
     */
    public static GovernanceTransactionResponse success(
        UUID transactionId,
        String transactionHash,
        String walletAddress,
        String networkType,
        BigDecimal amount,
        String description,
        UUID proposalId,
        String proposalTitle) {
        
        GovernanceTransactionResponse response = new GovernanceTransactionResponse();
        response.setTransactionId(transactionId);
        response.setTransactionHash(transactionHash);
        response.setStatus("CONFIRMED");
        response.setWalletAddress(walletAddress);
        response.setNetworkType(networkType);
        response.setAmount(amount);
        response.setDescription(description);
        response.setCreatedAt(LocalDateTime.now());
        response.setConfirmedAt(LocalDateTime.now());
        response.setProposalId(proposalId);
        response.setProposalTitle(proposalTitle);
        return response;
    }
    
    /**
     * 대기 중 응답 생성
     */
    public static GovernanceTransactionResponse pending(
        UUID transactionId,
        String walletAddress,
        String networkType,
        BigDecimal amount,
        String description,
        UUID proposalId,
        String proposalTitle) {
        
        GovernanceTransactionResponse response = new GovernanceTransactionResponse();
        response.setTransactionId(transactionId);
        response.setStatus("PENDING");
        response.setWalletAddress(walletAddress);
        response.setNetworkType(networkType);
        response.setAmount(amount);
        response.setDescription(description);
        response.setCreatedAt(LocalDateTime.now());
        response.setProposalId(proposalId);
        response.setProposalTitle(proposalTitle);
        return response;
    }
    
    /**
     * 실패 응답 생성
     */
    public static GovernanceTransactionResponse failure(
        UUID transactionId,
        String walletAddress,
        String networkType,
        String errorMessage,
        String errorCode) {
        
        GovernanceTransactionResponse response = new GovernanceTransactionResponse();
        response.setTransactionId(transactionId);
        response.setStatus("FAILED");
        response.setWalletAddress(walletAddress);
        response.setNetworkType(networkType);
        response.setErrorMessage(errorMessage);
        response.setErrorCode(errorCode);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
    
    /**
     * 투표 성공 응답 생성
     */
    public static GovernanceTransactionResponse voteSuccess(
        UUID transactionId,
        String transactionHash,
        String walletAddress,
        String networkType,
        BigDecimal votingPower,
        String voteType,
        String reason,
        UUID proposalId,
        String proposalTitle) {
        
        GovernanceTransactionResponse response = new GovernanceTransactionResponse();
        response.setTransactionId(transactionId);
        response.setTransactionHash(transactionHash);
        response.setStatus("CONFIRMED");
        response.setWalletAddress(walletAddress);
        response.setNetworkType(networkType);
        response.setAmount(votingPower);
        response.setVotingPower(votingPower);
        response.setVoteType(voteType);
        response.setReason(reason);
        response.setDescription("투표: " + proposalTitle + " - " + voteType);
        response.setCreatedAt(LocalDateTime.now());
        response.setConfirmedAt(LocalDateTime.now());
        response.setProposalId(proposalId);
        response.setProposalTitle(proposalTitle);
        return response;
    }
    
    /**
     * 민팅 성공 응답 생성
     */
    public static GovernanceTransactionResponse mintSuccess(
        UUID transactionId,
        String transactionHash,
        String adminWalletAddress,
        String recipientWalletAddress,
        String networkType,
        BigDecimal amount) {
        
        GovernanceTransactionResponse response = new GovernanceTransactionResponse();
        response.setTransactionId(transactionId);
        response.setTransactionHash(transactionHash);
        response.setStatus("CONFIRMED");
        response.setWalletAddress(adminWalletAddress);
        response.setNetworkType(networkType);
        response.setAmount(amount);
        response.setDescription("거버넌스 토큰 민팅: " + amount + " 토큰 -> " + recipientWalletAddress);
        response.setCreatedAt(LocalDateTime.now());
        response.setConfirmedAt(LocalDateTime.now());
        return response;
    }
    
    /**
     * 소각 성공 응답 생성
     */
    public static GovernanceTransactionResponse burnSuccess(
        UUID transactionId,
        String transactionHash,
        String walletAddress,
        String networkType,
        BigDecimal amount) {
        
        GovernanceTransactionResponse response = new GovernanceTransactionResponse();
        response.setTransactionId(transactionId);
        response.setTransactionHash(transactionHash);
        response.setStatus("CONFIRMED");
        response.setWalletAddress(walletAddress);
        response.setNetworkType(networkType);
        response.setAmount(amount);
        response.setDescription("거버넌스 토큰 소각: " + amount + " 토큰");
        response.setCreatedAt(LocalDateTime.now());
        response.setConfirmedAt(LocalDateTime.now());
        return response;
    }
} 