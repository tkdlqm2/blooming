package com.bloominggrace.governance.governance.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.domain.model.Vote;
import com.bloominggrace.governance.governance.domain.model.VoteId;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.VoteType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VoteRepositoryAdapter implements VoteRepository {
    
    private final VoteJpaRepository jpaRepository;
    
    public VoteRepositoryAdapter(VoteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Vote save(Vote vote) {
        return jpaRepository.save(vote);
    }
    
    @Override
    public Optional<Vote> findById(VoteId id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<Vote> findByProposalId(ProposalId proposalId) {
        return jpaRepository.findByProposalId(proposalId);
    }
    
    @Override
    public List<Vote> findByVoterId(UserId voterId) {
        return jpaRepository.findByVoterId(voterId);
    }
    
    @Override
    public List<Vote> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public void delete(VoteId id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByProposalIdAndVoterId(ProposalId proposalId, UserId voterId) {
        return jpaRepository.existsByProposalIdAndVoterId(proposalId, voterId);
    }
} 