
# 🚀 블록체인 기반 재화 교환 서비스

## 📋 프로젝트 개요

**과제 주제**: 블록체인 기반 재화 교환 서비스  
**개발 기간**: 2024년  
**프로젝트 유형**: Web2 + Web3 통합 플랫폼

### �� 핵심 기능
- **Web2 포인트 시스템**: 전통적인 포인트 적립/사용 시스템
- **블록체인 거버넌스 토큰**: 투표 기능이 포함된 ERC-20 토큰
- **교환 시스템**: 포인트 ↔ 토큰 고정 비율 교환
- **웹 애플리케이션**: 모든 기능을 테스트할 수 있는 통합 플랫폼

---

## 🛠 기술 스택

### **Backend (Web2 Service)**
| 기술 | 버전 | 선택 이유 |
|------|------|-----------|
| **Java** | 17 | 엔터프라이즈급 안정성, 풍부한 생태계, 블록체인 라이브러리 지원 |
| **Spring Boot** | 3.x | 빠른 개발, 자동 설정, 마이크로서비스 아키텍처 지원 |
| **Spring Security** | 6.x | JWT 기반 인증/인가, 보안 강화 |
| **Spring Data JPA** | 3.x | 객체 관계 매핑, 데이터 접근 추상화 |
| **H2 Database** | 2.x | 개발용 인메모리 DB, 빠른 프로토타이핑 |

### **Blockchain (Smart Contract)**
| 기술 | 버전 | 선택 이유 |
|------|------|-----------|
| **Solidity** | 0.8.x | Ethereum 스마트 컨트랙트 표준, 안정성과 보안 |
| **Hardhat** | 2.x | 개발 환경, 테스트, 배포 자동화 |
| **Web3j** | 4.x | Java ↔ Ethereum 연동, 타입 안전성 |

### **Frontend**
| 기술 | 버전 | 선택 이유 |
|------|------|-----------|
| **React** | 18.x | 컴포넌트 기반 UI, 상태 관리 용이 |
| **TypeScript** | 5.x | 타입 안전성, 개발 생산성 향상 |
| **Tailwind CSS** | 3.x | 유틸리티 퍼스트 CSS, 빠른 스타일링 |

### **개발 환경**
| 도구 | 선택 이유 |
|------|-----------|
| **macOS** | Unix 기반, 개발자 친화적, 도구 통합 우수 |
| **IntelliJ IDEA** | Java 개발 최적화, 스마트 코드 완성, 디버깅 강력 |
| **VS Code** | 경량화, 확장성, 멀티 언어 지원 |
| **Git** | 버전 관리, 협업, 브랜치 전략 |

---

## 🏗 아키텍처 설계

### **Domain-Driven Design (DDD) 적용**

```
�� governance/
├── 🏛 Domain Layer (도메인 계층)
│   ├── 📁 user/          # 사용자 도메인
│   ├── �� wallet/        # 지갑 도메인
│   ├── �� token/         # 토큰 도메인
│   ├── �� point/         # 포인트 도메인
│   ├── �� governance/    # 거버넌스 도메인
│   ├── �� exchange/      # 교환 도메인
│   └── �� blockchain/    # 블록체인 도메인
├── 🎯 Application Layer (애플리케이션 계층)
│   ├── �� service/       # 비즈니스 로직
│   └── 📁 dto/          # 데이터 전송 객체
├── 🏗 Infrastructure Layer (인프라 계층)
│   ├── 📁 repository/    # 데이터 접근
│   ├── 📁 controller/    # REST API
│   └── �� config/       # 설정
└── 📁 shared/           # 공통 모듈
```

### **모듈화 전략**

#### **1. 도메인별 모듈 분리**
- **User Module**: 사용자 관리, 인증/인가
- **Wallet Module**: 지갑 생성, 관리, 서명
- **Token Module**: ERC-20 토큰 관리
- **Point Module**: 포인트 적립/사용
- **Governance Module**: 투표, 제안 관리
- **Exchange Module**: 포인트 ↔ 토큰 교환
- **Blockchain Module**: 블록체인 연동

#### **2. 공통 모듈 (Shared)**
- **Security**: 암호화, JWT 관리
- **Blockchain**: 트랜잭션, 유틸리티
- **Domain**: 공통 도메인 모델

---

## 🔧 코딩 스타일 & 설계 원칙

### **1. 확장성 (Scalability)**
```java
// 인터페이스 기반 설계로 확장성 확보
public interface BlockchainClient {
    NetworkType getNetworkType();
    String getLatestBlockHash();
    String broadcastTransaction(String signedTransaction);
}

// 구현체 추가로 새로운 블록체인 지원 가능
@Service("ethereumBlockchainClient")
public class EthereumBlockchainClient implements BlockchainClient { }

@Service("solanaBlockchainClient") 
public class SolanaBlockchainClient implements BlockchainClient { }
```

### **2. 재사용성 (Reusability)**
```java
// 공통 유틸리티 클래스
@Component
public class JsonRpcClient {
    public <T> T sendRequest(String rpcUrl, Object request, TypeReference<T> typeReference);
}

// 팩토리 패턴으로 객체 생성 추상화
@Component
public class WalletServiceFactory {
    public WalletService getWalletService(NetworkType networkType);
}
```

### **3. 효율성 (Efficiency)**
```java
// 비동기 처리로 성능 최적화
@Async
public CompletableFuture<String> broadcastTransactionAsync(String signedTransaction);

// 캐싱으로 반복 요청 최소화
@Cacheable("blockchain-data")
public String getLatestBlockHash();
```

### **4. 최적화 (Optimization)**
```java
// 배치 처리로 데이터베이스 부하 감소
@Transactional
public void processBatchTransactions(List<Transaction> transactions);

// 연결 풀링으로 리소스 효율적 사용
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        // HikariCP 연결 풀 설정
    }
}
```

### **5. 모듈화 (Modularity)**
```java
// 도메인별 패키지 구조
com.bloominggrace.governance
├── user/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── wallet/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
└── shared/
    ├── blockchain/
    └── security/
```

---

## �� 주요 기능 구현

### **1. Web2 포인트 시스템**
```java
@Entity
@Table(name = "point_accounts")
public class PointAccount extends AggregateRoot {
    @Embedded
    private UserId userId;
    
    @Embedded
    private PointAmount balance;
    
    public void earnPoints(PointAmount amount) {
        this.balance = this.balance.add(amount);
        addDomainEvent(new PointsEarnedEvent(this.userId, amount));
    }
    
    public void usePoints(PointAmount amount) {
        if (this.balance.isLessThan(amount)) {
            throw new InsufficientPointsException();
        }
        this.balance = this.balance.subtract(amount);
        addDomainEvent(new PointsUsedEvent(this.userId, amount));
    }
}
```

### **2. 블록체인 거버넌스 토큰**
```solidity
// GovernanceToken.sol
contract GovernanceToken is ERC20 {
    mapping(address => uint256) public votingPower;
    mapping(uint256 => Proposal) public proposals;
    
    function createProposal(
        string memory title,
        string memory description,
        uint256 startTime,
        uint256 endTime
    ) external returns (uint256) {
        require(votingPower[msg.sender] >= MIN_PROPOSAL_POWER, "Insufficient voting power");
        
        uint256 proposalId = proposalCount++;
        proposals[proposalId] = Proposal({
            title: title,
            description: description,
            startTime: startTime,
            endTime: endTime,
            creator: msg.sender,
            yesVotes: 0,
            noVotes: 0
        });
        
        return proposalId;
    }
    
    function vote(uint256 proposalId, bool support) external {
        require(block.timestamp >= proposals[proposalId].startTime, "Voting not started");
        require(block.timestamp <= proposals[proposalId].endTime, "Voting ended");
        
        uint256 power = votingPower[msg.sender];
        require(power > 0, "No voting power");
        
        if (support) {
            proposals[proposalId].yesVotes += power;
        } else {
            proposals[proposalId].noVotes += power;
        }
    }
}
```

### **3. 교환 시스템**
```java
@Service
@Transactional
public class ExchangeApplicationService {
    
    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("100"); // 1 토큰 = 100 포인트
    
    public ExchangeResult exchangePointsToTokens(UserId userId, PointAmount points) {
        // 1. 포인트 차감
        PointAccount pointAccount = pointAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new PointAccountNotFoundException(userId));
        
        pointAccount.usePoints(points);
        pointAccountRepository.save(pointAccount);
        
        // 2. 토큰 계산
        TokenAmount tokens = points.divide(EXCHANGE_RATE);
        
        // 3. 블록체인 토큰 전송
        String transactionHash = blockchainService.mintTokens(userId, tokens);
        
        // 4. 교환 기록 저장
        ExchangeRecord record = new ExchangeRecord(userId, points, tokens, transactionHash);
        exchangeRepository.save(record);
        
        return ExchangeResult.success(tokens, transactionHash);
    }
}
```

---

## �� 테스트 전략

### **1. 단위 테스트**
```java
@ExtendWith(MockitoExtension.class)
class ExchangeApplicationServiceTest {
    
    @Mock
    private PointAccountRepository pointAccountRepository;
    
    @Mock
    private BlockchainService blockchainService;
    
    @InjectMocks
    private ExchangeApplicationService exchangeService;
    
    @Test
    @DisplayName("포인트를 토큰으로 교환할 수 있다")
    void exchangePointsToTokens() {
        // Given
        UserId userId = new UserId(UUID.randomUUID());
        PointAmount points = new PointAmount(1000);
        PointAccount pointAccount = new PointAccount(userId, new PointAmount(2000));
        
        when(pointAccountRepository.findByUserId(userId))
            .thenReturn(Optional.of(pointAccount));
        when(blockchainService.mintTokens(userId, new TokenAmount(10)))
            .thenReturn("0x123...");
        
        // When
        ExchangeResult result = exchangeService.exchangePointsToTokens(userId, points);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTokens()).isEqualTo(new TokenAmount(10));
        verify(pointAccountRepository).save(any(PointAccount.class));
    }
}
```

### **2. 통합 테스트**
```java
@SpringBootTest
@AutoConfigureTestDatabase
class ExchangeIntegrationTest {
    
    @Autowired
    private ExchangeApplicationService exchangeService;
    
    @Autowired
    private PointAccountRepository pointAccountRepository;
    
    @Test
    @DisplayName("전체 교환 플로우가 정상적으로 작동한다")
    void completeExchangeFlow() {
        // Given
        UserId userId = new UserId(UUID.randomUUID());
        PointAccount account = new PointAccount(userId, new PointAmount(1000));
        pointAccountRepository.save(account);
        
        // When
        ExchangeResult result = exchangeService.exchangePointsToTokens(
            userId, new PointAmount(500)
        );
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        PointAccount updatedAccount = pointAccountRepository.findByUserId(userId).get();
        assertThat(updatedAccount.getBalance()).isEqualTo(new PointAmount(500));
    }
}
```

## �� 핵심 설계 패턴

### **1. AdminWalletService - 캐싱 패턴**

```java
@Service
public class AdminWalletService {
    
    @Cacheable("blockchain-data")
    public String getLatestBlockHash(NetworkType networkType) {
        BlockchainClient client = blockchainClientFactory.getClient(networkType);
        return client.getLatestBlockHash();
    }
    
    @Cacheable("gas-price")
    public String getGasPrice(NetworkType networkType) {
        BlockchainClient client = blockchainClientFactory.getClient(networkType);
        return client.getGasPrice();
    }
    
    @Cacheable("nonce")
    public String getNonce(String address, NetworkType networkType) {
        BlockchainClient client = blockchainClientFactory.getClient(networkType);
        return client.getNonce(address);
    }
}
```

**장점:**
- ✅ **성능 향상**: 반복적인 블록체인 RPC 호출 최소화
- ✅ **비용 절약**: 가스 가격 조회 등 빈번한 호출 비용 감소
- ✅ **응답 속도**: 캐시된 데이터로 즉시 응답

---

### **2. RawTransactionBuilder - 템플릿 패턴**

```java
// 인터페이스 정의
public interface RawTransactionBuilder {
    String createRawTransaction(Map<String, String> data);
    String createProposalCreationRawTransaction(UUID proposalId, String title, String description, String walletAddress, BigDecimal proposalFee, LocalDateTime votingStartDate, LocalDateTime votingEndDate, BigDecimal requiredQuorum, String nonce);
    String createVoteRawTransaction(BigInteger proposalCount, UUID proposalId, String walletAddress, String voteType, String reason, BigDecimal votingPower, String nonce);
    String createDelegationRawTransaction(String delegatorWalletAddress, String delegateeWalletAddress, NetworkType networkType);
}

// 팩토리 패턴
@Component
public class RawTransactionBuilderFactory {
    private final Map<NetworkType, RawTransactionBuilder> builders;
    
    public RawTransactionBuilder getBuilder(NetworkType networkType) {
        return builders.get(networkType);
    }
}

// 호출부 - 추상적 호출
@Service
public class GovernanceService {
    public String createProposal(NetworkType networkType, ProposalData data) {
        RawTransactionBuilder builder = rawTransactionBuilderFactory.getBuilder(networkType);
        return builder.createProposalCreationRawTransaction(
            data.getProposalId(), data.getTitle(), data.getDescription(),
            data.getWalletAddress(), data.getProposalFee(),
            data.getVotingStartDate(), data.getVotingEndDate(),
            data.getRequiredQuorum(), null
        );
    }
}
```

**장점:**
- ✅ **추상화**: 호출부는 네트워크별 구현 세부사항 몰라도 됨
- ✅ **확장성**: 새로운 블록체인 추가 시 기존 코드 변경 없음
- ✅ **일관성**: 모든 네트워크가 동일한 인터페이스 구현

---

### **3. 팩토리 패턴들**

#### **WalletServiceFactory**
```java
@Component
public class WalletServiceFactory {
    private final Map<NetworkType, WalletService> services;
    
    public WalletService getWalletService(NetworkType networkType) {
        return services.get(networkType);
    }
}
```

#### **BlockchainClientFactory**
```java
@Component
public class BlockchainClientFactory {
    private final Map<NetworkType, BlockchainClient> clients;
    
    public BlockchainClient getClient(NetworkType networkType) {
        return clients.get(networkType);
    }
}
```

#### **TransactionBuilderFactory**
```java
@Component
public class TransactionBuilderFactory {
    private final Map<NetworkType, TransactionBuilder> builders;
    
    public TransactionBuilder getBuilder(NetworkType networkType) {
        return builders.get(networkType);
    }
}
```

**팩토리 패턴 장점:**
- ✅ **의존성 주입**: 런타임에 적절한 구현체 선택
- ✅ **테스트 용이성**: Mock 객체로 쉽게 교체 가능
- ✅ **확장성**: 새로운 네트워크 추가 시 Map에만 추가
- ✅ **단일 책임**: 각 팩토리가 특정 타입의 객체만 생성

---

### **4. TransactionOrchestrator - 오케스트레이션 패턴**

```java
@Service
public class TransactionOrchestrator {
    
    public TransactionResult executeProposalCreation(ProposalData data) {
        try {
            // 1. RawTransaction 생성
            RawTransactionBuilder builder = rawTransactionBuilderFactory.getBuilder(data.getNetworkType());
            String rawTransaction = builder.createProposalCreationRawTransaction(
                data.getProposalId(), data.getTitle(), data.getDescription(),
                data.getWalletAddress(), data.getProposalFee(),
                data.getVotingStartDate(), data.getVotingEndDate(),
                data.getRequiredQuorum(), null
            );

            // 2. 지갑 서명
            WalletService walletService = walletServiceFactory.getWalletService(data.getNetworkType());
            byte[] signedTx = walletService.sign(rawTransaction, data.getPrivateKey());

            // 3. 블록체인 브로드캐스트
            BlockchainClient client = blockchainClientFactory.getClient(data.getNetworkType());
            String txHash = client.broadcastTransaction(signedTx);

            return TransactionResult.success(txHash);
        } catch (Exception e) {
            return TransactionResult.failure(e.getMessage());
        }
    }
}
```

**오케스트레이션 패턴 장점:**
- ✅ **복잡한 플로우 관리**: 여러 서비스의 협력을 조율
- ✅ **에러 처리**: 중앙화된 에러 처리 및 롤백
- ✅ **트랜잭션 관리**: 전체 플로우의 원자성 보장
- ✅ **모니터링**: 전체 프로세스 추적 가능

---

## 🎯 설계 패턴 통합 효과

### **성능 최적화**
- **팩토리**: 객체 생성 오버헤드 최소화
- **템플릿**: 코드 재사용으로 개발 시간 단축

### **확장성**
- **새로운 블록체인 추가**: 기존 코드 변경 없이 구현체만 추가
- **새로운 기능**: 인터페이스 확장으로 모든 구현체에 자동 적용
- **마이크로서비스**: 각 팩토리를 독립적인 서비스로 분리 가능

### **유지보수성**
- **단일 책임**: 각 클래스가 명확한 역할
- **의존성 역전**: 고수준 모듈이 저수준 모듈에 의존하지 않음
- **테스트 용이성**: Mock 객체로 격리된 테스트 가능

이러한 설계 패턴을 통해 **확장 가능하고, 성능이 최적화된, 유지보수가 용이한** 블록체인 애플리케이션을 구축했습니다! 🚀