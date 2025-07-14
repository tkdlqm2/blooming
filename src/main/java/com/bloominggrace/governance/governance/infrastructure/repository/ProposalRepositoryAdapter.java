package com.bloominggrace.governance.governance.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.Proposal;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ProposalRepositoryAdapter implements ProposalRepository {
    
    private final ProposalJpaRepository jpaRepository;
    
    public ProposalRepositoryAdapter(ProposalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Proposal save(Proposal proposal) {
        return jpaRepository.save(proposal);
    }
    
    @Override
    public Optional<Proposal> findById(ProposalId id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<Proposal> findByCreatorId(UserId creatorId) {
        return jpaRepository.findByCreatorId(creatorId);
    }
    
    @Override
    public List<Proposal> findByStatus(ProposalStatus status) {
        return jpaRepository.findByStatus(status);
    }
    
    @Override
    public List<Proposal> findByVotingPeriodEndBefore(LocalDateTime endDate) {
        return jpaRepository.findByVotingPeriodEndBefore(endDate);
    }
    
    @Override
    public List<Proposal> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public void delete(ProposalId id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(ProposalId id) {
        return jpaRepository.existsById(id);
    }
} 