package com.bloominggrace.governance.governance.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.Vote;
import com.bloominggrace.governance.governance.domain.model.VoteId;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteJpaRepository extends JpaRepository<Vote, VoteId> {
    List<Vote> findByProposalId(ProposalId proposalId);
    List<Vote> findByVoterId(UserId voterId);
    Optional<Vote> findByProposalIdAndVoterId(ProposalId proposalId, UserId voterId);
    List<Vote> findByProposalIdAndVoteType(ProposalId proposalId, VoteType voteType);
    boolean existsByProposalIdAndVoterId(ProposalId proposalId, UserId voterId);
} 