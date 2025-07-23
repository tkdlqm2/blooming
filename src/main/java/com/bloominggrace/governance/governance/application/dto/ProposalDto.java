package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.Proposal;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;
import com.bloominggrace.governance.governance.domain.model.VoteResults;
import com.bloominggrace.governance.governance.domain.model.VotingPeriod;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProposalDto {
    private final UUID id;
    private final UUID creatorId;
    private final String title;
    private final String description;
    private final String status;
    private final VotingPeriodDto votingPeriod;
    private final VoteResultsDto voteResults;
    private final long requiredQuorum;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String txHash;
    private final String creatorWalletAddress;

    public ProposalDto(
            UUID id,
            UUID creatorId,
            String title,
            String description,
            String status,
            VotingPeriodDto votingPeriod,
            VoteResultsDto voteResults,
            long requiredQuorum,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String txHash,
            String creatorWalletAddress) {
        this.id = id;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.votingPeriod = votingPeriod;
        this.voteResults = voteResults;
        this.requiredQuorum = requiredQuorum;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.txHash = txHash;
        this.creatorWalletAddress = creatorWalletAddress;
    }

    public static ProposalDto from(Proposal proposal) {
        return ProposalDto.builder()
            .id(proposal.getId().getValue())
            .creatorId(proposal.getCreatorId() != null ? proposal.getCreatorId().getValue() : null)
            .title(proposal.getTitle())
            .description(proposal.getDescription())
            .status(proposal.getStatus().name())
            .votingPeriod(VotingPeriodDto.from(proposal.getVotingPeriod()))
            .voteResults(VoteResultsDto.from(proposal.getVoteResults()))
            .requiredQuorum(proposal.getRequiredQuorum())
            .createdAt(proposal.getCreatedAt())
            .updatedAt(proposal.getUpdatedAt())
            .txHash(proposal.getTxHash())
            .creatorWalletAddress(proposal.getCreatorWalletAddress())
            .build();
    }
    
    public static ProposalDto from(Proposal proposal, String transactionSignature, String creatorWalletAddress) {
        return ProposalDto.builder()
            .id(proposal.getId().getValue())
            .creatorId(proposal.getCreatorId() != null ? proposal.getCreatorId().getValue() : null)
            .title(proposal.getTitle())
            .description(proposal.getDescription())
            .status(proposal.getStatus().name())
            .votingPeriod(VotingPeriodDto.from(proposal.getVotingPeriod()))
            .voteResults(VoteResultsDto.from(proposal.getVoteResults()))
            .requiredQuorum(proposal.getRequiredQuorum())
            .createdAt(proposal.getCreatedAt())
            .updatedAt(proposal.getUpdatedAt())
            .txHash(transactionSignature)
            .creatorWalletAddress(creatorWalletAddress)
            .build();
    }
    
    public static ProposalDtoBuilder builder() {
        return new ProposalDtoBuilder();
    }
    
    public static class ProposalDtoBuilder {
        private UUID id;
        private UUID creatorId;
        private String title;
        private String description;
        private String status;
        private VotingPeriodDto votingPeriod;
        private VoteResultsDto voteResults;
        private long requiredQuorum;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String txHash;
        private String creatorWalletAddress;
        
        public ProposalDtoBuilder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public ProposalDtoBuilder creatorId(UUID creatorId) {
            this.creatorId = creatorId;
            return this;
        }
        
        public ProposalDtoBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public ProposalDtoBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public ProposalDtoBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public ProposalDtoBuilder votingPeriod(VotingPeriodDto votingPeriod) {
            this.votingPeriod = votingPeriod;
            return this;
        }
        
        public ProposalDtoBuilder voteResults(VoteResultsDto voteResults) {
            this.voteResults = voteResults;
            return this;
        }
        
        public ProposalDtoBuilder requiredQuorum(long requiredQuorum) {
            this.requiredQuorum = requiredQuorum;
            return this;
        }
        
        public ProposalDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public ProposalDtoBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public ProposalDtoBuilder txHash(String txHash) {
            this.txHash = txHash;
            return this;
        }
        
        public ProposalDtoBuilder creatorWalletAddress(String creatorWalletAddress) {
            this.creatorWalletAddress = creatorWalletAddress;
            return this;
        }
        
        public ProposalDto build() {
            return new ProposalDto(id, creatorId, title, description, status, votingPeriod, voteResults, requiredQuorum, createdAt, updatedAt, txHash, creatorWalletAddress);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public VotingPeriodDto getVotingPeriod() {
        return votingPeriod;
    }

    public VoteResultsDto getVoteResults() {
        return voteResults;
    }

    public long getRequiredQuorum() {
        return requiredQuorum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public String getTxHash() {
        return txHash;
    }
    
    public String getCreatorWalletAddress() {
        return creatorWalletAddress;
    }
} 