package com.bloominggrace.governance.governance.infrastructure.controller;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.application.dto.ProposalDto;
import com.bloominggrace.governance.governance.application.dto.VoteDto;
import com.bloominggrace.governance.governance.application.dto.CreateProposalRequest;
import com.bloominggrace.governance.governance.application.dto.VoteRequest;
import com.bloominggrace.governance.governance.application.service.GovernanceApplicationService;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;
import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.governance.domain.model.Proposal;
import com.bloominggrace.governance.governance.domain.model.Vote;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/governance")
public class GovernanceController {
    
    private final GovernanceApplicationService governanceService;
    
    public GovernanceController(GovernanceApplicationService governanceService) {
        this.governanceService = governanceService;
    }
    
    // ===== 거버넌스 관련 엔드포인트 =====
    
    @PostMapping("/proposals")
    public ResponseEntity<ProposalDto> createProposal(@RequestBody CreateProposalRequest request) {
        UserId creatorIdObj = new UserId(request.getCreatorId());
        
        ProposalDto proposalDto = governanceService.createProposalWithTransaction(
            creatorIdObj, 
            request.getTitle(), 
            request.getDescription(), 
            request.getVotingStartDate(), 
            request.getVotingEndDate(), 
            request.getRequiredQuorum().getAmount().longValue(),
            request.getCreatorWalletAddress(),
            request.getProposalFee().getAmount(),
            request.getNetworkType().name()
        );
        
        return ResponseEntity.ok(proposalDto);
    }
    
    @PostMapping("/proposals/{proposalId}/vote")
    public ResponseEntity<VoteDto> vote(@PathVariable UUID proposalId, @RequestBody VoteRequest request) {
        ProposalId proposalIdObj = new ProposalId(proposalId);
        UserId voterIdObj = new UserId(request.getVoterId());
        VoteType voteTypeEnum = request.getVoteType();
        
        VoteDto voteDto = governanceService.voteWithSnapshot(
            proposalIdObj, 
            voterIdObj, 
            voteTypeEnum, 
            request.getReason(),
            request.getVoterWalletAddress(),
            request.getNetworkType().name()
        );
        
        return ResponseEntity.ok(voteDto);
    }
    
    @GetMapping("/proposals/{proposalId}")
    public ResponseEntity<ProposalDto> getProposal(@PathVariable UUID proposalId) {
        ProposalId proposalIdObj = new ProposalId(proposalId);
        Proposal proposal = governanceService.getProposal(proposalIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        return ResponseEntity.ok(ProposalDto.from(proposal));
    }
    
    @GetMapping("/proposals/status/{status}")
    public ResponseEntity<List<ProposalDto>> getProposalsByStatus(@PathVariable String status) {
        ProposalStatus proposalStatus = ProposalStatus.valueOf(status.toUpperCase());
        List<Proposal> proposals = governanceService.getProposalsByStatus(proposalStatus);
        
        List<ProposalDto> proposalDtos = proposals.stream()
            .map(ProposalDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(proposalDtos);
    }
    
    @GetMapping("/proposals/creator/{creatorId}")
    public ResponseEntity<List<ProposalDto>> getProposalsByCreator(@PathVariable UUID creatorId) {
        UserId creatorIdObj = new UserId(creatorId);
        List<Proposal> proposals = governanceService.getProposalsByCreator(creatorIdObj);
        
        List<ProposalDto> proposalDtos = proposals.stream()
            .map(ProposalDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(proposalDtos);
    }
    
    @GetMapping("/votes/proposal/{proposalId}")
    public ResponseEntity<List<VoteDto>> getVotesByProposal(@PathVariable UUID proposalId) {
        ProposalId proposalIdObj = new ProposalId(proposalId);
        List<Vote> votes = governanceService.getVotesByProposal(proposalIdObj);
        
        List<VoteDto> voteDtos = votes.stream()
            .map(VoteDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(voteDtos);
    }
    
    @GetMapping("/votes/voter/{voterId}")
    public ResponseEntity<List<VoteDto>> getVotesByVoter(@PathVariable UUID voterId) {
        UserId voterIdObj = new UserId(voterId);
        List<Vote> votes = governanceService.getVotesByVoter(voterIdObj);
        
        List<VoteDto> voteDtos = votes.stream()
            .map(VoteDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(voteDtos);
    }
    

    
    // ===== DTO 클래스들 =====
} 