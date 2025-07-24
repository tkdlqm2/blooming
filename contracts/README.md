### �� **핵심 정보**
- **프로젝트 개요**: ERC20 기반 거버넌스 토큰과 투표 시스템
- **배포된 컨트랙트 주소**: Sepolia 테스트넷의 실제 주소들
- **프로젝트 구조**: 현재 남아있는 파일들 기준

### 🏗️ **구조 설명**
- `contracts/` 폴더: 원본 컨트랙트 소스 코드
- `*_flattened.sol`: 배포용 컨트랙트 (의존성 포함)
- `deployment.json`: 배포 정보
- Hardhat 설정 및 의존성 파일들

### �� **사용법 가이드**
- 설치 및 설정 방법
- 컴파일, 테스트, 배포 명령어
- 컨트랙트별 상세 기능 설명
- 실제 사용 예제 코드

### 🔧 **개발 도구**
- Hardhat 설정 정보
- 지원 네트워크 (Hardhat, Sepolia, Goerli)
- Etherscan 검증 방법

### 📜 **컨트랙트 상세**
- **GovernanceToken**: ERC20 + 투표권 + 포인트 교환 + 자동 위임
- **GovernanceContract**: 제안 시스템 + 투표 + 쿼럼 기반 의사결정

이제 프로젝트를 처음 접하는 사람도 쉽게 이해하고 사용할 수 있는 완전한 문서가 되었습니다!

### **배포된 컨트랙트 주소**:
```json
{
  "GovernanceToken": "0xd2Dfe16C1F31493530D297D58E32c337fd27615D",
  "GovernanceContract": "0x4E5EE91796498E843a7Ae952BC86B1a1547C60bB"
}
```
# Governance System Documentation

이 문서는 Governance Token & Contract System의 상세한 기술 문서입니다.

## 📚 문서 목록

### 컨트랙트 문서
- **[GovernanceToken.md](./GovernanceToken.md)** - 거버넌스 토큰 컨트랙트 상세 설명
- **[GovernanceContract.md](./GovernanceContract.md)** - 거버넌스 투표 컨트랙트 상세 설명

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌──────────────────┐
│ GovernanceToken │    │ GovernanceContract│
│                 │    │                  │
│ • ERC20 Token   │◄───┤ • Proposal Mgmt  │
│ • Voting Power  │    │ • Voting System  │
│ • Point Exchange│    │ • Execution      │
│ • Auto Delegation│   │ • Quorum System  │
└─────────────────┘    └──────────────────┘
```

## 🔗 컨트랙트 관계

### GovernanceToken
- **역할**: 투표권을 제공하는 기본 토큰
- **기능**:
   - ERC20 표준 토큰 기능
   - 투표권 관리 및 위임
   - 포인트 교환 시스템
   - 자동 위임 기능

### GovernanceContract
- **역할**: 거버넌스 투표 시스템
- **의존성**: GovernanceToken 컨트랙트
- **기능**:
   - 제안 생성 및 관리
   - 투표 시스템
   - 쿼럼 기반 의사결정
   - 제안 실행

## 📊 시스템 파라미터

### GovernanceToken
| 파라미터 | 값 | 설명 |
|---------|-----|------|
| EXCHANGE_RATE | 100 | 100 포인트 = 1 토큰 |
| DECIMALS_FACTOR | 10^18 | 토큰 소수점 |

### GovernanceContract
| 파라미터 | 값 | 설명 |
|---------|-----|------|
| PROPOSAL_THRESHOLD | 1000 토큰 | 제안 생성 최소 투표권 |
| VOTING_DELAY | 1일 | 제안 생성 후 투표 시작까지 |
| VOTING_PERIOD | 7일 | 투표 기간 |
| EXECUTION_DELAY | 2일 | 투표 종료 후 실행까지 |
| QUORUM_PERCENTAGE | 4% | 최소 참여율 |

## 🔄 거버넌스 워크플로우

### 1. 제안 생성
```
사용자 → GovernanceContract.propose()
       ↓
투표권 확인 (≥ 1000 토큰)
       ↓
제안 생성 및 저장
       ↓
ProposalCreated 이벤트 발생
```

### 2. 투표 과정
```
투표 시작 (VOTING_DELAY 후)
       ↓
사용자 → GovernanceContract.vote()
       ↓
투표권 계산 (제안 시작 블록 기준)
       ↓
투표 집계 및 기록
       ↓
VoteCast 이벤트 발생
```

### 3. 제안 실행
```
투표 종료
       ↓
쿼럼 및 결과 확인
       ↓
실행 대기 (EXECUTION_DELAY)
       ↓
제안자/소유자 → GovernanceContract.execute()
       ↓
ProposalExecuted 이벤트 발생
```

## 🔒 보안 기능

### 1. 재진입 공격 방지
- `ReentrancyGuard` 사용
- `nonReentrant` 수정자 적용

### 2. 권한 관리
- `Ownable` 패턴
- 명시적 권한 검증

### 3. 일시정지 기능
- 긴급 상황 시 컨트랙트 일시정지
- `whenNotPaused` 수정자

### 4. 투표권 스냅샷
- 특정 블록 기준 투표권 계산
- 투표 조작 방지

## 📈 이벤트 시스템

### GovernanceToken 이벤트
- `PointsExchanged`: 포인트 교환
- `AutoDelegated`: 자동 위임
- `TokensBurned`: 토큰 소각

### GovernanceContract 이벤트
- `ProposalCreated`: 제안 생성
- `VoteCast`: 투표
- `ProposalExecuted`: 제안 실행
- `ProposalCancelled`: 제안 취소

## 🛠️ 개발 가이드

### 컨트랙트 배포 순서
1. **GovernanceToken 배포**
   ```javascript
   const token = await GovernanceToken.deploy(
       "Governance Token",
       "GOV", 
       1000000  // 100만 토큰
   );
   ```

2. **GovernanceContract 배포**
   ```javascript
   const governance = await GovernanceContract.deploy(
       token.address
   );
   ```

### 테스트 시나리오
1. 토큰 전송 및 위임
2. 제안 생성
3. 투표 진행
4. 제안 실행

## 🔍 모니터링

### 주요 지표
- 총 제안 수
- 활성 제안 수
- 투표 참여율
- 토큰 분포

### 이벤트 모니터링
- 제안 생성 이벤트
- 투표 이벤트
- 실행 이벤트

## 📝 주의사항

1. **투표권 활성화**: 토큰 보유 후 `delegate()` 호출 필요
2. **제안 임계값**: 1000 토큰 이상의 투표권 필요
3. **투표 기간**: 충분한 투표 기간 설정
4. **쿼럼 요구**: 4% 최소 참여율
5. **실행 지연**: 2일 대기 기간

## 🔗 관련 링크

- [메인 README](../README.md)
- [배포 정보](../deployment.json)
- [Hardhat 설정](../hardhat.config.js) 