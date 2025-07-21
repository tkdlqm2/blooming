package com.bloominggrace.governance.governance.domain.service;

import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BlockchainGovernanceService {
    NetworkType getSupportedNetworkType();

    // ===== 거버넌스 트랜잭션 생성 =====
    TransactionBody createProposalTransaction(
        String proposalId,
        String title,
        String description,
        String creatorWalletAddress,
        BigDecimal proposalFee,
        LocalDateTime votingStartDate,
        LocalDateTime votingEndDate,
        BigDecimal requiredQuorum
    );

    TransactionBody createVoteTransaction(
        String proposalId,
        String voterWalletAddress,
        VoteType voteType,
        BigDecimal votingPower,
        String reason
    );

    // ===== 토큰 관련 트랜잭션 =====
    TransactionBody createTokenTransferTransaction(
        String fromAddress,
        String toAddress,
        BigDecimal amount,
        String tokenContract
    );

    TransactionBody createTokenMintTransaction(
        String toAddress,
        BigDecimal amount,
        String tokenContract
    );

    TransactionBody createTokenBurnTransaction(
        String fromAddress,
        BigDecimal amount,
        String tokenContract
    );

    // ===== 교환 관련 트랜잭션 =====
    TransactionBody createExchangeTransaction(
        String userAddress,
        BigDecimal amount,
        String exchangeType,
        String tokenContract
    );

    // ===== 트랜잭션 데이터 생성 (바이너리) =====
    byte[] createProposalTransactionData(
        ProposalId proposalId,
        String title,
        String description,
        String creatorWalletAddress,
        BigDecimal proposalFee
    );

    byte[] createVoteTransactionData(
        ProposalId proposalId,
        String voterWalletAddress,
        VoteType voteType,
        BigDecimal votingPower,
        String reason
    );

    byte[] createExecuteProposalTransactionData(
        ProposalId proposalId,
        String executorWalletAddress
    );



    byte[] createBurnGovernanceTokensTransactionData(
        String burnerWalletAddress,
        BigDecimal amount
    );



    // ===== 교환 관련 트랜잭션 =====
    TransactionBody createExchangePointsToTokensTransaction(
        String userWalletAddress,
        BigDecimal pointAmount,
        BigDecimal tokenAmount,
        String description
    );

    TransactionBody createExchangeTokensToPointsTransaction(
        String userWalletAddress,
        BigDecimal tokenAmount,
        BigDecimal pointAmount,
        String description
    );

    /**
     * 교환 완료 트랜잭션 생성 (Admin 계정에서 교환 요청한 지갑으로 토큰 전송)
     */
    TransactionBody createCompleteExchangeTransaction(
        String adminWalletAddress,
        String userWalletAddress,
        BigDecimal tokenAmount,
        String description
    );

    byte[] createExchangeRequestTransactionData(
        String userWalletAddress,
        BigDecimal pointAmount,
        BigDecimal tokenAmount,
        String exchangeType
    );

    byte[] createExecuteExchangeTransactionData(
        String userWalletAddress,
        BigDecimal pointAmount,
        BigDecimal tokenAmount,
        String exchangeType
    );

    /**
     * 교환 완료 트랜잭션 데이터 생성 (바이너리)
     */
    byte[] createCompleteExchangeTransactionData(
        String adminWalletAddress,
        String userWalletAddress,
        BigDecimal tokenAmount
    );

    // ===== 상태 조회 =====
    ProposalStatusInfo getProposalStatusFromBlockchain(ProposalId proposalId);
    VoteResultInfo getVoteResultsFromBlockchain(ProposalId proposalId);

    ExchangeInfo getExchangeInfoFromBlockchain(String walletAddress);

    // ===== 서명 검증 =====
    boolean verifyTransactionSignature(
        byte[] transactionData,
        byte[] signature,
        String publicKey
    );

    // ===== 결과 정보 DTO =====
    class ProposalStatusInfo {
        public final Object proposalId;
        public final String status;
        public final LocalDateTime votingStart;
        public final LocalDateTime votingEnd;
        public final BigDecimal requiredQuorum;
        public final BigDecimal totalVotes;
        public ProposalStatusInfo(Object proposalId, String status, LocalDateTime votingStart, LocalDateTime votingEnd, BigDecimal requiredQuorum, BigDecimal totalVotes) {
            this.proposalId = proposalId;
            this.status = status;
            this.votingStart = votingStart;
            this.votingEnd = votingEnd;
            this.requiredQuorum = requiredQuorum;
            this.totalVotes = totalVotes;
        }
        
        public Object getProposalId() { return proposalId; }
        public String getStatus() { return status; }
        public LocalDateTime getVotingStart() { return votingStart; }
        public LocalDateTime getVotingEnd() { return votingEnd; }
        public BigDecimal getRequiredQuorum() { return requiredQuorum; }
        public BigDecimal getTotalVotes() { return totalVotes; }
    }
    class VoteResultInfo {
        public final Object proposalId;
        public final BigDecimal yesVotes;
        public final BigDecimal noVotes;
        public final BigDecimal abstainVotes;
        public final BigDecimal totalVotes;
        public final String status;
        public VoteResultInfo(Object proposalId, BigDecimal yesVotes, BigDecimal noVotes, BigDecimal abstainVotes, BigDecimal totalVotes, String status) {
            this.proposalId = proposalId;
            this.yesVotes = yesVotes;
            this.noVotes = noVotes;
            this.abstainVotes = abstainVotes;
            this.totalVotes = totalVotes;
            this.status = status;
        }
        
        public Object getProposalId() { return proposalId; }
        public BigDecimal getYesVotes() { return yesVotes; }
        public BigDecimal getNoVotes() { return noVotes; }
        public BigDecimal getAbstainVotes() { return abstainVotes; }
        public BigDecimal getTotalVotes() { return totalVotes; }
        public String getStatus() { return status; }
    }

    class ExchangeInfo {
        public final String walletAddress;
        public final BigDecimal exchangeRate;
        public final BigDecimal minimumExchangeAmount;
        public final BigDecimal cooldownPeriod;
        public final LocalDateTime lastExchangeAt;
        public ExchangeInfo(String walletAddress, BigDecimal exchangeRate, BigDecimal minimumExchangeAmount, BigDecimal cooldownPeriod, LocalDateTime lastExchangeAt) {
            this.walletAddress = walletAddress;
            this.exchangeRate = exchangeRate;
            this.minimumExchangeAmount = minimumExchangeAmount;
            this.cooldownPeriod = cooldownPeriod;
            this.lastExchangeAt = lastExchangeAt;
        }
        
        public String getWalletAddress() { return walletAddress; }
        public BigDecimal getExchangeRate() { return exchangeRate; }
        public BigDecimal getMinimumExchangeAmount() { return minimumExchangeAmount; }
        public BigDecimal getCooldownPeriod() { return cooldownPeriod; }
        public LocalDateTime getLastExchangeAt() { return lastExchangeAt; }
    }
} 