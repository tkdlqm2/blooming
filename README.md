
# 솔라나 기반 거버넌스 토큰 플랫폼

Web2 포인트 시스템과 Solana 블록체인 기반 거버넌스 토큰의 통합 플랫폼입니다.

## 🏗️ 아키텍처

이 프로젝트는 Domain-Driven Design (DDD) 패턴과 Hexagonal Architecture를 기반으로 설계되었습니다.

### 바운디드 컨텍스트

1. **Point Management Context** - Web2 포인트 시스템 관리
2. **Exchange Context** - 포인트-토큰 교환 중재
3. **Token Management Context** - Solana 토큰 관리
4. **Governance Context** - 탈중앙화 거버넌스

### 기술 스택

- **Backend**: Spring Boot 3.5.3, Java 17
- **Database**: H2 (개발), PostgreSQL (운영)
- **ORM**: Spring Data JPA, Hibernate 6.6.18
- **Cache**: Redis
- **Blockchain**: Solana
- **Architecture**: Hexagonal Architecture (Ports & Adapters)

### 아키텍처 패턴

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
├─────────────────────────────────────────────────────────────┤
│                    Domain Layer                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │   Point     │ │  Exchange   │ │   Token     │ │Governance│ │
│  │  Context    │ │  Context    │ │  Context    │ │ Context │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
├─────────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │   JPA       │ │   Redis     │ │  Solana     │ │   Web   │ │
│  │ Repository  │ │   Cache     │ │   Client    │ │  REST   │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 시작하기

### 필수 요구사항

- Java 17+
- Gradle 7.0+
- Redis (선택사항)

### 실행 방법

1. **프로젝트 클론**
   ```bash
   git clone <repository-url>
   cd governance
   ```

2. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

3. **H2 콘솔 접속**
   - URL: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:governance`
   - Username: `sa`
   - Password: (비어있음)

## 📚 API 문서

### Point Management API

#### 포인트 적립
```http
POST /api/points/earn?userId={userId}&amount={amount}&reason={reason}
```

#### 포인트 잔액 조회
```http
GET /api/points/balance/{userId}
```

### Exchange API

#### 교환 요청
```http
POST /api/exchange/request?userId={userId}&pointAmount={amount}
```

#### 현재 교환 비율 조회
```http
GET /api/exchange/rate
```

#### 교환 요청 상세 조회
```http
GET /api/exchange/request/{requestId}
```

### Token Management API

#### 토큰 민팅
```http
POST /api/tokens/mint?userId={userId}&walletAddress={address}&amount={amount}&reason={reason}
```

#### 토큰 스테이킹
```http
POST /api/tokens/stake?userId={userId}&amount={amount}&reason={reason}
```

#### 토큰 언스테이킹
```http
POST /api/tokens/unstake?userId={userId}&amount={amount}&reason={reason}
```

#### 토큰 잔액 조회
```http
GET /api/tokens/balance/{userId}
```

#### Solana 토큰 잔액 조회
```http
GET /api/tokens/solana-balance/{walletAddress}
```

#### 트랜잭션 상태 확인
```http
GET /api/tokens/transaction/{transactionSignature}/status
```

### Governance API

#### 프로포잘 생성
```http
POST /api/governance/proposals
Content-Type: application/json

{
  "proposerId": "uuid",
  "title": "제안 제목",
  "description": "제안 설명",
  "proposerAddress": "Solana 지갑 주소",
  "votingPeriodDays": 7,
  "votingPeriodHours": 0,
  "minimumVotingPower": 100
}
```

#### 투표 시작
```http
POST /api/governance/proposals/{proposalId}/start?proposerTokenBalance={amount}
```

#### 투표
```http
POST /api/governance/proposals/{proposalId}/vote?voterId={voterId}&voteOption={FOR|AGAINST|ABSTAIN}&votingPower={amount}
```

#### 활성 프로포잘 조회
```http
GET /api/governance/proposals/active
```

## 🏛️ 도메인 모델

### Point Management

- `PointAccount`: 포인트 계정 애그리게이트
- `PointBalance`: 포인트 잔액 값 객체
- `PointAmount`: 포인트 금액 값 객체
- `PointTransaction`: 포인트 거래 엔티티

### Exchange

- `ExchangeRequest`: 교환 요청 애그리게이트
- `ExchangeRate`: 교환 비율 값 객체
- `TokenAmount`: 토큰 금액 값 객체

### Token Management

- `TokenAccount`: 토큰 계정 애그리게이트
- `TokenBalance`: 토큰 잔액 값 객체 (사용가능/스테이킹/잠금)
- `TokenTransaction`: 토큰 거래 엔티티

### Governance

- `Proposal`: 프로포잘 애그리게이트
- `Vote`: 투표 값 객체
- `VoteResults`: 투표 결과 값 객체
- `VotingPeriod`: 투표 기간 값 객체

## 🔄 이벤트 기반 통신

시스템은 도메인 이벤트를 통해 바운디드 컨텍스트 간 통신을 합니다:

- `PointsEarnedEvent`: 포인트 적립 시 발생
- `PointsFrozenEvent`: 포인트 동결 시 교환 처리 시작
- `ExchangeCompletedEvent`: 교환 완료 시 후처리
- `TokensMintedEvent`: 토큰 민팅 시 발생
- `TokensStakedEvent`: 토큰 스테이킹 시 발생
- `ProposalActivatedEvent`: 프로포잘 활성화 시 알림
- `VoteCastEvent`: 투표 시 결과 업데이트

## 🗄️ 데이터베이스 스키마

### 주요 테이블 구조

```sql
-- 포인트 계정
CREATE TABLE point_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    available_amount DECIMAL NOT NULL,
    frozen_amount DECIMAL NOT NULL
);

-- 토큰 계정
CREATE TABLE token_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    wallet_address VARCHAR NOT NULL,
    available_amount DECIMAL NOT NULL,
    staked_amount DECIMAL NOT NULL,
    locked_amount DECIMAL NOT NULL
);

-- 프로포잘
CREATE TABLE proposals (
    id UUID PRIMARY KEY,
    proposer_id UUID NOT NULL,
    title VARCHAR NOT NULL,
    description TEXT,
    status VARCHAR NOT NULL,
    for_votes_amount DECIMAL NOT NULL,
    against_votes_amount DECIMAL NOT NULL,
    abstain_votes_amount DECIMAL NOT NULL,
    total_voting_power_amount DECIMAL NOT NULL,
    minimum_voting_power_amount DECIMAL NOT NULL
);
```

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

## 🔧 주요 기술적 해결사항

### JPA 매핑 최적화

1. **복합 키 매핑**: `@EmbeddedId`를 사용한 복합 키 클래스 구현
2. **중복 컬럼 해결**: `@AttributeOverrides`를 사용한 컬럼명 충돌 방지
3. **값 객체 매핑**: `@Embeddable`을 사용한 도메인 값 객체 매핑
4. **타입 안전성**: JPA Repository 메서드 시그니처의 타입 일관성 보장

### 아키텍처 패턴

1. **Adapter Pattern**: 도메인 리포지토리와 JPA 리포지토리 분리
2. **Event-Driven Architecture**: 도메인 이벤트를 통한 느슨한 결합
3. **Hexagonal Architecture**: 인프라스트럭처와 도메인 계층 분리

## 📦 배포

```bash
# JAR 파일 빌드
./gradlew build

# Docker 이미지 빌드
docker build -t governance-platform .
```

## 🔧 설정

주요 설정은 `application.yml`에서 관리됩니다:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:governance
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  redis:
    host: localhost
    port: 6379

governance:
  exchange:
    rate: 1.0
  voting:
    minimum-participation: 0.1
```

## 📋 개발 가이드

### 새로운 바운디드 컨텍스트 추가

1. 도메인 모델 정의 (`domain/model/`)
2. 리포지토리 인터페이스 정의 (`domain/repository/`)
3. 애플리케이션 서비스 구현 (`application/service/`)
4. JPA 엔티티 및 리포지토리 구현 (`infrastructure/repository/`)
5. REST 컨트롤러 구현 (`infrastructure/web/`)

### 도메인 이벤트 추가

1. 이벤트 클래스 정의 (`domain/event/`)
2. 이벤트 핸들러 구현 (`application/handler/`)
3. 이벤트 발행 로직 추가

## 🤝 기여하기

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 