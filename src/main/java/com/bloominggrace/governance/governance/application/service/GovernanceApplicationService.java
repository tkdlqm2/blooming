package com.bloominggrace.governance.governance.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.*;
import com.bloominggrace.governance.governance.infrastructure.repository.ProposalRepository;
import com.bloominggrace.governance.governance.infrastructure.repository.VoteRepository;
import com.bloominggrace.governance.governance.application.dto.*;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.token.application.service.TokenApplicationService;
import com.bloominggrace.governance.shared.domain.model.BlockchainTransactionType;
import com.bloominggrace.governance.shared.domain.model.Transaction;
import com.bloominggrace.governance.shared.infrastructure.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.TransactionRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class GovernanceApplicationService {
    
    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final WalletApplicationService walletApplicationService;
    private final TokenApplicationService tokenApplicationService;
    private final TransactionRepository transactionRepository;
    
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
        
        // 3. 실제 블록체인 네트워크에 정책 제안 트랜잭션 생성
        TransactionRequest.ProposalData proposalData = TransactionRequest.ProposalData.builder()
            .proposalId(proposal.getId().getValue().toString())
            .title(title)
            .description(description)
            .proposalFee(proposalFee)
            .build();
        TransactionBody txBody = walletApplicationService.createTransactionBody(
            creatorWalletAddress,
            null,
            null,
            null,
            proposalData,
            TransactionRequest.TransactionType.PROPOSAL_CREATE,
            NetworkType.valueOf(networkType.toUpperCase())
        );
        byte[] signedRawTransaction = walletApplicationService.signTransactionBody(txBody, creatorWalletAddress);
        String transactionSignature = walletApplicationService.broadcastSignedTransaction(signedRawTransaction, networkType);
        
        // 4. 토큰 차감 (제안 수수료) - 실제 블록체인에서 이미 처리됨
        tokenAccount.burnTokens(proposalFee, "제안 수수료: " + proposal.getTitle());
        // tokenAccountRepository.save(tokenAccount); // TokenApplicationService에서 처리됨
        
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
        transaction.confirm(transactionSignature); // 실제 블록체인 트랜잭션 해시
        transactionRepository.save(transaction);
        
        return ProposalDto.from(proposal, transactionSignature, creatorWalletAddress);
    }
    
    /**
     * 투표 (블록체인 트랜잭션 포함)
     */
    public VoteDto voteWithTransaction(
            ProposalId proposalId,
            UserId voterId,
            VoteType voteType,
            String reason,
            String voterWalletAddress,
            BigDecimal votingPower,
            String networkType) {
        
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        if (!proposal.canVote()) {
            throw new IllegalStateException("Voting is not active for this proposal");
        }
        
        if (voteRepository.existsByProposalIdAndVoterId(proposalId, voterId)) {
            throw new IllegalStateException("User has already voted on this proposal");
        }
        
        // 1. 투표자 토큰 잔액 확인
        NetworkType networkTypeEnum = determineNetworkType(voterWalletAddress);
        TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
            voterId, voterWalletAddress, networkTypeEnum, "default-contract", "TOKEN");
        if (tokenAccount.getAvailableBalance().compareTo(votingPower) < 0) {
            throw new IllegalStateException("투표에 필요한 토큰이 부족합니다. 필요: " + votingPower + ", 보유: " + tokenAccount.getAvailableBalance());
        }
        
        // 2. 실제 블록체인 네트워크에 투표 트랜잭션 생성 및 브로드캐스트
        // 2-1. 투표용 TransactionBody 생성 (통합된 메서드 사용)
        TransactionRequest.VoteData voteData = TransactionRequest.VoteData.builder()
            .proposalId(proposalId.getValue().toString())
            .voteType(voteType.name())
            .votingPower(votingPower)
            .reason(reason)
            .build();
        TransactionBody voteTxBody = walletApplicationService.createTransactionBody(
            voterWalletAddress,           // fromAddress
            null,                         // toAddress (투표에는 불필요)
            null,                         // amount (투표에는 불필요)
            null,                         // tokenAddress (투표에는 불필요)
            voteData,                     // 투표 고유 데이터
            TransactionRequest.TransactionType.PROPOSAL_VOTE, // transactionType
            NetworkType.valueOf(networkType.toUpperCase()) // networkType
        );
        
        // 2-2. TransactionBody 서명하여 signedRawTransaction 반환
        byte[] signedRawTransaction = walletApplicationService.signTransactionBody(voteTxBody, voterWalletAddress);
        
        // 2-3. signedRawTransaction 브로드캐스트
        String transactionSignature = walletApplicationService.broadcastSignedTransaction(signedRawTransaction, networkType);
        
        // 3. 토큰 스테이킹 (투표권으로 사용) - 실제 블록체인에서 이미 처리됨
        tokenAccount.stakeTokens(votingPower);
        // tokenAccountRepository.save(tokenAccount); // TokenApplicationService에서 처리됨
        
        // 4. 투표 기록 생성
        Vote vote = new Vote(proposalId, voterId, voteType, votingPower.longValue(), reason);
        vote = voteRepository.save(vote);
        
        // 5. 제안서에 투표 추가
        proposal.addVote(voteType, votingPower.longValue());
        proposalRepository.save(proposal);
        
        // 6. 블록체인 트랜잭션 기록 (실제 블록체인 트랜잭션 해시 저장)
        Transaction transaction = new Transaction(
            tokenAccount.getUserId(),
            BlockchainTransactionType.TOKEN_STAKE,
            NetworkType.valueOf(networkType.toUpperCase()),
            votingPower,
            voterWalletAddress,
            null, // STAKE는 toAddress 없음
            "투표: " + proposal.getTitle() + " - " + voteType.name()
        );
        transaction.confirm(transactionSignature); // 실제 블록체인 트랜잭션 해시
        transactionRepository.save(transaction);
        
        return VoteDto.from(vote, transactionSignature, voterWalletAddress);
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
    
    @Transactional(readOnly = true)
    public Optional<Vote> getVoteByProposalAndVoter(ProposalId proposalId, UserId voterId) {
        return voteRepository.findByProposalIdAndVoterId(proposalId, voterId);
    }
    


    // ===== 토큰 관련 메서드들 =====
    
    /**
     * 지갑 주소로 네트워크 타입 결정
     */
    private NetworkType determineNetworkType(String walletAddress) {
        if (walletAddress.startsWith("0x")) {
            return NetworkType.ETHEREUM;
        } else if (walletAddress.length() == 44) {
            return NetworkType.SOLANA;
        } else {
            throw new IllegalArgumentException("Unsupported wallet address format: " + walletAddress);
        }
    }
    
    /**
     * 투표권 계산 (스테이킹된 토큰 기반)
     */
    private long calculateVotingPower(UserId userId) {
        return tokenApplicationService.calculateVotingPower(userId);
    }
    
    // ===== 조회 메서드들 =====
    
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(UserId userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    

    
    // 스케줄러: 만료된 투표 기간의 제안서들을 자동으로 종료
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void processExpiredProposals() {
        LocalDateTime now = LocalDateTime.now();
        List<Proposal> expiredProposals = proposalRepository.findByVotingPeriodEndBefore(now);
        
        for (Proposal proposal : expiredProposals) {
            if (proposal.isVotingActive()) {
                proposal.endVoting();
                proposalRepository.save(proposal);
            }
        }
    }
} 