package com.bloominggrace.governance.governance.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.Proposal;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProposalRepository {
    Proposal save(Proposal proposal);
    Optional<Proposal> findById(ProposalId id);
    List<Proposal> findByCreatorId(UserId creatorId);
    List<Proposal> findByStatus(ProposalStatus status);
    List<Proposal> findByVotingPeriodEndBefore(LocalDateTime endDate);
    List<Proposal> findAll();
    void delete(ProposalId id);
    boolean existsById(ProposalId id);
} 