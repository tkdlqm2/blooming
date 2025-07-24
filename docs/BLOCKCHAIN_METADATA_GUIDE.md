# BlockchainMetadata 사용 가이드

## 📋 개요

`BlockchainMetadata` 클래스는 블록체인별 메타데이터를 static으로 패키지 전역에서 접근 가능하게 관리하는 클래스입니다. 이더리움과 솔라나 블록체인의 컨트랙트 주소, 관리자 주소, 가스 설정, 수수료 비용 등을 중앙화하여 관리합니다.

## 🏗️ 구조

```
BlockchainMetadata
├── Ethereum (이더리움 메타데이터)
├── Solana (솔라나 메타데이터)
├── Common (공통 설정)
├── Environment (환경별 설정)
└── Utils (유틸리티 메서드)
```

## 🚀 사용법

### 1. 이더리움 메타데이터

```java
import com.bloominggrace.governance.shared.domain.model.BlockchainMetadata;

// ERC20 토큰 정보
String contractAddress = BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS;
String symbol = BlockchainMetadata.Ethereum.ERC20_SYMBOL;
int decimals = BlockchainMetadata.Ethereum.ERC20_DECIMALS;

// 관리자 지갑 정보
String adminAddress = BlockchainMetadata.Ethereum.ADMIN_WALLET_ADDRESS;
String adminPrivateKey = BlockchainMetadata.Ethereum.ADMIN_PRIVATE_KEY;

// 가스 설정
BigInteger gasLimit = BlockchainMetadata.Ethereum.GAS_LIMIT;
BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
BigInteger maxFeePerGas = BlockchainMetadata.Ethereum.MAX_FEE_PER_GAS;

// 수수료 설정
BigDecimal transactionFee = BlockchainMetadata.Ethereum.TRANSACTION_FEE;
BigDecimal exchangeFeePercentage = BlockchainMetadata.Ethereum.EXCHANGE_FEE_PERCENTAGE;

// 네트워크 설정
String networkName = BlockchainMetadata.Ethereum.NETWORK_NAME;
long chainId = BlockchainMetadata.Ethereum.CHAIN_ID;
String explorerUrl = BlockchainMetadata.Ethereum.EXPLORER_URL;

// 컨트랙트 설정
String governanceContract = BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS;
String stakingContract = BlockchainMetadata.Ethereum.STAKING_CONTRACT_ADDRESS;
```

### 2. 솔라나 메타데이터

```java
// SPL 토큰 정보
String tokenMintAddress = BlockchainMetadata.Solana.SPL_TOKEN_MINT_ADDRESS;
String tokenSymbol = BlockchainMetadata.Solana.SPL_TOKEN_SYMBOL;
int tokenDecimals = BlockchainMetadata.Solana.SPL_TOKEN_DECIMALS;

// 관리자 지갑 정보
String adminAddress = BlockchainMetadata.Solana.ADMIN_WALLET_ADDRESS;
String adminPrivateKey = BlockchainMetadata.Solana.ADMIN_PRIVATE_KEY;

// 수수료 설정
BigDecimal transactionFee = BlockchainMetadata.Solana.TRANSACTION_FEE;
BigDecimal exchangeFeePercentage = BlockchainMetadata.Solana.EXCHANGE_FEE_PERCENTAGE;

// 네트워크 설정
String networkName = BlockchainMetadata.Solana.NETWORK_NAME;
String rpcEndpoint = BlockchainMetadata.Solana.RPC_ENDPOINT;
String explorerUrl = BlockchainMetadata.Solana.EXPLORER_URL;

// 프로그램 ID
String governanceProgramId = BlockchainMetadata.Solana.GOVERNANCE_PROGRAM_ID;
String stakingProgramId = BlockchainMetadata.Solana.STAKING_PROGRAM_ID;
String tokenProgramId = BlockchainMetadata.Solana.TOKEN_PROGRAM_ID;
```

### 3. 공통 설정

```java
// 타임아웃 설정
int requestTimeout = BlockchainMetadata.Common.REQUEST_TIMEOUT_SECONDS;
int connectionTimeout = BlockchainMetadata.Common.CONNECTION_TIMEOUT_SECONDS;

// 재시도 설정
int maxRetryAttempts = BlockchainMetadata.Common.MAX_RETRY_ATTEMPTS;
long retryDelayMs = BlockchainMetadata.Common.RETRY_DELAY_MS;

// 로깅 설정
boolean enableDetailedLogging = BlockchainMetadata.Common.ENABLE_DETAILED_LOGGING;
boolean enableTransactionLogging = BlockchainMetadata.Common.ENABLE_TRANSACTION_LOGGING;

// 보안 설정
int minPasswordLength = BlockchainMetadata.Common.MIN_PASSWORD_LENGTH;
boolean requireSpecialCharacters = BlockchainMetadata.Common.REQUIRE_SPECIAL_CHARACTERS;
boolean requireNumbers = BlockchainMetadata.Common.REQUIRE_NUMBERS;
boolean requireUppercase = BlockchainMetadata.Common.REQUIRE_UPPERCASE;
```

### 4. 환경 설정

```java
// 환경 상수
String dev = BlockchainMetadata.Environment.DEVELOPMENT;
String staging = BlockchainMetadata.Environment.STAGING;
String prod = BlockchainMetadata.Environment.PRODUCTION;

// 현재 환경 확인
boolean isDev = BlockchainMetadata.Environment.isDevelopment();
boolean isStaging = BlockchainMetadata.Environment.isStaging();
boolean isProd = BlockchainMetadata.Environment.isProduction();

// 환경 설정 (application.yml에서 설정 가능)
BlockchainMetadata.Environment.CURRENT_ENV = "prod";
```

### 5. 유틸리티 메서드

```java
// 이더리움 가스 비용 계산
BigInteger gasUsed = BigInteger.valueOf(21000L);
BigDecimal gasCost = BlockchainMetadata.Utils.calculateEthereumGasCost(gasUsed);

// 솔라나 수수료 계산
BigDecimal solanaFee = BlockchainMetadata.Utils.calculateSolanaFee();

// 교환 수수료 계산
BigDecimal amount = BigDecimal.valueOf(100.0);
BigDecimal ethereumFee = BlockchainMetadata.Utils.calculateExchangeFee(amount, "ETHEREUM");
BigDecimal solanaFee = BlockchainMetadata.Utils.calculateExchangeFee(amount, "SOLANA");

// 거래 금액 유효성 검사
boolean isValid = BlockchainMetadata.Utils.isValidTransactionAmount(amount, "ETHEREUM");
```

## 🔧 실제 사용 예시

### 1. 이더리움 지갑 서비스에서 사용

```java
@Service
public class EthereumWallet implements WalletService {
    
    private BigInteger getGasPrice() {
        // 메타데이터에서 가스 가격 가져오기
        return BlockchainMetadata.Ethereum.GAS_PRICE;
    }
    
    private BigInteger getGasLimit(TransactionBody<T> transactionBody) {
        switch (transactionBody.getType()) {
            case TOKEN_TRANSFER:
                // ERC-20 토큰 전송
                return BlockchainMetadata.Ethereum.GAS_LIMIT.multiply(BigInteger.valueOf(15L));
            default:
                // 기본 ETH 전송
                return BlockchainMetadata.Ethereum.GAS_LIMIT;
        }
    }
    
    private String getContractAddress() {
        return BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS;
    }
}
```

### 2. 교환 서비스에서 사용

```java
@Service
public class ExchangeApplicationService {
    
    private String getTokenContractAddress(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                return BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS;
            case SOLANA:
                return BlockchainMetadata.Solana.SPL_TOKEN_MINT_ADDRESS;
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }
    
    private BigDecimal calculateExchangeFee(BigDecimal amount, NetworkType networkType) {
        String blockchain = networkType.name();
        return BlockchainMetadata.Utils.calculateExchangeFee(amount, blockchain);
    }
}
```

### 3. 토큰 계정 서비스에서 사용

```java
@Service
public class TokenAccountApplicationService {
    
    public TokenAccountDto findOrCreateDefaultTokenAccount(String userId, String walletAddress, NetworkType network) {
        String contractAddress;
        String symbol;
        
        switch (network) {
            case ETHEREUM:
                contractAddress = BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS;
                symbol = BlockchainMetadata.Ethereum.ERC20_SYMBOL;
                break;
            case SOLANA:
                contractAddress = BlockchainMetadata.Solana.SPL_TOKEN_MINT_ADDRESS;
                symbol = BlockchainMetadata.Solana.SPL_TOKEN_SYMBOL;
                break;
            default:
                throw new IllegalArgumentException("Unsupported network type: " + network);
        }
        
        return findOrCreateTokenAccount(userId, walletAddress, network, contractAddress, symbol);
    }
}
```

## 📊 메타데이터 값들

### 이더리움 설정
- **ERC20 컨트랙트 주소**: `0x1234567890123456789012345678901234567890`
- **토큰 심볼**: `ETH`
- **소수점 자릿수**: `18`
- **가스 한도**: `21,000` (기본)
- **가스 가격**: `20 Gwei`
- **트랜잭션 수수료**: `0.001 ETH`
- **교환 수수료**: `0.5%`
- **네트워크**: `Sepolia`
- **체인 ID**: `11155111`

### 솔라나 설정
- **SPL 토큰 민트 주소**: `EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v`
- **토큰 심볼**: `SOL`
- **소수점 자릿수**: `9`
- **트랜잭션 수수료**: `0.000005 SOL`
- **교환 수수료**: `0.3%`
- **네트워크**: `Devnet`
- **RPC 엔드포인트**: `https://api.devnet.solana.com`

## 🔒 보안 고려사항

1. **프라이빗 키**: 실제 운영 환경에서는 환경 변수나 보안 저장소를 사용해야 합니다.
2. **컨트랙트 주소**: 실제 배포된 컨트랙트 주소로 업데이트해야 합니다.
3. **네트워크 설정**: 프로덕션 환경에 맞는 네트워크 설정을 사용해야 합니다.

## 🚀 확장 방법

새로운 블록체인을 추가하려면:

1. `BlockchainMetadata` 클래스에 새로운 내부 클래스 추가
2. 필요한 상수들 정의
3. `Utils` 클래스에 관련 유틸리티 메서드 추가
4. 기존 서비스에서 새로운 메타데이터 사용

```java
public static class Polygon {
    // Polygon 메타데이터 정의
    public static final String ERC20_CONTRACT_ADDRESS = "...";
    public static final String NETWORK_NAME = "Mumbai";
    // ... 기타 설정
}
```

## 📝 주의사항

1. **Static 접근**: 모든 메타데이터는 static으로 접근 가능하므로 인스턴스 생성이 필요 없습니다.
2. **타입 안전성**: 적절한 타입을 사용하여 컴파일 타임에 오류를 방지합니다.
3. **문서화**: 새로운 메타데이터를 추가할 때 문서를 업데이트해야 합니다.
4. **테스트**: 메타데이터 변경 시 관련 테스트를 실행해야 합니다. 