package com.bloominggrace.governance.governance.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.Proposal;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProposalJpaRepository extends JpaRepository<Proposal, ProposalId> {
    List<Proposal> findByCreatorId(UserId creatorId);
    List<Proposal> findByStatus(ProposalStatus status);
    
    @Query("SELECT p FROM Proposal p WHERE p.votingPeriod.endDate < :endDate")
    List<Proposal> findByVotingPeriodEndBefore(@Param("endDate") LocalDateTime endDate);
} 