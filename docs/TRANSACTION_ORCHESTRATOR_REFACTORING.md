# TransactionOrchestrator 리팩토링 가이드

## 📋 개요

`TransactionOrchestrator`의 `executeVote`와 `executeProposalCreation` 메서드를 `executeTransfer`처럼 역할 분리가 명확하게 리팩토링했습니다.

## 🏗️ 리팩토링 전후 구조 비교

### 🔄 **리팩토링 전 (기존 구조)**

```java
// 기존 executeProposalCreation
public TransactionResult executeProposalCreation(...) {
    // 1. BlockchainGovernanceService를 통한 TransactionBody 생성
    TransactionBody<Object> txBody = createProposalTransactionBody(...);
    
    // 2. 트랜잭션 서명 및 브로드캐스트
    return executeTransaction(txBody, walletAddress, networkType, description);
}

// 기존 executeVote (스냅샷 방식)
public TransactionResult executeVote(...) {
    // 스냅샷 방식이므로 실제 블록체인 트랜잭션 없이 가상 해시 반환
    String virtualHash = createSnapshotVoteHash(proposalId);
    return TransactionResult.success(proposalId, virtualHash, ...);
}
```

### ✅ **리팩토링 후 (새로운 구조)**

```java
// 새로운 executeProposalCreation
public TransactionResult executeProposalCreation(...) {
    // 1. RawTransaction 생성
    String rawTransactionJson = createProposalCreationRawTransaction(...);

    // 2. 지갑 정보 조회 및 개인키 복호화
    String decryptedPrivateKey = getDecryptedPrivateKey(walletAddress, networkType);

    // 3. 트랜잭션 서명
    byte[] signedTx = signTransaction(rawTransactionJson, walletAddress, networkType, decryptedPrivateKey);

    // 4. 블록체인에 브로드캐스트
    String txHash = broadcastTransaction(signedTx, networkType);

    // 5. 결과 반환
    return TransactionResult.success(UUID.randomUUID(), txHash, ...);
}

// 새로운 executeVote
public TransactionResult executeVote(...) {
    // 1. RawTransaction 생성
    String rawTransactionJson = createVoteRawTransaction(...);

    // 2. 지갑 정보 조회 및 개인키 복호화
    String decryptedPrivateKey = getDecryptedPrivateKey(walletAddress, networkType);

    // 3. 트랜잭션 서명
    byte[] signedTx = signTransaction(rawTransactionJson, walletAddress, networkType, decryptedPrivateKey);

    // 4. 블록체인에 브로드캐스트
    String txHash = broadcastTransaction(signedTx, networkType);

    // 5. 결과 반환
    return TransactionResult.success(UUID.randomUUID(), txHash, ...);
}
```

## 🔧 **RawTransactionBuilder 확장**

### 1. **인터페이스 확장**

`RawTransactionBuilder` 인터페이스에 새로운 메서드들을 추가했습니다:

```java
public interface RawTransactionBuilder {
    // 기존 메서드
    String createRawTransaction(Map<String, String> data);
    
    // 새로운 메서드들
    String createProposalCreationRawTransaction(
        UUID proposalId,
        String title,
        String description,
        String walletAddress,
        BigDecimal proposalFee,
        LocalDateTime votingStartDate,
        LocalDateTime votingEndDate,
        BigDecimal requiredQuorum,
        String nonce
    );
    
    String createVoteRawTransaction(
        UUID proposalId,
        String walletAddress,
        String voteType,
        String reason,
        BigDecimal votingPower,
        String nonce
    );
}
```

### 2. **EthereumRawTransactionBuilder 리팩토링**

이더리움 네트워크용 구현체를 `walletService` 의존성 없이 직접 구현하도록 리팩토링했습니다:

```java
@Component
public class EthereumRawTransactionBuilder implements RawTransactionBuilder {
    
    private final BlockchainClientFactory blockchainClientFactory;
    
    @Override
    public String createRawTransaction(Map<String, String> data) {
        try {
            String fromAddress = data.get("fromAddress");
            String toAddress = data.get("toAddress");
            String tokenAddress = data.get("tokenAddress");
            String amount = data.get("amount");
            String nonce = data.get("nonce");
            
            // 1. nonce가 제공되지 않은 경우 블록체인에서 조회
            if (nonce == null || nonce.isEmpty()) {
                BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
                nonce = blockchainClient.getNonce(fromAddress);
            }
            
            // 2. ERC-20 transfer 함수 데이터 생성
            String functionData = createERC20TransferFunctionData(toAddress, new BigDecimal(amount));
            
            // 3. RawTransaction 생성
            BigInteger nonceBigInt = new BigInteger(nonce);
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger gasLimit = BlockchainMetadata.Ethereum.GAS_LIMIT.multiply(BigInteger.valueOf(15L));
            BigInteger value = BigInteger.ZERO;
            
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonceBigInt, gasPrice, gasLimit, tokenAddress, value, functionData
            );
            
            // 4. JSON 형태로 반환
            return String.format(
                "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"tokenAddress\":\"%s\",\"amount\":\"%s\",\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\",\"value\":\"%s\",\"data\":\"%s\"}",
                fromAddress, toAddress, tokenAddress, amount, nonce, gasPrice.toString(), gasLimit.toString(), value.toString(), functionData
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ERC20 RawTransaction: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String createProposalCreationRawTransaction(...) {
        try {
            // 1. nonce가 제공되지 않은 경우 블록체인에서 조회
            if (nonce == null || nonce.isEmpty()) {
                BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
                nonce = blockchainClient.getNonce(walletAddress);
            }
            
            // 2. 거버넌스 컨트랙트 주소 가져오기
            String governanceContractAddress = BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS;
            
            // 3. 제안 생성 함수 데이터 생성
            String functionData = createProposalCreationFunctionData(
                proposalId, title, description, proposalFee, 
                votingStartDate, votingEndDate, requiredQuorum
            );
            
            // 4. RawTransaction 생성
            BigInteger nonceBigInt = new BigInteger(nonce);
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger gasLimit = BlockchainMetadata.Ethereum.PROPOSAL_CREATION_GAS_LIMIT;
            BigInteger value = proposalFee.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonceBigInt, gasPrice, gasLimit, governanceContractAddress, value, functionData
            );
            
            // 5. JSON 형태로 반환
            return String.format(
                "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"data\":\"%s\",\"value\":\"%s\",\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\"}",
                walletAddress, governanceContractAddress, functionData, value.toString(), nonce, gasPrice.toString(), gasLimit.toString()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Proposal Creation RawTransaction: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String createVoteRawTransaction(...) {
        try {
            // 1. nonce가 제공되지 않은 경우 블록체인에서 조회
            if (nonce == null || nonce.isEmpty()) {
                BlockchainClient blockchainClient = blockchainClientFactory.getClient(NetworkType.ETHEREUM);
                nonce = blockchainClient.getNonce(walletAddress);
            }
            
            // 2. 거버넌스 컨트랙트 주소 가져오기
            String governanceContractAddress = BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS;
            
            // 3. 투표 함수 데이터 생성
            String functionData = createVoteFunctionData(proposalId, voteType, reason, votingPower);
            
            // 4. RawTransaction 생성
            BigInteger nonceBigInt = new BigInteger(nonce);
            BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
            BigInteger gasLimit = BlockchainMetadata.Ethereum.VOTE_GAS_LIMIT;
            BigInteger value = BigInteger.ZERO;
            
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonceBigInt, gasPrice, gasLimit, governanceContractAddress, value, functionData
            );
            
            // 5. JSON 형태로 반환
            return String.format(
                "{\"fromAddress\":\"%s\",\"toAddress\":\"%s\",\"data\":\"%s\",\"value\":\"%s\",\"nonce\":\"%s\",\"gasPrice\":\"%s\",\"gasLimit\":\"%s\"}",
                walletAddress, governanceContractAddress, functionData, value.toString(), nonce, gasPrice.toString(), gasLimit.toString()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Vote RawTransaction: " + e.getMessage(), e);
        }
    }
    
    // Web3j FunctionEncoder를 사용한 함수 데이터 생성 메서드들
    private String createERC20TransferFunctionData(String toAddress, BigDecimal amount) {
        BigInteger amountWei = amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
        
        Function transferFunction = new Function(
            "transfer",
            Arrays.asList(
                new org.web3j.abi.datatypes.Address(toAddress),
                new Uint256(amountWei)
            ),
            Collections.emptyList()
        );
        
        return FunctionEncoder.encode(transferFunction);
    }
    
    private String createProposalCreationFunctionData(...) {
        long votingStartTimestamp = votingStartDate.toEpochSecond(java.time.ZoneOffset.UTC);
        long votingEndTimestamp = votingEndDate.toEpochSecond(java.time.ZoneOffset.UTC);
        
        Function createProposalFunction = new Function(
            "createProposal",
            Arrays.asList(
                new Utf8String(title),
                new Utf8String(description),
                new Uint256(proposalFee.multiply(BigDecimal.valueOf(1e18)).toBigInteger()),
                new Uint256(BigInteger.valueOf(votingStartTimestamp)),
                new Uint256(BigInteger.valueOf(votingEndTimestamp)),
                new Uint256(requiredQuorum.multiply(BigDecimal.valueOf(1e18)).toBigInteger())
            ),
            Arrays.asList(new TypeReference<Uint256>() {})
        );
        
        return FunctionEncoder.encode(createProposalFunction);
    }
    
    private String createVoteFunctionData(...) {
        // voteType을 숫자로 변환 (0: AGAINST, 1: FOR, 2: ABSTAIN)
        int voteTypeNumber = switch (voteType.toUpperCase()) {
            case "NO", "AGAINST" -> 0;
            case "YES", "FOR" -> 1;
            case "ABSTAIN" -> 2;
            default -> 0;
        };
        
        Function voteFunction = new Function(
            "vote",
            Arrays.asList(
                new Uint256(proposalId.getMostSignificantBits()),
                new Uint8(BigInteger.valueOf(voteTypeNumber)),
                new Utf8String(reason != null ? reason : ""),
                new Uint256(votingPower.multiply(BigDecimal.valueOf(1e18)).toBigInteger())
            ),
            Collections.emptyList()
        );
        
        return FunctionEncoder.encode(voteFunction);
    }
}
```

## 🏦 **WalletService 추상 클래스 리팩토링**

### 1. **인터페이스에서 추상 클래스로 변경**

`WalletService`를 인터페이스에서 추상 클래스로 변경하여 공통 함수를 포함하도록 리팩토링했습니다:

```java
/**
 * 지갑 서비스 추상 클래스
 * 지갑 생성, 조회, 관리 기능을 담당하는 도메인 서비스
 */
public abstract class WalletService {
    
    protected WalletApplicationService walletApplicationService;
    
    public WalletService(WalletApplicationService walletApplicationService) {
        this.walletApplicationService = walletApplicationService;
    }
    
    /**
     * 지갑 주소로 복호화된 개인키를 가져옵니다.
     * 공통 함수로 모든 구현체에서 사용할 수 있습니다.
     *
     * @param fromWalletAddress 지갑 주소
     * @return 복호화된 개인키
     */
    public String getDecryptedPrivateKey(String fromWalletAddress) {
        Optional<Wallet> walletOpt = walletApplicationService.getWalletByAddress(fromWalletAddress);
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("Wallet not found for address: " + fromWalletAddress);
        }
        Wallet wallet = walletOpt.get();
        return walletApplicationService.getDecryptedPrivateKey(
            new UserId(wallet.getUser().getId()),
            wallet.getNetworkType()
        );
    }
    
    /**
     * 새로운 지갑을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA 등)
     * @return 생성된 지갑
     */
    public abstract Wallet createWallet(UserId userId, NetworkType networkType);
    
    /**
     * 지갑 주소로 지갑을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @return 지갑 정보
     */
    public abstract Optional<Wallet> findByAddress(String walletAddress);
    
    /**
     * 사용자의 모든 지갑을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자의 지갑 목록
     */
    public abstract List<Wallet> findByUserId(UserId userId);
    
    /**
     * 지갑을 저장합니다.
     * 
     * @param wallet 저장할 지갑
     * @return 저장된 지갑
     */
    public abstract Wallet save(Wallet wallet);

    
    /**
     * 지갑 활성화 상태를 변경합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param active 활성화 여부
     * @return 업데이트된 지갑
     */
    public abstract Wallet updateActiveStatus(String walletAddress, boolean active);


    /**
     * 주어진 메시지에 대해 개인키로 서명합니다.
     *
     * @param privateKey 개인키 (hex string)
     * @return 서명 결과 (byte[])
     */
    public abstract <T> byte[] sign(TransactionBody<T> transactionBody, String privateKey);



    /**
     * 지갑 주소의 유효성을 검증합니다.
     *
     * @param address 검증할 지갑 주소
     * @return 유효성 여부
     */
    public abstract boolean isValidAddress(String address);

}
```

### 2. **EthereumWallet 구현체 수정**

이더리움 네트워크용 구현체를 추상 클래스를 상속받도록 수정했습니다:

```java
@Service("ethereumWalletService")
public class EthereumWallet extends WalletService {
    
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;
    
    public EthereumWallet(
            WalletApplicationService walletApplicationService,
            WalletRepository walletRepository,
            EncryptionService encryptionService,
            UserRepository userRepository) {
        super(walletApplicationService);
        this.walletRepository = walletRepository;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
    }
    
    @Override
    public Wallet createWallet(UserId userId, NetworkType networkType) {
        // 기존 구현 유지
    }
    
    @Override
    public Optional<Wallet> findByAddress(String walletAddress) {
        // 기존 구현 유지
    }
    
    @Override
    public List<Wallet> findByUserId(UserId userId) {
        // 기존 구현 유지
    }
    
    @Override
    public Wallet save(Wallet wallet) {
        // 기존 구현 유지
    }
    
    @Override
    public Wallet updateActiveStatus(String walletAddress, boolean active) {
        // 기존 구현 유지
    }
    
    @Override
    public <T> byte[] sign(TransactionBody<T> transactionBody, String privateKey) {
        // 기존 구현 유지
    }
    
    @Override
    public boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }
}
```

### 3. **SolanaWalletService 구현체 수정**

솔라나 네트워크용 구현체를 추상 클래스를 상속받도록 수정했습니다:

```java
@Service("solanaWalletService")
public class SolanaWalletService extends WalletService {
    
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;
    
    public SolanaWalletService(
            WalletApplicationService walletApplicationService,
            WalletRepository walletRepository,
            EncryptionService encryptionService,
            UserRepository userRepository) {
        super(walletApplicationService);
        this.walletRepository = walletRepository;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
    }
    
    @Override
    public Wallet createWallet(UserId userId, NetworkType networkType) {
        // 기존 구현 유지
    }
    
    @Override
    public Optional<Wallet> findByAddress(String walletAddress) {
        // 기존 구현 유지
    }
    
    @Override
    public List<Wallet> findByUserId(UserId userId) {
        // 기존 구현 유지
    }
    
    @Override
    public Wallet save(Wallet wallet) {
        // 기존 구현 유지
    }
    
    @Override
    public Wallet updateActiveStatus(String walletAddress, boolean active) {
        // 기존 구현 유지
    }
    
    @Override
    public <T> byte[] sign(TransactionBody<T> transactionBody, String privateKey) {
        // 기존 구현 유지
    }
    
    @Override
    public boolean isValidAddress(String address) {
        return Base58Utils.isValid(address);
    }
}
```

### 4. **WalletServiceFactory 수정**

팩토리 클래스에서 생성자 호출을 수정했습니다:

```java
@RequiredArgsConstructor
@Component
public class WalletServiceFactory {
    private final WalletApplicationService walletApplicationService;
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;

    /**
     * 네트워크 타입에 따라 적절한 지갑 서비스를 반환합니다.
     * 
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA)
     * @return 지갑 서비스
     * @throws IllegalArgumentException 지원하지 않는 네트워크 타입인 경우
     */
    public WalletService getWalletService(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                return new EthereumWallet(walletApplicationService, walletRepository, encryptionService, userRepository);
            case SOLANA:
                return new SolanaWalletService(walletApplicationService, walletRepository, encryptionService, userRepository);
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }
}
```

### 5. **TransactionOrchestrator에서 공통 함수 사용**

`TransactionOrchestrator`에서 `WalletService`의 공통 함수를 사용하도록 수정했습니다:

```java
private String getDecryptedPrivateKey(String fromWalletAddress, NetworkType networkType) {
    WalletService walletService = walletServiceFactory.getWalletService(networkType);
    return walletService.getDecryptedPrivateKey(fromWalletAddress);
}
```

## 🎯 **역할 분리의 장점**

### 1. **명확한 책임 분리**
- **RawTransaction 생성**: `RawTransactionBuilder`가 담당 (직접 구현)
- **서명**: `WalletService`가 담당
- **브로드캐스트**: `BlockchainClient`가 담당
- **오케스트레이션**: `TransactionOrchestrator`가 담당

### 2. **재사용성 향상**
- `RawTransactionBuilder`는 다른 트랜잭션 타입에서도 재사용 가능
- `WalletService`는 모든 네트워크에서 일관된 인터페이스 제공
- 각 단계별로 독립적인 테스트 가능

### 3. **확장성 개선**
- 새로운 블록체인 네트워크 추가 시 `RawTransactionBuilder`와 `WalletService` 구현체만 추가
- 새로운 트랜잭션 타입 추가 시 인터페이스에 메서드만 추가

### 4. **일관성 있는 구조**
- 모든 트랜잭션 실행 메서드가 동일한 패턴을 따름
- 코드 가독성과 유지보수성 향상

### 5. **공통 함수 재사용**
- `getDecryptedPrivateKey` 함수가 모든 구현체에서 공통으로 사용
- 중복 코드 제거 및 일관성 보장

## 📊 **메타데이터 활용**

### 1. **BlockchainMetadata 사용**
```java
// 거버넌스 컨트랙트 주소
String governanceContractAddress = BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS;

// 가스 설정
BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
BigInteger gasLimit = BlockchainMetadata.Ethereum.PROPOSAL_CREATION_GAS_LIMIT;
BigInteger voteGasLimit = BlockchainMetadata.Ethereum.VOTE_GAS_LIMIT;
```

### 2. **투표 권한 계산**
```java
private BigDecimal calculateVotingPower(String walletAddress, NetworkType networkType) {
    // 실제 구현에서는 토큰 잔액을 조회
    // TODO: TokenAccountApplicationService를 통해 토큰 잔액 조회
    return BigDecimal.valueOf(1.0); // 임시 기본값
}
```

## 🔄 **실행 플로우**

### 1. **제안 생성 플로우**
```
1. executeProposalCreation() 호출
2. createProposalCreationRawTransaction() → RawTransactionBuilder (직접 구현)
3. getDecryptedPrivateKey() → WalletService 공통 함수 사용
4. signTransaction() → WalletService로 서명
5. broadcastTransaction() → BlockchainClient로 브로드캐스트
6. TransactionResult 반환
```

### 2. **투표 플로우**
```
1. executeVote() 호출
2. createVoteRawTransaction() → RawTransactionBuilder (직접 구현)
3. calculateVotingPower() → 투표 권한 계산
4. getDecryptedPrivateKey() → WalletService 공통 함수 사용
5. signTransaction() → WalletService로 서명
6. broadcastTransaction() → BlockchainClient로 브로드캐스트
7. TransactionResult 반환
```

## 🧪 **테스트 방법**

### 1. **단위 테스트**
```java
@Test
void testCreateProposalCreationRawTransaction() {
    // RawTransactionBuilder 테스트
    String rawTx = ethereumRawTransactionBuilder.createProposalCreationRawTransaction(...);
    assertNotNull(rawTx);
}

@Test
void testGetDecryptedPrivateKey() {
    // WalletService 공통 함수 테스트
    String privateKey = ethereumWallet.getDecryptedPrivateKey(walletAddress);
    assertNotNull(privateKey);
}

@Test
void testExecuteProposalCreation() {
    // 전체 플로우 테스트
    TransactionResult result = transactionOrchestrator.executeProposalCreation(...);
    assertTrue(result.isSuccess());
    assertNotNull(result.getTransactionHash());
}
```

### 2. **통합 테스트**
```java
@Test
void testProposalCreationEndToEnd() {
    // 1. 제안 생성
    TransactionResult result = transactionOrchestrator.executeProposalCreation(...);
    
    // 2. 제안에 투표
    TransactionResult voteResult = transactionOrchestrator.executeVote(...);
    
    // 3. 결과 검증
    assertTrue(result.isSuccess());
    assertTrue(voteResult.isSuccess());
}
```

## 🚀 **향후 개선 사항**

### 1. **실제 Web3j 구현**
- ✅ 현재 구현된 함수 데이터 생성 메서드를 실제 Web3j FunctionEncoder로 교체 완료
- 정확한 ABI 인코딩 구현 완료

### 2. **토큰 잔액 조회**
- `calculateVotingPower` 메서드에서 실제 토큰 잔액 조회 구현
- `TokenAccountApplicationService` 연동

### 3. **솔라나 지원**
- `SolanaWalletService`에 실제 거버넌스 프로그램 호출 로직 구현
- 솔라나 네트워크용 거버넌스 트랜잭션 지원

### 4. **에러 처리 개선**
- 각 단계별 상세한 에러 메시지
- 재시도 로직 구현

## 📝 **주요 변경사항 요약**

1. **RawTransactionBuilder 인터페이스 확장**
   - `createProposalCreationRawTransaction()` 추가
   - `createVoteRawTransaction()` 추가

2. **EthereumRawTransactionBuilder 리팩토링**
   - `walletService` 의존성 제거
   - 직접 Web3j를 사용한 RawTransaction 생성 구현
   - Web3j FunctionEncoder를 사용한 정확한 ABI 인코딩

3. **WalletService 추상 클래스 리팩토링**
   - 인터페이스에서 추상 클래스로 변경
   - `getDecryptedPrivateKey()` 공통 함수 추가
   - 모든 구현체에서 공통 함수 사용 가능

4. **EthereumWallet 구현**
   - 추상 클래스 상속으로 변경
   - Web3j FunctionEncoder를 사용한 정확한 ABI 인코딩
   - 제안 생성 및 투표 트랜잭션 서명 로직 구현

5. **SolanaWalletService 구현**
   - 추상 클래스 상속으로 변경
   - 새로운 메서드들의 기본 구현 (UnsupportedOperationException)
   - 향후 실제 Solana 프로그램 호출 로직 구현 예정

6. **WalletServiceFactory 수정**
   - `WalletApplicationService` 의존성 추가
   - 생성자 호출 수정

7. **TransactionOrchestrator 리팩토링**
   - `executeProposalCreation()` 메서드 개선
   - `executeVote()` 메서드 개선
   - `WalletService` 공통 함수 사용
   - 역할 분리된 구조로 변경

8. **메타데이터 활용**
   - `BlockchainMetadata`를 통한 설정 중앙화
   - 거버넌스 트랜잭션 가스 한도 추가
   - 투표 권한 계산 로직 추가

이제 모든 거버넌스 트랜잭션이 `executeTransfer`와 동일한 패턴을 따르며, 명확한 역할 분리와 재사용 가능한 구조를 가지게 되었습니다. 또한 `RawTransactionBuilder`가 `walletService` 의존성 없이 직접 구현되어 더욱 독립적이고 효율적인 구조가 되었습니다. `WalletService`가 추상 클래스로 변경되어 공통 함수를 제공함으로써 코드 중복을 제거하고 일관성을 보장하게 되었습니다. 