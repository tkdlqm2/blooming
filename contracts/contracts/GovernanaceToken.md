# GovernanceToken Contract

## 📋 개요

`GovernanceToken`은 ERC20 표준을 기반으로 한 거버넌스 토큰입니다. 투표권 관리, 포인트 교환 시스템, 자동 위임 기능을 포함한 완전한 거버넌스 토큰을 제공합니다.

## 🏗️ 컨트랙트 구조

### 상속 관계
```solidity
contract GovernanceToken is ERC20, ERC20Votes, ERC20Permit, Ownable, Pausable, ReentrancyGuard
```

- **ERC20**: 기본 토큰 기능
- **ERC20Votes**: 투표권 관리 및 위임
- **ERC20Permit**: 서명 기반 승인
- **Ownable**: 소유자 권한 관리
- **Pausable**: 일시정지 기능
- **ReentrancyGuard**: 재진입 공격 방지

## 🔧 주요 기능

### 1. 기본 토큰 기능
- **전송**: `transfer()`, `transferFrom()`
- **잔액 조회**: `balanceOf()`
- **승인**: `approve()`, `allowance()`
- **총 공급량**: `totalSupply()`

### 2. 투표권 관리
- **위임**: `delegate()`, `delegateBySig()`
- **투표권 조회**: `getVotes()`, `getPastVotes()`
- **위임자 조회**: `delegates()`
- **총 투표권**: `getPastTotalSupply()`

### 3. 포인트 교환 시스템
- **교환 비율**: 100 포인트 = 1 토큰
- **권한 관리**: `authorizedExchangers` 매핑
- **교환 기록**: `totalExchanged` 매핑

### 4. 자동 위임 기능
- **자동 위임**: 토큰 전송 시 자동으로 수신자에게 위임
- **설정 관리**: `autoDelegationEnabled`, `autoDelegationExempt`

## 📊 상태 변수

### 상수
```solidity
uint256 public constant EXCHANGE_RATE = 100;        // 100 포인트 = 1 토큰
uint256 public constant DECIMALS_FACTOR = 10**18;   // 소수점 팩터
```

### 매핑
```solidity
mapping(address => bool) public authorizedExchangers;  // 교환 권한자
mapping(address => uint256) public totalExchanged;     // 교환 기록
mapping(address => bool) public autoDelegationExempt;  // 자동 위임 면제
```

### 설정
```solidity
bool public autoDelegationEnabled = true;  // 자동 위임 활성화 여부
```

## 🚀 주요 함수

### 생성자
```solidity
constructor(
    string memory name,
    string memory symbol,
    uint256 initialSupply
)
```
- 토큰 이름, 심볼, 초기 공급량 설정
- 배포자에게 초기 공급량 발행
- 배포자를 기본 교환 권한자로 설정
- 자동 위임 활성화 시 배포자에게 자동 위임

### 토큰 전송 (자동 위임 포함)
```solidity
function transferWithAutoDelegation(address to, uint256 amount) external returns (bool)
function transferFromWithAutoDelegation(address from, address to, uint256 amount) external returns (bool)
```
- 기본 전송 기능 + 자동 위임
- 수신자가 아직 위임하지 않은 경우 자동으로 자신에게 위임
- `autoDelegationExempt` 주소는 제외

### 포인트 교환
```solidity
function exchangePointsForTokens(
    address user, 
    uint256 points
) external nonReentrant whenNotPaused
```
- 권한자만 호출 가능
- 100 포인트를 1 토큰으로 교환
- 교환 기록 업데이트
- 자동 위임 처리

### 토큰 소각
```solidity
function burnTokens(uint256 amount) external
```
- 사용자가 자신의 토큰을 소각
- 투표권도 함께 감소

### 권한 관리
```solidity
function addExchanger(address exchanger) external onlyOwner
function removeExchanger(address exchanger) external onlyOwner
```
- 교환 권한자 추가/제거
- 소유자만 호출 가능

### 자동 위임 설정
```solidity
function toggleAutoDelegation() external onlyOwner
function setAutoDelegationExempt(address user, bool exempt) external onlyOwner
```
- 자동 위임 기능 전체 토글
- 특정 주소의 자동 위임 면제 설정

### 일시정지 관리
```solidity
function pause() external onlyOwner
function unpause() external onlyOwner
```
- 컨트랙트 일시정지/해제
- 긴급 상황 시 사용

## 📈 이벤트

```solidity
event PointsExchanged(address indexed user, uint256 points, uint256 tokens);
event ExchangerAdded(address indexed exchanger);
event ExchangerRemoved(address indexed exchanger);
event TokensBurned(address indexed user, uint256 amount);
event AutoDelegationToggled(bool enabled);
event AutoDelegationExemptSet(address indexed user, bool exempt);
event AutoDelegated(address indexed user, uint256 amount);
```

## ⚠️ 에러

```solidity
error UnauthorizedExchanger();  // 권한 없는 교환 시도
error InvalidAmount();          // 잘못된 금액
error ExchangePaused();         // 교환 일시정지 상태
```

## 🔒 보안 기능

### 1. 재진입 공격 방지
- `ReentrancyGuard` 사용
- `nonReentrant` 수정자 적용

### 2. 권한 관리
- `Ownable` 패턴으로 소유자 권한 관리
- 교환 권한자 명시적 관리

### 3. 일시정지 기능
- 긴급 상황 시 컨트랙트 일시정지
- `whenNotPaused` 수정자로 제한

### 4. 투표권 스냅샷
- 특정 블록에서의 투표권 조회
- 투표 조작 방지

## 💡 사용 예제

### 1. 토큰 전송 및 자동 위임
```javascript
// 자동 위임이 포함된 전송
await governanceToken.transferWithAutoDelegation(
    recipientAddress, 
    ethers.parseEther("100")
);
```

### 2. 포인트 교환
```javascript
// 권한자만 호출 가능
await governanceToken.exchangePointsForTokens(
    userAddress, 
    10000  // 10000 포인트 = 100 토큰
);
```

### 3. 투표권 위임
```javascript
// 자신에게 위임
await governanceToken.delegate(userAddress);

// 다른 주소에 위임
await governanceToken.delegate(delegateAddress);
```

### 4. 투표권 조회
```javascript
// 현재 투표권
const currentVotes = await governanceToken.getVotes(userAddress);

// 과거 블록의 투표권
const pastVotes = await governanceToken.getPastVotes(
    userAddress, 
    blockNumber
);
```

## 🔧 설정 및 배포

### 배포 정보
- **네트워크**: Sepolia Testnet
- **주소**: `0xeafF00556BC06464511319dAb26D6CAC148b89d0`
- **배포자**: `0x55D5c49e36f8A89111687C9DC8355121068f0cD8`

### 초기 설정
```javascript
const token = await GovernanceToken.deploy(
    "Governance Token",    // 이름
    "GOV",                 // 심볼
    1000000                // 초기 공급량 (100만 토큰)
);
```

## 📝 주의사항

1. **투표권 활성화**: 토큰을 받은 후 `delegate()` 호출 필요
2. **교환 권한**: 포인트 교환은 권한자만 가능
3. **자동 위임**: 전송 시 자동으로 위임되지만 수동 위임도 가능
4. **일시정지**: 긴급 상황 시 모든 기능 일시정지 가능
5. **투표권 스냅샷**: 투표 시점의 토큰 잔액 기준으로 투표권 계산

## 🔗 관련 컨트랙트

- **GovernanceContract**: 이 토큰을 사용하는 거버넌스 시스템
- **ERC20Votes**: 투표권 관리 표준
- **ERC20Permit**: 서명 기반 승인 표준 