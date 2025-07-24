package com.bloominggrace.governance.governance.application.service;

import com.bloominggrace.governance.shared.blockchain.domain.constants.EthereumConstants;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.*;
import com.bloominggrace.governance.governance.infrastructure.repository.ProposalRepository;
import com.bloominggrace.governance.governance.infrastructure.repository.VoteRepository;
import com.bloominggrace.governance.governance.application.dto.*;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.token.application.service.TokenApplicationService;
import com.bloominggrace.governance.shared.blockchain.domain.model.BlockchainTransactionType;
import com.bloominggrace.governance.shared.blockchain.domain.model.Transaction;
import com.bloominggrace.governance.shared.blockchain.infrastructure.repository.TransactionRepository;
import com.bloominggrace.governance.shared.blockchain.infrastructure.service.TransactionOrchestrator;
import com.bloominggrace.governance.shared.blockchain.infrastructure.service.TransactionOrchestrator.TransactionResult;
import com.bloominggrace.governance.shared.security.infrastructure.service.AdminWalletService;
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
    private final AdminWalletService adminWalletService;
    
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
            
            // 2. 제안서 생성 (트랜잭션 해시 없이)
            VotingPeriod votingPeriod = new VotingPeriod(votingStartDate, votingEndDate);
            Proposal proposal = new Proposal(creatorId, title, description, votingPeriod, requiredQuorum);
            
            // 제안자 지갑 주소 설정
            proposal.setCreatorWalletAddress(creatorWalletAddress);
            proposal = proposalRepository.save(proposal);

            // 3. 블록체인에 거버넌스 제안 트랜잭션 실행
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
            System.out.println("✅ 블록체인 제안 생성 성공!");
            System.out.println("제안 트랜잭션 해시: " + transactionHash);
            
            // 4. 제안서에 트랜잭션 해시 설정 (별도 필드에 저장)
            proposal.setTxHash(transactionHash);
            proposalRepository.save(proposal);
            
            // 5. 제안자의 토큰 계정에서 수수료 차감 (제안 수수료가 있는 경우)
            if (proposalFee.compareTo(BigDecimal.ZERO) > 0) {
                tokenAccount.transferTokens(proposalFee, "거버넌스 제안 수수료: " + title);
                tokenAccountRepository.save(tokenAccount);
            }
            
            // 6. 트랜잭션 기록
            Transaction transaction = new Transaction(
                creatorId,
                BlockchainTransactionType.PROPOSAL_CREATE,
                networkTypeEnum,
                proposalFee,
                creatorWalletAddress,
                null,
                "거버넌스 제안 생성: " + title
            );
            transaction.confirm(transactionHash);
            transactionRepository.save(transaction);
            
            System.out.println("=== 🎯 제안 생성 완료 ===");
            System.out.println("제안 ID: " + proposal.getId().getValue());
            System.out.println("트랜잭션 해시: " + transactionHash);
            
            return ProposalDto.from(proposal);
            
        } catch (Exception e) {
            System.err.println("=== ❌ 제안 생성 실패 ===");
            System.err.println("Error: " + e.getMessage());
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
            AdminWalletService.AdminWalletInfo adminWallet = adminWalletService.getAdminWallet(networkTypeEnum);

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
            proposal.setTxHash(transactionHash);
            proposal.setProposalCount(adminWalletService.getProposalCount(networkTypeEnum));
            adminWalletService.getNextProposalId(networkTypeEnum);
            
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
    
    /**
     * 제안 상세 정보 조회 (투표 현황 포함)
     */
    @Transactional(readOnly = true)
    public ProposalDetailResponse getProposalDetail(ProposalId proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId.getValue()));
        
        List<Vote> votes = voteRepository.findByProposalId(proposalId);
        
        // TODO: 트랜잭션 해시와 네트워크 타입 조회 로직 추가
        String transactionHash = null; // 실제 구현에서는 트랜잭션 테이블에서 조회
        String networkType = "ETHEREUM"; // 실제 구현에서는 제안자 지갑에서 조회
        
        return ProposalDetailResponse.from(proposal, votes, transactionHash, networkType);
    }
    
    /**
     * 블록체인 투표 실행 (TransactionOrchestrator 사용)
     */
    public CastVoteResponse castVoteWithTransaction(
            ProposalId proposalId,
            UserId voterId,
            VoteType voteType,
            String reason,
            String voterWalletAddress,
            String networkType) {
        
        try {
            // 1. 제안 유효성 검증
            Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
            
            if (!proposal.canVote()) {
                throw new IllegalStateException("Voting is not active for this proposal");
            }
            
            if (voteRepository.existsByProposalIdAndVoterId(proposalId, voterId)) {
                throw new IllegalStateException("User has already voted on this proposal");
            }
            
            // 2. 투표자 토큰 잔액 확인 - 실제 컨트랙트 주소 사용
            NetworkType networkTypeEnum = determineNetworkType(voterWalletAddress);
            TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
                voterId, voterWalletAddress, networkTypeEnum, EthereumConstants.Contracts.ERC20_CONTRACT_ADDRESS, "TOKEN");
            
            BigDecimal votingPower = tokenAccount.getAvailableBalance();
            if (votingPower.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("투표에 필요한 토큰이 없습니다. 현재 잔액: " + votingPower);
            }
            
            // 3. 블록체인 투표 트랜잭션 실행
            System.out.println("=== 🗳️ 블록체인 투표 트랜잭션 시작 ===");
            System.out.println("제안 ID: " + proposalId.getValue());
            System.out.println("투표자: " + voterWalletAddress);
            System.out.println("투표 타입: " + voteType.name());
            System.out.println("투표권: " + votingPower);
            System.out.println("투표 proposalCount: " + proposal.getProposalCount());

            TransactionResult txResult = transactionOrchestrator.executeVoteCreation(
                proposal.getProposalCount(),
                proposalId.getValue(),
                voterWalletAddress,
                voteType.name(),
                votingPower,
                reason,
                networkTypeEnum
            );
            
            if (!txResult.isSuccess()) {
                System.err.println("❌ 블록체인 투표 실패: " + txResult.getErrorMessage());
                return CastVoteResponse.failure(
                    proposalId.getValue(),
                    voterId.getValue(),
                    voteType.name(),
                    txResult.getErrorMessage()
                );
            }
            
            String transactionHash = txResult.getTransactionHash();
            System.out.println("✅ 블록체인 투표 성공!");
            System.out.println("투표 트랜잭션 해시: " + transactionHash);
            
            // 4. 투표 기록 생성
            Vote vote = new Vote(proposalId, voterId, voteType, votingPower.longValue(), reason);
            vote = voteRepository.save(vote);
            
            // 5. 제안서에 투표 추가
            proposal.addVote(voteType, votingPower.longValue());
            proposalRepository.save(proposal);
            
            // 6. 투표 트랜잭션 기록
            Transaction transaction = new Transaction(
                tokenAccount.getUserId(),
                BlockchainTransactionType.PROPOSAL_VOTE,
                networkTypeEnum,
                votingPower,
                voterWalletAddress,
                null,
                "투표: " + proposal.getTitle() + " - " + voteType.name() + " (투표권: " + votingPower + ")"
            );
            transaction.confirm(transactionHash);
            transactionRepository.save(transaction);
            
            System.out.println("=== 🎯 투표 완료 ===");
            
            return CastVoteResponse.success(
                vote.getId().getValue(),
                proposalId.getValue(),
                voterId.getValue(),
                voteType.name(),
                votingPower,
                reason,
                transactionHash
            );
            
        } catch (Exception e) {
            System.err.println("=== ❌ 투표 실패 ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            return CastVoteResponse.failure(
                proposalId.getValue(),
                voterId.getValue(),
                voteType.name(),
                e.getMessage()
            );
        }
    }
    
    /**
     * 활성 제안 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ProposalDetailResponse> getActiveProposals() {
        List<Proposal> activeProposals = proposalRepository.findByStatus(ProposalStatus.ACTIVE);
        
        return activeProposals.stream()
            .map(proposal -> {
                List<Vote> votes = voteRepository.findByProposalId(proposal.getId());
                return ProposalDetailResponse.from(proposal, votes, null, "ETHEREUM");
            })
            .toList();
    }
    
    /**
     * 사용자의 투표 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean canUserVote(ProposalId proposalId, UserId voterId, String voterWalletAddress, String networkType) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        if (!proposal.canVote()) {
            return false;
        }
        
        if (voteRepository.existsByProposalIdAndVoterId(proposalId, voterId)) {
            return false;
        }
        
        // 토큰 잔액 확인 추가 - 실제 토큰 컨트랙트 주소 사용
        try {
            NetworkType networkTypeEnum = determineNetworkType(voterWalletAddress);
            TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
                voterId, voterWalletAddress, networkTypeEnum, EthereumConstants.Contracts.ERC20_CONTRACT_ADDRESS, "TOKEN");
            
            BigDecimal votingPower = tokenAccount.getAvailableBalance();
            if (votingPower.compareTo(BigDecimal.ZERO) <= 0) {
                return false; // 토큰이 없으면 투표 불가
            }
        } catch (Exception e) {
            // log.warn("Failed to check voting power for user {}: {}", voterId.getValue(), e.getMessage()); // Original code had this line commented out
            return false;
        }
        
        return true;
    }
    
    /**
     * 사용자의 투표 파워 조회
     */
    @Transactional(readOnly = true)
    public BigDecimal getUserVotingPower(UserId voterId, String voterWalletAddress, String networkType) {
        NetworkType networkTypeEnum = determineNetworkType(voterWalletAddress);
        // 실제 토큰 컨트랙트 주소 사용
        TokenAccount tokenAccount = tokenApplicationService.getOrCreateTokenAccount(
            voterId, voterWalletAddress, networkTypeEnum, "0xeafF00556BC06464511319dAb26D6CAC148b89d0", "TOKEN");
        
        return tokenAccount.getAvailableBalance();
    }

    /**
     * 제안 저장
     */
    public Proposal saveProposal(Proposal proposal) {
        return proposalRepository.save(proposal);
    }

    // ===== 유틸리티 메서드들 =====
    private NetworkType determineNetworkType(String walletAddress) {
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