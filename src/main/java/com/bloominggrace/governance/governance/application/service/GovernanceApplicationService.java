package com.bloominggrace.governance.governance.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.*;
import com.bloominggrace.governance.governance.infrastructure.repository.ProposalRepository;
import com.bloominggrace.governance.governance.infrastructure.repository.VoteRepository;
import com.bloominggrace.governance.governance.application.dto.*;
import com.bloominggrace.governance.shared.domain.model.BlockchainMetadata;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.token.application.service.TokenApplicationService;
import com.bloominggrace.governance.shared.domain.model.BlockchainTransactionType;
import com.bloominggrace.governance.shared.domain.model.Transaction;
import com.bloominggrace.governance.shared.infrastructure.repository.TransactionRepository;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator.TransactionResult;
import com.bloominggrace.governance.shared.infrastructure.service.AdminWalletService;
import com.bloominggrace.governance.token.infrastructure.repository.TokenAccountJpaRepository;

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
    private final TokenAccountJpaRepository tokenAccountRepository;
    
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
        
        try {
            // 1. 제안자 토큰 잔액 확인
            NetworkType networkTypeEnum = determineNetworkType(creatorWalletAddress);
            TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
                creatorId, creatorWalletAddress, networkTypeEnum, "default-contract", "TOKEN");
            
            // 제안 수수료가 있는 경우 잔액 확인
            if (proposalFee.compareTo(BigDecimal.ZERO) > 0) {
                if (tokenAccount.getAvailableBalance().compareTo(proposalFee) < 0) {
                    throw new IllegalStateException("제안 수수료가 부족합니다. 필요: " + proposalFee + ", 보유: " + tokenAccount.getAvailableBalance());
                }
            }
            
            // 2. 제안서 생성
            VotingPeriod votingPeriod = new VotingPeriod(votingStartDate, votingEndDate);
            Proposal proposal = new Proposal(creatorId, title, description, votingPeriod, requiredQuorum);
            proposal = proposalRepository.save(proposal);

            // 3. 블록체인에 거버넌스 제안 트랜잭션 실행 (수수료와 관계없이 항상 실행)
            String transactionHash = null;
            
            System.out.println("=== 🔄 executeProposalCreation 호출 중... ===");
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
            
            transactionHash = txResult.getTransactionHash();
            
            // 제안 수수료가 있는 경우 토큰 차감
            if (proposalFee.compareTo(BigDecimal.ZERO) > 0) {
                tokenAccount.burnTokens(proposalFee, "제안 수수료: " + proposal.getTitle());
            }
            
            // 블록체인 트랜잭션 기록
            Transaction transaction = new Transaction(
                tokenAccount.getUserId(),
                BlockchainTransactionType.PROPOSAL_CREATE,
                NetworkType.valueOf(networkType.toUpperCase()),
                proposalFee,
                creatorWalletAddress,
                null, // 제안 생성은 toAddress 없음
                "거버넌스 제안 생성: " + proposal.getTitle()
            );
            transaction.confirm(transactionHash);
            transactionRepository.save(transaction);
            
            return ProposalDto.from(proposal, transactionHash, creatorWalletAddress);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 1단계: 거버넌스 제안 저장 (블록체인 트랜잭션 없이)
     */
    public ProposalDto createProposal(
            UserId creatorId,
            String title,
            String description,
            LocalDateTime votingStartDate,
            LocalDateTime votingEndDate,
            long requiredQuorum,
            String creatorWalletAddress,
            BigDecimal proposalFee,
            String networkType) {
        
        try {
            // 1. 제안서 생성 및 저장 (토큰 잔액 확인은 2단계에서 수행)
            VotingPeriod votingPeriod = new VotingPeriod(votingStartDate, votingEndDate);
            Proposal proposal = new Proposal(creatorId, title, description, votingPeriod, requiredQuorum);
            proposal = proposalRepository.save(proposal);
            
            System.out.println("=== 📝 거버넌스 제안 저장 완료 ===");
            System.out.println("제안 ID: " + proposal.getId().getValue());
            System.out.println("제안자 ID: " + proposal.getCreatorId().getValue());
            System.out.println("제목: " + proposal.getTitle());
            System.out.println("제안 수수료: " + proposalFee);
            System.out.println("지갑 주소: " + creatorWalletAddress);
            System.out.println("네트워크: " + networkType);
            
            return ProposalDto.from(proposal, null, creatorWalletAddress);
            
        } catch (Exception e) {
            System.err.println("=== ❌ 거버넌스 제안 저장 실패 ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 2단계: 거버넌스 제안 수수료 충전 (Admin에서 제안자로)
     */
    public String chargeProposalFee(
            ProposalId proposalId,
            String creatorWalletAddress,
            String networkType) {
        
        try {
            // 1. 제안 조회 및 유효성 검증
            Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + proposalId.getValue()));
            
            NetworkType networkTypeEnum = NetworkType.valueOf(networkType.toUpperCase());
            BigDecimal proposalFee = new BigDecimal("0.01");

            System.out.println("=== 💰 거버넌스 제안 수수료 충전 시작 ===");
            System.out.println("제안 ID: " + proposalId.getValue());
            System.out.println("제안자 지갑: " + creatorWalletAddress);
            System.out.println("필요 수수료: " + proposalFee);
            System.out.println("네트워크: " + networkTypeEnum);

            // 2. Admin 지갑 정보 조회
            System.out.println("=== 🔍 Admin 지갑 정보 조회 중... ===");
            AdminWalletService.AdminWalletInfo adminWallet = AdminWalletService.getAdminWallet(networkTypeEnum);

            // 3. Admin에서 제안자로 수수료 전송
            TransactionResult feeTransferResult = transactionOrchestrator.executeTransfer(
                adminWallet.getWalletAddress(),
                creatorWalletAddress,
                networkTypeEnum,
                proposalFee,
    null
            );
            
            if (!feeTransferResult.isSuccess()) {
                System.err.println("❌ 수수료 전송 실패: " + feeTransferResult.getErrorMessage());
                throw new RuntimeException("Failed to transfer proposal fee: " + feeTransferResult.getErrorMessage());
            }
            
            System.out.println("✅ 수수료 전송 성공!");
            System.out.println("수수료 트랜잭션 해시: " + feeTransferResult.getTransactionHash());
            System.out.println("=== 💰 거버넌스 제안 수수료 충전 완료 ===");
            
            // 4. 제안자의 토큰 계정에 수수료 추가 (임시로 제거)
             TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
                 proposal.getCreatorId(), creatorWalletAddress, networkTypeEnum, "default-contract", "TOKEN");
             tokenAccount.receiveTokens(proposalFee, "거버넌스 제안 수수료 충전: " + proposal.getTitle());
             tokenAccountRepository.save(tokenAccount);
            
            return feeTransferResult.getTransactionHash();
            
        } catch (Exception e) {
            System.err.println("=== ❌ 수수료 충전 실패 ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 3단계: 거버넌스 제안을 블록체인 네트워크로 브로드캐스트
     */
    public String broadcastProposal(
            ProposalId proposalId,
            String creatorWalletAddress,
            String networkType) {
        
        try {
            // 1. 제안 조회 및 유효성 검증
            Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + proposalId.getValue()));
            
            NetworkType networkTypeEnum = NetworkType.valueOf(networkType.toUpperCase());
            BigDecimal proposalFee = new BigDecimal("0.00029");

            System.out.println("=== 🚀 거버넌스 제안 블록체인 브로드캐스트 시작 ===");
            System.out.println("제안 ID: " + proposalId.getValue());
            System.out.println("제목: " + proposal.getTitle());
            System.out.println("제안자 지갑: " + creatorWalletAddress);
            System.out.println("네트워크: " + networkTypeEnum);

            // 2. 블록체인에 거버넌스 제안 트랜잭션 실행
            TransactionResult txResult = transactionOrchestrator.executeProposalCreation(
                proposal.getId().getValue(),
                proposal.getTitle(),
                proposal.getDescription(),
                creatorWalletAddress,
                networkTypeEnum,
                proposalFee,
                proposal.getVotingPeriod().getStartDate(),
                proposal.getVotingPeriod().getEndDate(),
                BigDecimal.valueOf(proposal.getRequiredQuorum())
            );
            
            if (!txResult.isSuccess()) {
                System.err.println("❌ 블록체인 브로드캐스트 실패: " + txResult.getErrorMessage());
                throw new RuntimeException("Failed to broadcast proposal: " + txResult.getErrorMessage());
            }
            
            String transactionHash = txResult.getTransactionHash();
            System.out.println("✅ 블록체인 브로드캐스트 성공!");
            System.out.println("거버넌스 트랜잭션 해시: " + transactionHash);
            System.out.println("=== 🚀 거버넌스 제안 블록체인 브로드캐스트 완료 ===");
            
            // 3. 제안 수수료가 있는 경우 토큰 차감
            if (proposalFee.compareTo(BigDecimal.ZERO) > 0) {
                TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
                    proposal.getCreatorId(), creatorWalletAddress, networkTypeEnum, "default-contract", "TOKEN");
                tokenAccount.burnTokens(proposalFee, "제안 수수료: " + proposal.getTitle());
                tokenAccountRepository.save(tokenAccount);
            }
            
            // 4. 블록체인 트랜잭션 기록
            Transaction transaction = new Transaction(
                proposal.getCreatorId(),
                BlockchainTransactionType.PROPOSAL_CREATE,
                networkTypeEnum,
                proposalFee,
                creatorWalletAddress,
                null, // 제안 생성은 toAddress 없음
                "거버넌스 제안 생성: " + proposal.getTitle()
            );
            transaction.confirm(transactionHash);
            transactionRepository.save(transaction);
            
            return transactionHash;
            
        } catch (Exception e) {
            System.err.println("=== ❌ 블록체인 브로드캐스트 실패 ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
    
    /**
     * 투표권 위임
     */
    public String delegateVotes(
            String delegateeWalletAddress,
            String networkType) {
        
        try {
            NetworkType networkTypeEnum = NetworkType.valueOf(networkType.toUpperCase());
            AdminWalletService.AdminWalletInfo adminWallet = AdminWalletService.getAdminWallet(networkTypeEnum);

            // TransactionOrchestrator를 통해 위임 트랜잭션 실행
            TransactionResult txResult = transactionOrchestrator.executeDelegationCreation(
                adminWallet.getWalletAddress(),
                delegateeWalletAddress,
                networkTypeEnum
            );

            if (!txResult.isSuccess()) {
                throw new RuntimeException("Failed to delegate votes: " + txResult.getErrorMessage());
            }
            
            String transactionHash = txResult.getTransactionHash();
            System.out.println("✅ 투표권 위임 성공!");
            System.out.println("위임 트랜잭션 해시: " + transactionHash);
            System.out.println("=== 🎯 투표권 위임 완료 ===");
            
            return transactionHash;
            
        } catch (Exception e) {
            System.err.println("=== ❌ 투표권 위임 실패 ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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