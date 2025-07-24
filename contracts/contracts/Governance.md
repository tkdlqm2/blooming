# GovernanceContract Contract

## 📋 개요

`GovernanceContract`는 완전한 DAO(Decentralized Autonomous Organization) 거버넌스 시스템을 구현한 스마트 컨트랙트입니다. 제안 생성, 투표, 실행을 포함한 전체 거버넌스 생태계를 제공합니다.

## 🏗️ 컨트랙트 구조

### 상속 관계
```solidity
contract GovernanceContract is Ownable, ReentrancyGuard, Pausable
```

- **Ownable**: 소유자 권한 관리
- **ReentrancyGuard**: 재진입 공격 방지
- **Pausable**: 일시정지 기능

### 의존성
```solidity
GovernanceToken public governanceToken;  // 거버넌스 토큰 컨트랙트
```

## 🔧 주요 기능

### 1. 제안 시스템
- **제안 생성**: 투표 가능한 제안 생성
- **제안 관리**: 제안 상태 추적 및 관리
- **제안 취소**: 제안자 또는 소유자가 제안 취소

### 2. 투표 시스템
- **투표 타입**: 찬성(For), 반대(Against), 기권(Abstain)
- **투표권 계산**: 제안 시작 블록 기준 투표권
- **중복 투표 방지**: 한 제안당 한 번만 투표 가능

### 3. 쿼럼 시스템
- **최소 참여율**: 4% 쿼럼 요구사항
- **투표 집계**: 찬성/반대/기권 투표 집계

### 4. 실행 시스템
- **제안 실행**: 승인된 제안 실행
- **실행 지연**: 투표 종료 후 2일 대기 기간
- **권한 관리**: 제안자 또는 소유자만 실행 가능

## 📊 상태 변수

### 상수
```solidity
uint256 public constant VOTING_DELAY = 1 days;           // 1일 = 약 7200 블록
uint256 public constant VOTING_PERIOD = 7 days;          // 7일 = 약 50400 블록
uint256 public constant EXECUTION_DELAY = 2 days;        // 2일 = 약 14400 블록
uint256 public constant PROPOSAL_THRESHOLD = 1000 * 10**18; // 1000 토큰 필요
uint256 public constant QUORUM_PERCENTAGE = 4;           // 4% 쿼럼
```

### 상태 변수
```solidity
uint256 public proposalCount;                           // 총 제안 수
mapping(uint256 => Proposal) public proposals;          // 제안 정보
mapping(address => uint256) public latestProposalIds;   // 사용자별 최신 제안 ID
```

## 🏛️ 데이터 구조

### Proposal 구조체
```solidity
struct Proposal {
    uint256 id;                    // 제안 ID
    string title;                  // 제안 제목
    string description;            // 제안 설명
    address proposer;              // 제안자
    uint256 forVotes;              // 찬성 투표 수
    uint256 againstVotes;          // 반대 투표 수
    uint256 abstainVotes;          // 기권 투표 수
    uint256 startBlock;            // 투표 시작 블록
    uint256 endBlock;              // 투표 종료 블록
    uint256 executionTime;         // 실행 시간
    bool executed;                 // 실행 여부
    bool cancelled;                // 취소 여부
    mapping(address => bool) hasVoted;    // 투표 여부
    mapping(address => VoteType) votes;   // 투표 타입
}
```

### VoteType 열거형
```solidity
enum VoteType {
    Against,    // 0: 반대
    For,        // 1: 찬성
    Abstain     // 2: 기권
}
```

### ProposalState 열거형
```solidity
enum ProposalState {
    Pending,    // 대기 중 (투표 시작 전)
    Active,     // 활성 (투표 중)
    Cancelled,  // 취소됨
    Defeated,   // 패배 (반대 > 찬성 또는 쿼럼 미달)
    Succeeded,  // 성공 (찬성 > 반대, 쿼럼 달성)
    Queued,     // 대기열 (실행 대기)
    Expired,    // 만료됨
    Executed    // 실행됨
}
```

## 🚀 주요 함수

### 제안 생성
```solidity
function propose(
    string calldata title,
    string calldata description,
    uint256 votingStartBlock,
    uint256 votingEndBlock
) external whenNotPaused returns (uint256)
```

**기능:**
- 새 제안 생성
- 투표 임계값 확인 (1000 토큰)
- 투표 시간 유효성 검사
- 제안 정보 저장

**검증 조건:**
- 호출자의 투표권 >= 1000 토큰
- 시작 블록 > 현재 블록
- 종료 블록 > 시작 블록
- 투표 기간 >= 최소 기간 (4200 블록)

### 투표
```solidity
function vote(
    uint256 proposalId,
    VoteType voteType
) external validProposal(proposalId) whenNotPaused
```

**기능:**
- 제안에 투표
- 투표권 계산 (제안 시작 블록 기준)
- 투표 집계 및 기록

**검증 조건:**
- 제안이 Active 상태
- 중복 투표 방지
- 유효한 투표 타입

### 제안 실행
```solidity
function execute(uint256 proposalId) external validProposal(proposalId)
```

**기능:**
- 승인된 제안 실행
- 실행 시간 기록

**검증 조건:**
- 제안이 Queued 상태
- 실행 지연 기간 경과
- 제안자 또는 소유자만 실행 가능

### 제안 취소
```solidity
function cancelProposal(uint256 proposalId) external validProposal(proposalId)
```

**기능:**
- 제안 취소
- 제안자 또는 소유자만 가능

**검증 조건:**
- 제안이 아직 실행되지 않음
- 제안자 또는 소유자 권한

### 제안 상태 조회
```solidity
function getProposalState(uint256 proposalId) public view returns (ProposalState)
```

**반환 상태:**
- **Pending**: 투표 시작 전
- **Active**: 투표 중
- **Cancelled**: 취소됨
- **Defeated**: 패배 (반대 > 찬성 또는 쿼럼 미달)
- **Succeeded**: 성공 (찬성 > 반대, 쿼럼 달성)
- **Queued**: 실행 대기
- **Expired**: 만료됨
- **Executed**: 실행됨

### 제안 정보 조회
```solidity
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
)
```

### 투표 여부 확인
```solidity
function hasVoted(uint256 proposalId, address voter) external view validProposal(proposalId) returns (bool, VoteType)
```

## 📈 이벤트

```solidity
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
```

## ⚠️ 에러

```solidity
error InsufficientProposalThreshold();  // 제안 임계값 부족
error InvalidProposal();                // 유효하지 않은 제안
error ProposalNotActive();              // 제안이 활성 상태가 아님
error AlreadyVoted();                   // 이미 투표함
error ProposalNotSucceeded();           // 제안이 성공하지 않음
error ProposalNotReady();               // 제안이 실행 준비되지 않음
error ProposalAlreadyExecuted();        // 제안이 이미 실행됨
error UnauthorizedExecution();          // 실행 권한 없음
```

## 🔒 보안 기능

### 1. 재진입 공격 방지
- `ReentrancyGuard` 사용
- 안전한 상태 변경

### 2. 권한 관리
- `Ownable` 패턴으로 소유자 권한 관리
- 제안자 권한 검증

### 3. 일시정지 기능
- 긴급 상황 시 컨트랙트 일시정지
- `whenNotPaused` 수정자 적용

### 4. 투표권 스냅샷
- 제안 시작 블록 기준 투표권 계산
- 투표 조작 방지

### 5. 쿼럼 시스템
- 최소 참여율 요구사항
- 의사결정의 정당성 보장

## 💡 사용 예제

### 1. 제안 생성
```javascript
// 현재 블록 기준으로 투표 기간 설정
const currentBlock = await ethers.provider.getBlockNumber();
const startBlock = currentBlock + 10;  // 10블록 후 시작
const endBlock = startBlock + 7200;    // 24시간 투표

const proposalId = await governanceContract.propose(
    "토큰 소각 제안",
    "총 공급량의 5%를 소각하여 토큰 가치 상승을 도모합니다.",
    startBlock,
    endBlock
);
```

### 2. 투표
```javascript
// 제안 상태 확인
const state = await governanceContract.getProposalState(proposalId);

// 투표 (찬성)
await governanceContract.vote(proposalId, 1); // 1 = For

// 투표 (반대)
await governanceContract.vote(proposalId, 0); // 0 = Against

// 투표 (기권)
await governanceContract.vote(proposalId, 2); // 2 = Abstain
```

### 3. 제안 정보 조회
```javascript
// 제안 정보
const proposal = await governanceContract.getProposal(proposalId);
console.log("제목:", proposal.title);
console.log("설명:", proposal.description);
console.log("찬성:", ethers.formatEther(proposal.forVotes));
console.log("반대:", ethers.formatEther(proposal.againstVotes));

// 제안 상태
const state = await governanceContract.getProposalState(proposalId);
console.log("상태:", state);
```

### 4. 투표 여부 확인
```javascript
const [hasVoted, voteType] = await governanceContract.hasVoted(
    proposalId, 
    userAddress
);
console.log("투표 여부:", hasVoted);
console.log("투표 타입:", voteType);
```

### 5. 제안 실행
```javascript
// 실행 가능 상태 확인
const state = await governanceContract.getProposalState(proposalId);

// 제안 실행
await governanceContract.execute(proposalId);
```

## 🔧 설정 및 배포

### 배포 정보
- **네트워크**: Sepolia Testnet
- **주소**: `0xd2Dfe16C1F31493530D297D58E32c337fd27615D`
- **배포자**: `0x55D5c49e36f8A89111687C9DC8355121068f0cD8`

### 초기 설정
```javascript
const governance = await GovernanceContract.deploy(
    governanceTokenAddress  // 거버넌스 토큰 컨트랙트 주소
);
```

## 📝 주의사항

1. **투표권 필요**: 제안 생성 시 최소 1000 토큰의 투표권 필요
2. **투표 기간**: 제안 생성 후 1일 대기, 7일 투표 기간
3. **실행 지연**: 투표 종료 후 2일 대기 후 실행 가능
4. **쿼럼 요구**: 최소 4% 참여율 필요
5. **투표권 스냅샷**: 제안 시작 블록 기준으로 투표권 계산
6. **중복 투표 방지**: 한 제안당 한 번만 투표 가능

## 🔗 관련 컨트랙트

- **GovernanceToken**: 투표권을 제공하는 토큰 컨트랙트
- **ERC20Votes**: 투표권 관리 표준
- **OpenZeppelin**: 보안 컨트랙트 라이브러리

## 📊 거버넌스 파라미터

| 파라미터 | 값 | 설명 |
|---------|-----|------|
| PROPOSAL_THRESHOLD | 1000 토큰 | 제안 생성 최소 투표권 |
| VOTING_DELAY | 1일 | 제안 생성 후 투표 시작까지 |
| VOTING_PERIOD | 7일 | 투표 기간 |
| EXECUTION_DELAY | 2일 | 투표 종료 후 실행까지 |
| QUORUM_PERCENTAGE | 4% | 최소 참여율 | 