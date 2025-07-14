package com.bloominggrace.governance.governance.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.*;
import com.bloominggrace.governance.governance.infrastructure.repository.ProposalRepository;
import com.bloominggrace.governance.governance.infrastructure.repository.VoteRepository;
import com.bloominggrace.governance.token.application.service.TokenApplicationService;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GovernanceApplicationService {
    
    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final TokenApplicationService tokenApplicationService;
    private final WalletApplicationService walletApplicationService;
    
    public GovernanceApplicationService(
            ProposalRepository proposalRepository,
            VoteRepository voteRepository,
            TokenApplicationService tokenApplicationService,
            WalletApplicationService walletApplicationService) {
        this.proposalRepository = proposalRepository;
        this.voteRepository = voteRepository;
        this.tokenApplicationService = tokenApplicationService;
        this.walletApplicationService = walletApplicationService;
    }
    
    public Proposal createProposal(
            UserId creatorId,
            String title,
            String description,
            LocalDateTime votingStartDate,
            LocalDateTime votingEndDate,
            long requiredQuorum) {
        
        VotingPeriod votingPeriod = new VotingPeriod(votingStartDate, votingEndDate);
        Proposal proposal = new Proposal(creatorId, title, description, votingPeriod, requiredQuorum);
        
        return proposalRepository.save(proposal);
    }
    
    public void activateProposal(ProposalId proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        proposal.activate();
        proposalRepository.save(proposal);
    }
    
    public void startVoting(ProposalId proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        proposal.startVoting();
        proposalRepository.save(proposal);
    }
    
    public void vote(
            ProposalId proposalId,
            UserId voterId,
            VoteType voteType,
            String reason) {
        
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        if (!proposal.canVote()) {
            throw new IllegalStateException("Voting is not active for this proposal");
        }
        
        if (voteRepository.existsByProposalIdAndVoterId(proposalId, voterId)) {
            throw new IllegalStateException("User has already voted on this proposal");
        }
        
        // 토큰 잔액을 기반으로 투표권 계산
        long votingPower = calculateVotingPower(voterId);
        
        // 투표 기록 생성
        Vote vote = new Vote(proposalId, voterId, voteType, votingPower, reason);
        voteRepository.save(vote);
        
        // 제안서에 투표 추가
        proposal.addVote(voteType, votingPower);
        proposalRepository.save(proposal);
    }
    
    public void endVoting(ProposalId proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        proposal.endVoting();
        proposalRepository.save(proposal);
    }
    
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
    
    public void updateProposal(
            ProposalId proposalId,
            String title,
            String description) {
        
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        if (title != null) {
            proposal.updateTitle(title);
        }
        if (description != null) {
            proposal.updateDescription(description);
        }
        
        proposalRepository.save(proposal);
    }
    
    private long calculateVotingPower(UserId userId) {
        // 토큰 잔액을 기반으로 투표권 계산
        // 실제 구현에서는 스테이킹된 토큰 수량을 기준으로 계산
        var tokenAccount = tokenApplicationService.getTokenAccount(userId);
        if (tokenAccount.isPresent()) {
            return tokenAccount.get().getBalance().getStakedBalance().longValue();
        }
        return 0;
    }
    
    /**
     * 투표 트랜잭션 서명용 payload 생성
     */
    public byte[] createVoteSignPayload(ProposalId proposalId, UserId voterId, VoteType voteType, long votingPower, String reason) {
        // 간단히 proposalId, voterId, voteType, votingPower, reason을 concat하여 바이트 배열로 만듦
        String payload = proposalId.getId().toString() + ":" +
                         voterId.getValue().toString() + ":" +
                         voteType.name() + ":" +
                         votingPower + ":" +
                         (reason == null ? "" : reason);
        return payload.getBytes();
    }

    /**
     * 투표 트랜잭션에 서명 (WalletApplicationService를 통해 데이터베이스에서 개인키 로드)
     */
    public byte[] signVoteTransaction(String walletAddress, String networkType, byte[] payload) {
        NetworkType networkTypeEnum = NetworkType.valueOf(networkType.toUpperCase());
        return walletApplicationService.signMessage(walletAddress, payload, networkTypeEnum);
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