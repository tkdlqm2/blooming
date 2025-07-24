# 아키텍처 문서

## 개요

이 프로젝트는 Domain-Driven Design (DDD)과 Hexagonal Architecture를 기반으로 설계된 Solana 기반 거버넌스 토큰 플랫폼입니다.

## 아키텍처 원칙

### 1. Domain-Driven Design (DDD)

- **바운디드 컨텍스트**: 명확한 경계를 가진 도메인 영역
- **애그리게이트**: 일관성 경계를 가진 엔티티 그룹
- **값 객체**: 불변 객체로 표현되는 도메인 개념
- **도메인 서비스**: 애그리게이트 간 협력을 위한 서비스

### 2. Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                 Application Services                    │ │
│  │  - PointManagementService                              │ │
│  │  - ExchangeApplicationService                          │ │
│  │  - TokenManagementService                              │ │
│  │  - GovernanceApplicationService                        │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │   Point     │ │  Exchange   │ │   Token     │ │Governance│ │
│  │  Context    │ │  Context    │ │  Context    │ │ Context │ │
│  │             │ │             │ │             │ │         │ │
│  │ - PointAccount│ - ExchangeRequest│ - TokenAccount│ - Proposal│ │
│  │ - PointBalance│ - ExchangeRate  │ - TokenBalance│ - Vote   │ │
│  │ - PointAmount │ - TokenAmount   │ - TokenTransaction│ - VoteResults│ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
├─────────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │   JPA       │ │   Redis     │ │  Solana     │ │   Web   │ │
│  │ Repository  │ │   Cache     │ │   Client    │ │  REST   │ │
│  │ Adapters    │ │   Adapters  │ │   Adapters  │ │Controllers│ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 바운디드 컨텍스트

### 1. Point Management Context

**책임**: Web2 포인트 시스템 관리

**핵심 개념**:
- `PointAccount`: 포인트 계정 애그리게이트
- `PointBalance`: 포인트 잔액 (사용가능/동결)
- `PointAmount`: 포인트 금액 값 객체
- `PointTransaction`: 포인트 거래 기록

**도메인 이벤트**:
- `PointsEarnedEvent`: 포인트 적립
- `PointsFrozenEvent`: 포인트 동결

### 2. Exchange Context

**책임**: 포인트-토큰 교환 중재

**핵심 개념**:
- `ExchangeRequest`: 교환 요청 애그리게이트
- `ExchangeRate`: 교환 비율 값 객체
- `TokenAmount`: 토큰 금액 값 객체

**도메인 이벤트**:
- `ExchangeProcessingStartedEvent`: 교환 처리 시작
- `ExchangeCompletedEvent`: 교환 완료

### 3. Token Management Context

**책임**: Solana 토큰 관리

**핵심 개념**:
- `TokenAccount`: 토큰 계정 애그리게이트
- `TokenBalance`: 토큰 잔액 (사용가능/스테이킹/잠금)
- `TokenTransaction`: 토큰 거래 기록

**도메인 이벤트**:
- `TokensMintedEvent`: 토큰 민팅
- `TokensStakedEvent`: 토큰 스테이킹

### 4. Governance Context

**책임**: 탈중앙화 거버넌스

**핵심 개념**:
- `Proposal`: 프로포잘 애그리게이트
- `Vote`: 투표 값 객체
- `VoteResults`: 투표 결과 값 객체
- `VotingPeriod`: 투표 기간 값 객체

**도메인 이벤트**:
- `ProposalActivatedEvent`: 프로포잘 활성화
- `VoteCastEvent`: 투표

## 계층 구조

### Domain Layer

도메인 로직을 포함하는 핵심 계층입니다.

```java
// 애그리게이트 루트
public class PointAccount extends AggregateRoot<PointAccountId> {
    private PointAccountId id;
    private UserId userId;
    private PointBalance balance;
    private List<PointTransaction> transactions;
    
    // 도메인 메소드
    public void earnPoints(PointAmount amount, String reason) {
        // 비즈니스 로직
    }
}

// 값 객체
@Embeddable
public class PointBalance {
    @Embedded
    private PointAmount available;
    
    @Embedded
    private PointAmount frozen;
    
    // 불변 객체로 구현
}
```

### Application Layer

도메인 객체를 조율하는 애플리케이션 서비스 계층입니다.

```java
@Service
@Transactional
public class PointManagementService {
    private final PointAccountRepository pointAccountRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public void earnPoints(UserId userId, PointAmount amount, String reason) {
        PointAccount account = pointAccountRepository.findByUserId(userId)
            .orElseGet(() -> PointAccount.create(userId));
            
        account.earnPoints(amount, reason);
        pointAccountRepository.save(account);
        
        eventPublisher.publishEvent(new PointsEarnedEvent(userId, amount));
    }
}
```

### Infrastructure Layer

외부 시스템과의 통신을 담당하는 계층입니다.

```java
// JPA Repository Adapter
@Repository
public class PointAccountRepositoryAdapter implements PointAccountRepository {
    private final PointAccountJpaRepository jpaRepository;
    
    @Override
    public Optional<PointAccount> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId);
    }
}

// REST Controller
@RestController
@RequestMapping("/api/points")
public class PointController {
    private final PointManagementService pointManagementService;
    
    @PostMapping("/earn")
    public ResponseEntity<Void> earnPoints(
        @RequestParam UserId userId,
        @RequestParam PointAmount amount,
        @RequestParam String reason) {
        
        pointManagementService.earnPoints(userId, amount, reason);
        return ResponseEntity.ok().build();
    }
}
```

## 데이터 모델링

### JPA 매핑 전략

1. **복합 키 매핑**
```java
@Embeddable
public class PointAccountId {
    @Column(name = "id", nullable = false)
    private UUID value;
}

@Entity
@Table(name = "point_accounts")
public class PointAccount {
    @EmbeddedId
    private PointAccountId id;
}
```

2. **값 객체 매핑**
```java
@Embeddable
public class PointBalance {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "available_amount"))
    })
    private PointAmount available;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "frozen_amount"))
    })
    private PointAmount frozen;
}
```

3. **컬렉션 매핑**
```java
@Entity
public class PointAccount {
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PointTransaction> transactions;
}
```

## 이벤트 기반 통신

### 도메인 이벤트 구조

```java
public abstract class DomainEvent {
    private final Instant occurredAt;
    
    protected DomainEvent() {
        this.occurredAt = Instant.now();
    }
}

public class PointsEarnedEvent extends DomainEvent {
    private final UserId userId;
    private final PointAmount amount;
}
```

### 이벤트 핸들러

```java
@Component
public class ExchangeEventHandler {
    
    @EventListener
    public void handlePointsFrozenEvent(PointsFrozenEvent event) {
        // 교환 처리 로직
    }
}
```

## 보안 고려사항

1. **입력 검증**: 모든 외부 입력에 대한 검증
2. **트랜잭션 관리**: ACID 속성 보장
3. **권한 검사**: 사용자 권한 확인
4. **감사 로그**: 모든 중요 작업 기록

## 성능 최적화

1. **지연 로딩**: `FetchType.LAZY` 사용
2. **캐싱**: Redis를 통한 캐싱
3. **배치 처리**: 대량 데이터 처리 최적화
4. **인덱싱**: 적절한 데이터베이스 인덱스

## 확장성 고려사항

1. **마이크로서비스 분리**: 바운디드 컨텍스트별 분리 가능
2. **이벤트 소싱**: 이벤트 기반 아키텍처로 확장 가능
3. **CQRS**: 읽기/쓰기 모델 분리 가능
4. **API 버전 관리**: 하위 호환성 보장 