package com.bloominggrace.governance.governance.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.*;
import com.bloominggrace.governance.governance.infrastructure.repository.ProposalRepository;
import com.bloominggrace.governance.governance.infrastructure.repository.VoteRepository;
import com.bloominggrace.governance.governance.application.dto.*;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.token.application.service.TokenApplicationService;
import com.bloominggrace.governance.shared.domain.model.BlockchainTransactionType;
import com.bloominggrace.governance.shared.domain.model.Transaction;
import com.bloominggrace.governance.shared.infrastructure.repository.TransactionRepository;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator.TransactionResult;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class GovernanceApplicationService {
    
    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final TokenApplicationService tokenApplicationService;
    private final TransactionRepository transactionRepository;
    private final TransactionOrchestrator transactionOrchestrator;
    
    // ===== 거버넌스 관련 메서드들 =====
    
    /**
     * 정책 발의 생성 (블록체인 트랜잭션 포함)
     */
    public ProposalDto createProposalWithTransaction(
            UserId creatorId,
            String title,
            String description,
            LocalDateTime votingStartDate,
            LocalDateTime votingEndDate,
            long requiredQuorum,
            String creatorWalletAddress,
            BigDecimal proposalFee,
            String networkType) {
        
        // 1. 제안자 토큰 잔액 확인
        NetworkType networkTypeEnum = determineNetworkType(creatorWalletAddress);
        TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
            creatorId, creatorWalletAddress, networkTypeEnum, "default-contract", "TOKEN");
        if (tokenAccount.getAvailableBalance().compareTo(proposalFee) < 0) {
            throw new IllegalStateException("제안 수수료가 부족합니다. 필요: " + proposalFee + ", 보유: " + tokenAccount.getAvailableBalance());
        }
        
        // 2. 제안서 생성
        VotingPeriod votingPeriod = new VotingPeriod(votingStartDate, votingEndDate);
        Proposal proposal = new Proposal(creatorId, title, description, votingPeriod, requiredQuorum);
        proposal = proposalRepository.save(proposal);
        
        // 3. TransactionOrchestrator를 통해 트랜잭션 생성 및 브로드캐스트
        TransactionResult txResult = transactionOrchestrator.executeProposalCreation(
            proposal.getId().getValue(),
            title,
            description,
            creatorWalletAddress,
            networkTypeEnum,
            proposalFee,
            votingStartDate,
            votingEndDate,
            BigDecimal.valueOf(requiredQuorum)
        );
        
        if (!txResult.isSuccess()) {
            throw new RuntimeException("Failed to create proposal transaction: " + txResult.getErrorMessage());
        }
        
        // 4. 토큰 차감 (제안 수수료) - 실제 블록체인에서 이미 처리됨
        tokenAccount.burnTokens(proposalFee, "제안 수수료: " + proposal.getTitle());
        
        // 5. 블록체인 트랜잭션 기록 (실제 블록체인 트랜잭션 해시 저장)
        Transaction transaction = new Transaction(
            tokenAccount.getUserId(),
            BlockchainTransactionType.TOKEN_BURN,
            NetworkType.valueOf(networkType.toUpperCase()),
            proposalFee,
            creatorWalletAddress,
            null, // BURN은 toAddress 없음
            "제안 수수료: " + proposal.getTitle()
        );
        transaction.confirm(txResult.getTransactionHash()); // 실제 블록체인 트랜잭션 해시
        transactionRepository.save(transaction);
        
        return ProposalDto.from(proposal, txResult.getTransactionHash(), creatorWalletAddress);
    }
    
    /**
     * 투표 (스냅샷 방식 - 토큰 소각 없음)
     */
    public VoteDto voteWithSnapshot(
            ProposalId proposalId,
            UserId voterId,
            VoteType voteType,
            String reason,
            String voterWalletAddress,
            String networkType) {
        
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        if (!proposal.canVote()) {
            throw new IllegalStateException("Voting is not active for this proposal");
        }
        
        if (voteRepository.existsByProposalIdAndVoterId(proposalId, voterId)) {
            throw new IllegalStateException("User has already voted on this proposal");
        }
        
        // 1. 투표자 토큰 잔액 확인 (스냅샷용)
        NetworkType networkTypeEnum = determineNetworkType(voterWalletAddress);
        TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
            voterId, voterWalletAddress, networkTypeEnum, "default-contract", "TOKEN");
        
        // 2. 현재 토큰 잔액을 투표권으로 사용 (스냅샷 방식)
        BigDecimal votingPower = tokenAccount.getAvailableBalance();
        if (votingPower.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("투표에 필요한 토큰이 없습니다. 현재 잔액: " + votingPower);
        }
        
        // 3. 투표 기록 생성 (토큰 소각 없음)
        Vote vote = new Vote(proposalId, voterId, voteType, votingPower.longValue(), reason);
        vote = voteRepository.save(vote);
        
        // 4. 제안서에 투표 추가
        proposal.addVote(voteType, votingPower.longValue());
        proposalRepository.save(proposal);
        
        // 5. 투표 트랜잭션 기록 (스냅샷 방식이므로 실제 블록체인 트랜잭션 없음)
        Transaction transaction = new Transaction(
            tokenAccount.getUserId(),
            BlockchainTransactionType.PROPOSAL_VOTE,
            NetworkType.valueOf(networkType.toUpperCase()),
            votingPower,
            voterWalletAddress,
            null,
            "투표 (스냅샷): " + proposal.getTitle() + " - " + voteType.name() + " (투표권: " + votingPower + ")"
        );
        transaction.confirm("SNAPSHOT_VOTE_" + vote.getId().getValue()); // 스냅샷 투표는 가상 해시
        transactionRepository.save(transaction);
        
        return VoteDto.from(vote, "SNAPSHOT_VOTE_" + vote.getId().getValue(), voterWalletAddress);
    }
    
    // ===== 조회 메서드들 =====
    
    @Transactional(readOnly = true)
    public Optional<Proposal> getProposal(ProposalId proposalId) {
        return proposalRepository.findById(proposalId);
    }
    
    @Transactional(readOnly = true)
    public List<Proposal> getProposalsByStatus(ProposalStatus status) {
        return proposalRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Proposal> getProposalsByCreator(UserId creatorId) {
        return proposalRepository.findByCreatorId(creatorId);
    }
    
    @Transactional(readOnly = true)
    public List<Vote> getVotesByProposal(ProposalId proposalId) {
        return voteRepository.findByProposalId(proposalId);
    }
    
    @Transactional(readOnly = true)
    public List<Vote> getVotesByVoter(UserId voterId) {
        return voteRepository.findByVoterId(voterId);
    }
    

    // ===== 유틸리티 메서드들 =====
    private NetworkType determineNetworkType(String walletAddress) {
        // 간단한 주소 길이로 네트워크 타입 판단 (실제로는 더 정교한 검증 필요)
        if (walletAddress.startsWith("0x") && walletAddress.length() == 42) {
            return NetworkType.ETHEREUM;
        } else if (walletAddress.length() == 44) {
            return NetworkType.SOLANA;
        } else {
            throw new IllegalArgumentException("Unsupported wallet address format: " + walletAddress);
        }
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void processExpiredProposals() {
        List<Proposal> activeProposals = proposalRepository.findByStatus(ProposalStatus.VOTING);
        LocalDateTime now = LocalDateTime.now();
        
        for (Proposal proposal : activeProposals) {
            if (proposal.getVotingPeriod().getEndDate().isBefore(now)) {
                proposal.endVoting();
                proposalRepository.save(proposal);
            }
        }
    }
} 