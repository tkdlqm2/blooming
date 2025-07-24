// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";
import "./GovernanceToken.sol";

/**
 * @title GovernanceContract
 * @dev 거버넌스 투표 시스템
 */
contract GovernanceContract is Ownable, ReentrancyGuard, Pausable {
    
    GovernanceToken public governanceToken;
    
    // 제안 구조체
    struct Proposal {
        uint256 id;
        string title;
        string description;
        address proposer;
        uint256 forVotes;
        uint256 againstVotes;
        uint256 abstainVotes;
        uint256 startBlock;
        uint256 endBlock;
        uint256 executionTime;
        bool executed;
        bool cancelled;
        mapping(address => bool) hasVoted;
        mapping(address => VoteType) votes;
    }
    
    // 투표 타입
    enum VoteType {
        Against,
        For,
        Abstain
    }
    
    // 제안 상태
    enum ProposalState {
        Pending,
        Active,
        Cancelled,
        Defeated,
        Succeeded,
        Queued,
        Expired,
        Executed
    }
    
    // 설정 변수
    uint256 public constant VOTING_DELAY = 1 days; // 1일 = 약 7200 블록 (12초 블록 기준)
    uint256 public constant VOTING_PERIOD = 7 days; // 7일 = 약 50400 블록
    uint256 public constant EXECUTION_DELAY = 2 days; // 2일 = 약 14400 블록
    uint256 public constant PROPOSAL_THRESHOLD = 1000 * 10**18; // 1000 토큰 필요
    uint256 public constant QUORUM_PERCENTAGE = 4; // 4% 쿼럼
    
    // 상태 변수
    uint256 public proposalCount;
    mapping(uint256 => Proposal) public proposals;
    mapping(address => uint256) public latestProposalIds;
    
    // 이벤트
    event ProposalCreated(
        uint256 indexed proposalId,
        address indexed proposer,
        string title,
        string description,
        uint256 startBlock,
        uint256 endBlock
    );
    
    event VoteCast(
        address indexed voter,
        uint256 indexed proposalId,
        VoteType voteType,
        uint256 votingPower
    );
    
    event ProposalExecuted(uint256 indexed proposalId);
    event ProposalCancelled(uint256 indexed proposalId);
    
    // 에러
    error InsufficientProposalThreshold();
    error InvalidProposal();
    error ProposalNotActive();
    error AlreadyVoted();
    error ProposalNotSucceeded();
    error ProposalNotReady();
    error ProposalAlreadyExecuted();
    error UnauthorizedExecution();
    
    modifier validProposal(uint256 proposalId) {
        if (proposalId == 0 || proposalId > proposalCount) {
            revert InvalidProposal();
        }
        _;
    }
    
    constructor(address _governanceToken) Ownable(msg.sender) {
        governanceToken = GovernanceToken(_governanceToken);
    }
    
    /**
     * @dev 새 제안 생성 (커스텀 투표 시간 설정)
     * @param title 제안 제목
     * @param description 제안 설명
     * @param votingStartBlock 투표 시작 블록
     * @param votingEndBlock 투표 종료 블록
     * @return 생성된 제안 ID
     */
    function propose(
        string calldata title,
        string calldata description,
        uint256 votingStartBlock,
        uint256 votingEndBlock
    ) external whenNotPaused returns (uint256) {
        // 제안 임계값 확인 (현재 투표권 사용)
        if (governanceToken.getVotes(msg.sender) < PROPOSAL_THRESHOLD) {
            revert InsufficientProposalThreshold();
        }
        
        // 투표 시간 유효성 검사
        if (votingStartBlock <= block.number) {
            revert InvalidProposal();
        }
        if (votingEndBlock <= votingStartBlock) {
            revert InvalidProposal();
        }
        if (votingEndBlock - votingStartBlock < (VOTING_PERIOD / 12)) {
            revert InvalidProposal();
        }
        
        // 제안 생성
        proposalCount++;
        uint256 proposalId = proposalCount;
        
        Proposal storage newProposal = proposals[proposalId];
        newProposal.id = proposalId;
        newProposal.title = title;
        newProposal.description = description;
        newProposal.proposer = msg.sender;
        newProposal.startBlock = votingStartBlock;
        newProposal.endBlock = votingEndBlock;
        
        latestProposalIds[msg.sender] = proposalId;
        
        emit ProposalCreated(
            proposalId,
            msg.sender,
            title,
            description,
            newProposal.startBlock,
            newProposal.endBlock
        );
        
        return proposalId;
    }
    
    /**
     * @dev 제안에 투표
     * @param proposalId 제안 ID
     * @param voteType 투표 타입 (0: 반대, 1: 찬성, 2: 기권)
     */
    function vote(
        uint256 proposalId,
        VoteType voteType
    ) external validProposal(proposalId) whenNotPaused {
        Proposal storage proposal = proposals[proposalId];
        
        // 투표 가능 상태 확인
        if (getProposalState(proposalId) != ProposalState.Active) {
            revert ProposalNotActive();
        }
        
        // 중복 투표 방지
        if (proposal.hasVoted[msg.sender]) {
            revert AlreadyVoted();
        }
        
        // 투표권 계산 (제안 시작 블록 기준)
        uint256 votingPower = governanceToken.getPastVotes(msg.sender, proposal.startBlock - 1);
        
        // 투표 기록
        proposal.hasVoted[msg.sender] = true;
        proposal.votes[msg.sender] = voteType;
        
        // 투표 집계
        if (voteType == VoteType.Against) {
            proposal.againstVotes += votingPower;
        } else if (voteType == VoteType.For) {
            proposal.forVotes += votingPower;
        } else {
            proposal.abstainVotes += votingPower;
        }
        
        emit VoteCast(msg.sender, proposalId, voteType, votingPower);
    }
    
    /**
     * @dev 제안 실행
     * @param proposalId 실행할 제안 ID
     */
    function executeProposal(uint256 proposalId) external validProposal(proposalId) {
        Proposal storage proposal = proposals[proposalId];
        
        // 실행 가능 상태 확인
        if (getProposalState(proposalId) != ProposalState.Queued) {
            revert ProposalNotReady();
        }
        
        // 실행 시간 확인
        if (block.number < proposal.endBlock + (EXECUTION_DELAY / 12)) {
            revert ProposalNotReady();
        }
        
        // 실행 권한 확인 (제안자 또는 소유자만 실행 가능)
        if (msg.sender != proposal.proposer && msg.sender != owner()) {
            revert UnauthorizedExecution();
        }
        
        proposal.executed = true;
        proposal.executionTime = block.timestamp;
        
        emit ProposalExecuted(proposalId);
    }
    
    /**
     * @dev 제안 취소
     * @param proposalId 취소할 제안 ID
     */
    function cancelProposal(uint256 proposalId) external validProposal(proposalId) {
        Proposal storage proposal = proposals[proposalId];
        
        // 취소 권한 확인 (제안자 또는 소유자만 취소 가능)
        if (msg.sender != proposal.proposer && msg.sender != owner()) {
            revert UnauthorizedExecution();
        }
        
        // 이미 실행된 제안은 취소 불가
        if (proposal.executed) {
            revert ProposalAlreadyExecuted();
        }
        
        proposal.cancelled = true;
        
        emit ProposalCancelled(proposalId);
    }
    
    /**
     * @dev 제안 상태 조회
     * @param proposalId 조회할 제안 ID
     * @return 제안 상태
     */
    function getProposalState(uint256 proposalId) public view validProposal(proposalId) returns (ProposalState) {
        Proposal storage proposal = proposals[proposalId];
        
        if (proposal.cancelled) {
            return ProposalState.Cancelled;
        } else if (proposal.executed) {
            return ProposalState.Executed;
        } else if (block.number <= proposal.startBlock) {
            return ProposalState.Pending;
        } else if (block.number <= proposal.endBlock) {
            return ProposalState.Active;
        } else {
            // 투표 종료 후 상태 판단
            if (proposal.forVotes <= proposal.againstVotes || !_quorumReached(proposalId)) {
                return ProposalState.Defeated;
            } else if (block.number < proposal.endBlock + (EXECUTION_DELAY / 12)) {
                return ProposalState.Queued;
            } else {
                return ProposalState.Succeeded;
            }
        }
    }
    
    /**
     * @dev 쿼럼 달성 여부 확인
     * @param proposalId 확인할 제안 ID
     * @return 쿼럼 달성 여부
     */
    function _quorumReached(uint256 proposalId) internal view returns (bool) {
        Proposal storage proposal = proposals[proposalId];
        uint256 totalVotes = proposal.forVotes + proposal.againstVotes + proposal.abstainVotes;
        uint256 totalSupply = governanceToken.getPastTotalSupply(proposal.startBlock - 1);
        
        return (totalVotes * 100) >= (totalSupply * QUORUM_PERCENTAGE);
    }
    
    /**
     * @dev 제안 정보 조회
     * @param proposalId 조회할 제안 ID
     * @return id 제안 ID
     * @return title 제안 제목
     * @return description 제안 설명
     * @return proposer 제안자 주소
     * @return forVotes 찬성 투표 수
     * @return againstVotes 반대 투표 수
     * @return abstainVotes 기권 투표 수
     * @return startBlock 시작 블록
     * @return endBlock 종료 블록
     * @return executed 실행 여부
     * @return cancelled 취소 여부
     */
    function getProposal(uint256 proposalId) external view validProposal(proposalId) returns (
        uint256 id,
        string memory title,
        string memory description,
        address proposer,
        uint256 forVotes,
        uint256 againstVotes,
        uint256 abstainVotes,
        uint256 startBlock,
        uint256 endBlock,
        bool executed,
        bool cancelled
    ) {
        Proposal storage proposal = proposals[proposalId];
        return (
            proposal.id,
            proposal.title,
            proposal.description,
            proposal.proposer,
            proposal.forVotes,
            proposal.againstVotes,
            proposal.abstainVotes,
            proposal.startBlock,
            proposal.endBlock,
            proposal.executed,
            proposal.cancelled
        );
    }
    
    /**
     * @dev 사용자의 특정 제안 투표 여부 확인
     * @param proposalId 제안 ID
     * @param voter 투표자 주소
     * @return hasVoted 투표 여부
     * @return voteType 투표 타입
     */
    function hasVoted(uint256 proposalId, address voter) external view validProposal(proposalId) returns (bool, VoteType) {
        Proposal storage proposal = proposals[proposalId];
        return (proposal.hasVoted[voter], proposal.votes[voter]);
    }
    
    /**
     * @dev 컨트랙트 일시정지
     */
    function pause() external onlyOwner {
        _pause();
    }
    
    /**
     * @dev 컨트랙트 일시정지 해제
     */
    function unpause() external onlyOwner {
        _unpause();
    }
} 