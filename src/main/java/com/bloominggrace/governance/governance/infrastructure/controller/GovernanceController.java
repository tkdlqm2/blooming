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
import com.bloominggrace.governance.governance.application.dto.ProposalDetailResponse;
import com.bloominggrace.governance.governance.application.dto.CastVoteRequest;
import com.bloominggrace.governance.governance.application.dto.CastVoteResponse;
import com.bloominggrace.governance.governance.domain.model.VotingPeriod;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

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
    
    /**
     * 제안 활성화
     */
    @PostMapping("/proposals/{proposalId}/activate")
    public ResponseEntity<Map<String, Object>> activateProposal(@PathVariable UUID proposalId) {
        try {
            ProposalId proposalIdObj = new ProposalId(proposalId);
            Proposal proposal = governanceService.getProposal(proposalIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
            
            proposal.activate();
            governanceService.saveProposal(proposal);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("proposalId", proposalId);
            response.put("status", proposal.getStatus().name());
            response.put("message", "제안이 성공적으로 활성화되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("proposalId", proposalId);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 제안 투표 시작
     */
    @PostMapping("/proposals/{proposalId}/start-voting")
    public ResponseEntity<Map<String, Object>> startVoting(@PathVariable UUID proposalId) {
        try {
            ProposalId proposalIdObj = new ProposalId(proposalId);
            Proposal proposal = governanceService.getProposal(proposalIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
            
            proposal.startVoting();
            governanceService.saveProposal(proposal);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("proposalId", proposalId);
            response.put("status", proposal.getStatus().name());
            response.put("message", "제안 투표가 성공적으로 시작되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("proposalId", proposalId);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 제안 투표 기간 수정 (현재 시간으로 시작)
     */
    @PostMapping("/proposals/{proposalId}/update-voting-period")
    public ResponseEntity<Map<String, Object>> updateVotingPeriod(@PathVariable UUID proposalId) {
        try {
            ProposalId proposalIdObj = new ProposalId(proposalId);
            Proposal proposal = governanceService.getProposal(proposalIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
            
            // 현재 시간부터 7일 후까지로 투표 기간 설정
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = now.plusDays(7);
            
            // 새로운 VotingPeriod 생성
            VotingPeriod newVotingPeriod = new VotingPeriod(now, endDate);
            proposal.updateVotingPeriod(newVotingPeriod);
            
            governanceService.saveProposal(proposal);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("proposalId", proposalId);
            response.put("newStartDate", now.toString());
            response.put("newEndDate", endDate.toString());
            response.put("message", "투표 기간이 현재 시간으로 수정되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("proposalId", proposalId);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 심플한 투표 진행 API (DRAFT -> ACTIVE -> VOTING -> 투표 실행)
     */
    @PostMapping("/proposals/{proposalId}/quick-vote")
    public ResponseEntity<Map<String, Object>> quickVote(
            @PathVariable UUID proposalId,
            @RequestBody CastVoteRequest request) {
        
        try {
            ProposalId proposalIdObj = new ProposalId(proposalId);
            UserId voterIdObj = new UserId(request.getVoterId());
            
            // 1. 제안 상태 확인 및 자동 처리
            Proposal proposal = governanceService.getProposal(proposalIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
            
            // 2. DRAFT -> ACTIVE
            if (proposal.getStatus() == ProposalStatus.DRAFT) {
                proposal.activate();
                governanceService.saveProposal(proposal);
            }
            
            // 3. 투표 기간을 현재 시간으로 수정
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = now.plusDays(7);
            VotingPeriod newVotingPeriod = new VotingPeriod(now, endDate);
            proposal.updateVotingPeriod(newVotingPeriod);
            
            // 4. ACTIVE -> VOTING
            if (proposal.getStatus() == ProposalStatus.ACTIVE) {
                proposal.startVoting();
            }
            
            governanceService.saveProposal(proposal);
            
            // 5. 투표 실행
            CastVoteResponse voteResponse = governanceService.castVoteWithTransaction(
                proposalIdObj,
                voterIdObj,
                request.getVoteType(),
                request.getReason(),
                request.getVoterWalletAddress(),
                request.getNetworkType().name()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("proposalId", proposalId);
            response.put("finalStatus", proposal.getStatus().name());
            response.put("voteResponse", voteResponse);
            response.put("message", "투표가 성공적으로 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("proposalId", proposalId);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
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
    
    /**
     * 제안 상세 정보 조회 (투표 현황 포함)
     */
    @GetMapping("/proposals/{proposalId}/detail")
    public ResponseEntity<ProposalDetailResponse> getProposalDetail(@PathVariable UUID proposalId) {
        try {
            ProposalId proposalIdObj = new ProposalId(proposalId);
            ProposalDetailResponse response = governanceService.getProposalDetail(proposalIdObj);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 활성 제안 목록 조회
     */
    @GetMapping("/proposals/active")
    public ResponseEntity<List<ProposalDetailResponse>> getActiveProposals() {
        try {
            List<ProposalDetailResponse> activeProposals = governanceService.getActiveProposals();
            return ResponseEntity.ok(activeProposals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 블록체인 투표 실행
     */
    @PostMapping("/proposals/{proposalId}/vote-blockchain")
    public ResponseEntity<CastVoteResponse> castVote(
            @PathVariable UUID proposalId,
            @RequestBody CastVoteRequest request) {
        
        try {
            ProposalId proposalIdObj = new ProposalId(proposalId);
            UserId voterIdObj = new UserId(request.getVoterId());
            
            CastVoteResponse response = governanceService.castVoteWithTransaction(
                proposalIdObj,
                voterIdObj,
                request.getVoteType(),
                request.getReason(),
                request.getVoterWalletAddress(),
                request.getNetworkType().name()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 사용자 투표 가능 여부 확인
     */
    @GetMapping("/proposals/{proposalId}/can-vote/{voterId}")
    public ResponseEntity<Map<String, Object>> canUserVote(
            @PathVariable UUID proposalId,
            @PathVariable UUID voterId,
            @RequestParam String voterWalletAddress,
            @RequestParam(defaultValue = "ETHEREUM") String networkType) {
        
        try {
            ProposalId proposalIdObj = new ProposalId(proposalId);
            UserId voterIdObj = new UserId(voterId);
            
            boolean canVote = governanceService.canUserVote(proposalIdObj, voterIdObj, voterWalletAddress, networkType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("voterId", voterId);
            response.put("proposalId", proposalId);
            response.put("canVote", canVote);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("voterId", voterId);
            response.put("proposalId", proposalId);
            response.put("canVote", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 사용자 투표 파워 조회
     */
    @GetMapping("/voting-power/{voterId}")
    public ResponseEntity<Map<String, Object>> getUserVotingPower(
            @PathVariable UUID voterId,
            @RequestParam String voterWalletAddress,
            @RequestParam String networkType) {
        
        try {
            UserId voterIdObj = new UserId(voterId);
            
            BigDecimal votingPower = governanceService.getUserVotingPower(
                voterIdObj, voterWalletAddress, networkType
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("voterId", voterId);
            response.put("voterWalletAddress", voterWalletAddress);
            response.put("networkType", networkType);
            response.put("votingPower", votingPower);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("voterId", voterId);
            response.put("voterWalletAddress", voterWalletAddress);
            response.put("networkType", networkType);
            response.put("votingPower", 0);
            response.put("error", e.getMessage());
            
            return ResponseEntity.ok(response); // 404 대신 200으로 응답하되 votingPower: 0
        }
    }

    
    // ===== DTO 클래스들 =====
} 