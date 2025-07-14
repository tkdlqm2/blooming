package com.bloominggrace.governance.governance.infrastructure.controller;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.application.dto.ProposalDto;
import com.bloominggrace.governance.governance.application.dto.VoteDto;
import com.bloominggrace.governance.governance.application.service.GovernanceApplicationService;
import com.bloominggrace.governance.governance.domain.model.ProposalId;
import com.bloominggrace.governance.governance.domain.model.ProposalStatus;
import com.bloominggrace.governance.governance.domain.model.VoteType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    
    @PostMapping("/proposals")
    public ResponseEntity<ProposalDto> createProposal(
            @RequestParam UUID creatorId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String votingStartDate,
            @RequestParam String votingEndDate,
            @RequestParam long requiredQuorum) {
        
        UserId creatorIdObj = new UserId(creatorId);
        LocalDateTime startDate = LocalDateTime.parse(votingStartDate);
        LocalDateTime endDate = LocalDateTime.parse(votingEndDate);
        
        var proposal = governanceService.createProposal(
            creatorIdObj, title, description, startDate, endDate, requiredQuorum
        );
        
        return ResponseEntity.ok(ProposalDto.from(proposal));
    }
    
    @PostMapping("/proposals/{proposalId}/activate")
    public ResponseEntity<Void> activateProposal(@PathVariable UUID proposalId) {
        ProposalId proposalIdObj = new ProposalId(proposalId);
        governanceService.activateProposal(proposalIdObj);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/proposals/{proposalId}/start-voting")
    public ResponseEntity<Void> startVoting(@PathVariable UUID proposalId) {
        ProposalId proposalIdObj = new ProposalId(proposalId);
        governanceService.startVoting(proposalIdObj);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/proposals/{proposalId}/vote")
    public ResponseEntity<Void> vote(
            @PathVariable UUID proposalId,
            @RequestParam UUID voterId,
            @RequestParam String voteType,
            @RequestParam(required = false) String reason) {
        
        ProposalId proposalIdObj = new ProposalId(proposalId);
        UserId voterIdObj = new UserId(voterId);
        VoteType voteTypeEnum = VoteType.valueOf(voteType.toUpperCase());
        
        governanceService.vote(proposalIdObj, voterIdObj, voteTypeEnum, reason);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/proposals/{proposalId}/end-voting")
    public ResponseEntity<Void> endVoting(@PathVariable UUID proposalId) {
        ProposalId proposalIdObj = new ProposalId(proposalId);
        governanceService.endVoting(proposalIdObj);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/proposals/{proposalId}")
    public ResponseEntity<ProposalDto> getProposal(@PathVariable UUID proposalId) {
        ProposalId proposalIdObj = new ProposalId(proposalId);
        var proposal = governanceService.getProposal(proposalIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        return ResponseEntity.ok(ProposalDto.from(proposal));
    }
    
    @GetMapping("/proposals/status/{status}")
    public ResponseEntity<List<ProposalDto>> getProposalsByStatus(@PathVariable String status) {
        ProposalStatus proposalStatus = ProposalStatus.valueOf(status.toUpperCase());
        var proposals = governanceService.getProposalsByStatus(proposalStatus);
        
        List<ProposalDto> proposalDtos = proposals.stream()
            .map(ProposalDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(proposalDtos);
    }
    
    @GetMapping("/proposals/creator/{creatorId}")
    public ResponseEntity<List<ProposalDto>> getProposalsByCreator(@PathVariable UUID creatorId) {
        UserId creatorIdObj = new UserId(creatorId);
        var proposals = governanceService.getProposalsByCreator(creatorIdObj);
        
        List<ProposalDto> proposalDtos = proposals.stream()
            .map(ProposalDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(proposalDtos);
    }
    
    @GetMapping("/proposals/{proposalId}/votes")
    public ResponseEntity<List<VoteDto>> getVotesByProposal(@PathVariable UUID proposalId) {
        ProposalId proposalIdObj = new ProposalId(proposalId);
        var votes = governanceService.getVotesByProposal(proposalIdObj);
        
        List<VoteDto> voteDtos = votes.stream()
            .map(VoteDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(voteDtos);
    }
    
    @GetMapping("/votes/voter/{voterId}")
    public ResponseEntity<List<VoteDto>> getVotesByVoter(@PathVariable UUID voterId) {
        UserId voterIdObj = new UserId(voterId);
        var votes = governanceService.getVotesByVoter(voterIdObj);
        
        List<VoteDto> voteDtos = votes.stream()
            .map(VoteDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(voteDtos);
    }
    
    @GetMapping("/proposals/{proposalId}/votes/{voterId}")
    public ResponseEntity<VoteDto> getVoteByProposalAndVoter(
            @PathVariable UUID proposalId,
            @PathVariable UUID voterId) {
        
        ProposalId proposalIdObj = new ProposalId(proposalId);
        UserId voterIdObj = new UserId(voterId);
        var vote = governanceService.getVoteByProposalAndVoter(proposalIdObj, voterIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Vote not found"));
        
        return ResponseEntity.ok(VoteDto.from(vote));
    }
    
    @PutMapping("/proposals/{proposalId}")
    public ResponseEntity<Void> updateProposal(
            @PathVariable UUID proposalId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description) {
        
        ProposalId proposalIdObj = new ProposalId(proposalId);
        governanceService.updateProposal(proposalIdObj, title, description);
        return ResponseEntity.ok().build();
    }
} 