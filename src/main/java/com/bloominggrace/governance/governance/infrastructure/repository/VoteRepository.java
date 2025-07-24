package com.bloominggrace.governance.governance.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.Vote;
import com.bloominggrace.governance.governance.domain.model.VoteId;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.VoteType;

import java.util.List;
import java.util.Optional;

public interface VoteRepository {
    Vote save(Vote vote);
    Optional<Vote> findById(VoteId id);
    List<Vote> findByProposalId(ProposalId proposalId);
    List<Vote> findByVoterId(UserId voterId);
    List<Vote> findAll();
    void delete(VoteId id);
    boolean existsByProposalIdAndVoterId(ProposalId proposalId, UserId voterId);
} 