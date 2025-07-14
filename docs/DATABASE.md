# 데이터베이스 설계 문서

## 개요

이 문서는 Solana 기반 거버넌스 토큰 플랫폼의 데이터베이스 설계와 JPA 매핑 전략을 설명합니다.

## 데이터베이스 스키마

### 1. Point Management Context

#### point_accounts 테이블
```sql
CREATE TABLE point_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    available_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    frozen_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_point_accounts_user_id ON point_accounts(user_id);
```

#### point_transactions 테이블
```sql
CREATE TABLE point_transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    amount DECIMAL(20,8) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    FOREIGN KEY (account_id) REFERENCES point_accounts(id)
);

CREATE INDEX idx_point_transactions_account_id ON point_transactions(account_id);
CREATE INDEX idx_point_transactions_occurred_at ON point_transactions(occurred_at);
```

### 2. Token Management Context

#### token_accounts 테이블
```sql
CREATE TABLE token_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    wallet_address VARCHAR(44) NOT NULL,
    available_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    staked_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    locked_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_token_accounts_user_id ON token_accounts(user_id);
CREATE INDEX idx_token_accounts_wallet_address ON token_accounts(wallet_address);
CREATE UNIQUE INDEX uk_token_accounts_user_id ON token_accounts(user_id);
```

#### token_transactions 테이블
```sql
CREATE TABLE token_transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    amount DECIMAL(20,8) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    transaction_signature VARCHAR(88),
    reference_id VARCHAR(255),
    FOREIGN KEY (account_id) REFERENCES token_accounts(id)
);

CREATE INDEX idx_token_transactions_account_id ON token_transactions(account_id);
CREATE INDEX idx_token_transactions_occurred_at ON token_transactions(occurred_at);
CREATE INDEX idx_token_transactions_signature ON token_transactions(transaction_signature);
```

### 3. Exchange Context

#### exchange_requests 테이블
```sql
CREATE TABLE exchange_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    point_amount DECIMAL(20,8) NOT NULL,
    token_amount DECIMAL(20,8) NOT NULL,
    exchange_rate DECIMAL(20,8) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_exchange_requests_user_id ON exchange_requests(user_id);
CREATE INDEX idx_exchange_requests_status ON exchange_requests(status);
CREATE INDEX idx_exchange_requests_created_at ON exchange_requests(created_at);
```

### 4. Governance Context

#### proposals 테이블
```sql
CREATE TABLE proposals (
    id UUID PRIMARY KEY,
    proposer_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    for_votes_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    against_votes_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    abstain_votes_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    total_voting_power_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    minimum_voting_power_amount DECIMAL(20,8) NOT NULL,
    voting_start_at TIMESTAMP,
    voting_end_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_proposals_proposer_id ON proposals(proposer_id);
CREATE INDEX idx_proposals_status ON proposals(status);
CREATE INDEX idx_proposals_voting_end_at ON proposals(voting_end_at);
```

#### proposal_voters 테이블
```sql
CREATE TABLE proposal_voters (
    proposal_id UUID NOT NULL,
    voter_id UUID NOT NULL,
    PRIMARY KEY (proposal_id, voter_id),
    FOREIGN KEY (proposal_id) REFERENCES proposals(id)
);

CREATE INDEX idx_proposal_voters_voter_id ON proposal_voters(voter_id);
```

## JPA 매핑 전략

### 1. 복합 키 매핑

#### ID 클래스 설계
```java
@Embeddable
public class PointAccountId {
    @Column(name = "id", nullable = false)
    private UUID value;
    
    // 기본 생성자, getter, setter, equals, hashCode, toString
}

@Entity
@Table(name = "point_accounts")
public class PointAccount extends AggregateRoot<PointAccountId> {
    @EmbeddedId
    private PointAccountId id;
    
    // 다른 필드들...
}
```

### 2. 값 객체 매핑

#### 중복 컬럼 해결
```java
@Embeddable
public class PointBalance {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "available_amount", nullable = false))
    })
    private PointAmount available;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "frozen_amount", nullable = false))
    })
    private PointAmount frozen;
    
    // 생성자, 메소드들...
}
```

#### TokenBalance 매핑
```java
@Embeddable
public class TokenBalance {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "available_amount", nullable = false))
    })
    private TokenAmount available;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "staked_amount", nullable = false))
    })
    private TokenAmount staked;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "locked_amount", nullable = false))
    })
    private TokenAmount locked;
}
```

#### VoteResults 매핑
```java
@Embeddable
public class VoteResults {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "for_votes_amount", nullable = false))
    })
    private TokenAmount forVotes;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "against_votes_amount", nullable = false))
    })
    private TokenAmount againstVotes;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "abstain_votes_amount", nullable = false))
    })
    private TokenAmount abstainVotes;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "total_voting_power_amount", nullable = false))
    })
    private TokenAmount totalVotingPower;
}
```

### 3. 관계 매핑

#### 일대다 관계
```java
@Entity
@Table(name = "point_accounts")
public class PointAccount {
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PointTransaction> transactions;
}

@Entity
@Table(name = "point_transactions")
public class PointTransaction {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private PointAccount account;
}
```

#### 다대다 관계
```java
@Entity
@Table(name = "proposals")
public class Proposal {
    @ElementCollection
    @CollectionTable(name = "proposal_voters", 
        joinColumns = @JoinColumn(name = "proposal_id"))
    @Column(name = "voter_id")
    private Set<UserId> voters;
}
```

### 4. Repository 패턴

#### 도메인 Repository 인터페이스
```java
public interface PointAccountRepository {
    Optional<PointAccount> findByUserId(UserId userId);
    PointAccount save(PointAccount account);
    List<PointAccount> findAll();
}
```

#### JPA Repository 구현
```java
public interface PointAccountJpaRepository extends JpaRepository<PointAccount, PointAccountId> {
    Optional<PointAccount> findByUserId(UserId userId);
    boolean existsByUserId(UserId userId);
}
```

#### Adapter 구현
```java
@Repository
public class PointAccountRepositoryAdapter implements PointAccountRepository {
    private final PointAccountJpaRepository jpaRepository;
    
    @Override
    public Optional<PointAccount> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId);
    }
    
    @Override
    public PointAccount save(PointAccount account) {
        return jpaRepository.save(account);
    }
}
```

## 성능 최적화

### 1. 인덱스 전략

- **주요 조회 컬럼**: `user_id`, `status`, `created_at`
- **복합 인덱스**: 자주 함께 조회되는 컬럼들
- **고유 인덱스**: 중복 방지가 필요한 컬럼들

### 2. 쿼리 최적화

```java
// N+1 문제 방지
@Query("SELECT pa FROM PointAccount pa LEFT JOIN FETCH pa.transactions WHERE pa.userId = :userId")
Optional<PointAccount> findByUserIdWithTransactions(@Param("userId") UserId userId);

// 배치 처리
@Modifying
@Query("UPDATE PointAccount pa SET pa.balance = :balance WHERE pa.id = :id")
void updateBalance(@Param("id") PointAccountId id, @Param("balance") PointBalance balance);
```

### 3. 캐싱 전략

```java
@Cacheable("pointAccounts")
public Optional<PointAccount> findByUserId(UserId userId) {
    return jpaRepository.findByUserId(userId);
}

@CacheEvict("pointAccounts")
public PointAccount save(PointAccount account) {
    return jpaRepository.save(account);
}
```

## 데이터 마이그레이션

### 1. 스키마 버전 관리

```sql
-- V1__Create_initial_schema.sql
CREATE TABLE point_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    available_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    frozen_amount DECIMAL(20,8) NOT NULL DEFAULT 0
);

-- V2__Add_created_at_to_point_accounts.sql
ALTER TABLE point_accounts ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
```

### 2. 데이터 마이그레이션

```java
@Component
public class DataMigrationService {
    
    @Transactional
    public void migratePointAccounts() {
        // 마이그레이션 로직
    }
}
```

## 모니터링 및 로깅

### 1. 쿼리 성능 모니터링

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        statistics:
          enabled: true
          log_slow_query: true
          slow_query_threshold: 1000
```

### 2. 감사 로그

```java
@EntityListeners(AuditingEntityListener.class)
@Entity
public class PointAccount {
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}
```

## 보안 고려사항

1. **SQL 인젝션 방지**: JPA의 파라미터 바인딩 사용
2. **권한 기반 접근**: 데이터베이스 레벨 권한 설정
3. **암호화**: 민감한 데이터 암호화 저장
4. **백업**: 정기적인 데이터 백업

## 확장성 고려사항

1. **파티셔닝**: 대용량 테이블 파티셔닝
2. **샤딩**: 사용자별 데이터 샤딩
3. **읽기 전용 복제본**: 읽기 성능 향상
4. **NoSQL 도입**: 특정 용도에 맞는 NoSQL 사용 