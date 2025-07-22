package com.bloominggrace.governance.governance.infrastructure.controller;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.governance.application.dto.CreateProposalRequest;
import com.bloominggrace.governance.governance.application.dto.ProposalDto;
import com.bloominggrace.governance.governance.application.dto.VoteDto;
import com.bloominggrace.governance.governance.application.dto.ChargeProposalFeeRequest;
import com.bloominggrace.governance.governance.application.dto.BroadcastProposalRequest;
import com.bloominggrace.governance.governance.application.dto.VoteRequest;
import com.bloominggrace.governance.governance.application.dto.DelegateVotesRequest;
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
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/governance")
public class GovernanceController {
    
    private final GovernanceApplicationService governanceService;
    
    public GovernanceController(GovernanceApplicationService governanceService) {
        this.governanceService = governanceService;
    }
    
    // ===== 거버넌스 관련 엔드포인트 =====
    
    /**
     * 거버넌스 제안 생성 (기존 통합 방식)
     */
    @PostMapping("/proposals")
    public ResponseEntity<ProposalDto> createProposal(@RequestBody CreateProposalRequest request) {
        UserId creatorIdObj = new UserId(request.getCreatorId());
        
        ProposalDto proposalDto = governanceService.createProposalWithTransaction(
            creatorIdObj, 
            request.getTitle(), 
            request.getDescription(), 
            request.getVotingStartDate(), 
            request.getVotingEndDate(), 
            request.getRequiredQuorum().longValue(),
            request.getCreatorWalletAddress(),
            request.getProposalFee(),
            request.getNetworkType().name()
        );
        
        return ResponseEntity.ok(proposalDto);
    }
    
    /**
     * 1단계: 거버넌스 제안 저장 (블록체인 트랜잭션 없이)
     */
    @PostMapping("/proposals/save")
    public ResponseEntity<ProposalDto> saveProposal(@RequestBody CreateProposalRequest request) {
        UserId creatorIdObj = new UserId(request.getCreatorId());
        
        ProposalDto proposalDto = governanceService.createProposal(
            creatorIdObj, 
            request.getTitle(), 
            request.getDescription(), 
            request.getVotingStartDate(), 
            request.getVotingEndDate(), 
            request.getRequiredQuorum().longValue(),
            request.getCreatorWalletAddress(),
            request.getProposalFee(),
            request.getNetworkType().name()
        );
        
        return ResponseEntity.ok(proposalDto);
    }
    
    /**
     * 2단계: 거버넌스 제안 수수료 충전 (Admin에서 제안자로)
     */
    @PostMapping("/proposals/{proposalId}/charge-fee")
    public ResponseEntity<Map<String, Object>> chargeProposalFee(
            @PathVariable String proposalId,
            @RequestBody ChargeProposalFeeRequest request) {
        
        try {
            ProposalId proposalIdObj = new ProposalId(UUID.fromString(proposalId));
            
            String feeTransactionHash = governanceService.chargeProposalFee(
                proposalIdObj,
                request.getCreatorWalletAddress(),
                request.getNetworkType().name()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "수수료 충전이 성공적으로 완료되었습니다.");
            response.put("proposalId", proposalId);
            response.put("feeTransactionHash", feeTransactionHash);
            response.put("proposalFee", request.getProposalFee());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "수수료 충전 실패: " + e.getMessage());
            response.put("proposalId", proposalId);
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 3단계: 거버넌스 제안을 블록체인 네트워크로 브로드캐스트
     */
    @PostMapping("/proposals/{proposalId}/broadcast")
    public ResponseEntity<Map<String, Object>> broadcastProposal(
            @PathVariable String proposalId,
            @RequestBody BroadcastProposalRequest request) {
        
        try {
            ProposalId proposalIdObj = new ProposalId(UUID.fromString(proposalId));
            
            String governanceTransactionHash = governanceService.broadcastProposal(
                proposalIdObj,
                request.getCreatorWalletAddress(),
                request.getNetworkType().name()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "거버넌스 제안이 성공적으로 블록체인에 브로드캐스트되었습니다.");
            response.put("proposalId", proposalId);
            response.put("governanceTransactionHash", governanceTransactionHash);
            response.put("proposalFee", request.getProposalFee());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "블록체인 브로드캐스트 실패: " + e.getMessage());
            response.put("proposalId", proposalId);
            
            return ResponseEntity.badRequest().body(response);
        }
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
    
    /**
     * 투표권 위임
     */
    @PostMapping("/delegate")
    public ResponseEntity<Map<String, Object>> delegateVotes(@RequestBody DelegateVotesRequest request) {
        try {
            String delegationTransactionHash = governanceService.delegateVotes(
                request.getDelegateeWalletAddress(),
                request.getNetworkType().name()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("delegationTransactionHash", delegationTransactionHash);
            response.put("message", "투표권 위임이 성공적으로 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
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