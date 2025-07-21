# ERC20 토큰 전송 테스트 스크립트

이 디렉토리에는 새로운 `executeERC20Transfer` 구현을 테스트하기 위한 스크립트들이 포함되어 있습니다.

## 📁 스크립트 목록

### 1. `test_erc20_flow.sh` - 상세 테스트 스크립트
전체 ERC20 토큰 전송 플로우를 상세하게 테스트하는 스크립트입니다.

**기능:**
- 새로운 사용자 생성
- 무료 포인트 지급
- 포인트 잔액 확인
- 이더리움 지갑 생성
- 실제 Exchange 요청 (JWT 토큰 사용)
- Exchange 처리 (새로운 executeERC20Transfer 구현 실행)
- 트랜잭션 해시 확인
- Exchange 요청 목록 확인
- 최종 포인트 잔액 확인

**사용법:**
```bash
./scripts/test_erc20_flow.sh
```

### 2. `simple_erc20_test.sh` - 간단 테스트 스크립트
빠르고 간단한 ERC20 토큰 전송 테스트를 위한 스크립트입니다.

**기능:**
- 새로운 사용자 생성
- 포인트 지급
- 지갑 생성
- 실제 Exchange 요청 (JWT 토큰 사용)
- Exchange 처리 (새로운 executeERC20Transfer 구현 실행)
- 트랜잭션 해시 확인

**사용법:**
```bash
./scripts/simple_erc20_test.sh
```

### 3. `enhanced_erc20_test.sh` - 향상된 테스트 스크립트
새로운 executeERC20Transfer 구현이 실제로 실행되고 txhash를 보여주는 스크립트입니다.

**기능:**
- 새로운 사용자 생성
- 무료 포인트 지급
- 포인트 잔액 확인
- 이더리움 지갑 생성
- 실제 Exchange 요청 (JWT 토큰 사용)
- Exchange 처리 (새로운 executeERC20Transfer 실행)
- Exchange 요청 상세 정보 조회 (txhash 포함)
- 최종 포인트 잔액 확인
- 모든 Exchange 요청 목록 확인

**사용법:**
```bash
./scripts/enhanced_erc20_test.sh
```

### 4. `test_create_raw_transaction.sh` - 새로운 createRawTransaction 메서드 테스트 스크립트
WalletService 인터페이스의 추상화된 createRawTransaction 메서드를 테스트하는 스크립트입니다.

**기능:**
- 새로운 사용자 생성
- 포인트 지급
- 이더리움 지갑 생성
- 새로운 createRawTransaction 메서드 테스트 (ERC20 전송)
- Exchange 처리 (새로운 createRawTransaction 메서드 실행)
- 트랜잭션 해시 확인
- Solana 지갑 생성 및 테스트

**사용법:**
```bash
./scripts/test_create_raw_transaction.sh
```

## 🚀 실행 전 준비사항

1. **Spring Boot 애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

2. **스크립트 실행 권한 부여**
   ```bash
   chmod +x scripts/*.sh
   ```

3. **애플리케이션이 포트 8081에서 실행 중인지 확인**
   ```bash
   curl http://localhost:8081/api/exchange/all
   ```

## 📊 테스트 결과 해석

### ✅ 성공한 부분
- 새로운 `executeERC20Transfer` 구현이 성공적으로 실행됨
- `EthereumWalletService`의 새로운 메서드들이 정상 작동
- 역할 분리된 설계 (RawTransaction 생성 → 서명 → 브로드캐스트)
- EthereumClient와 동일한 로직 구현
- **실제 트랜잭션 해시 생성 및 확인**
- **ERC20 토큰이 성공적으로 블록체인에 브로드캐스트됨**

### 🎉 최종 성과
- 포인트 잔액 문제 해결 (실제 JWT 토큰 사용)
- 새로운 구현이 실제로 실행되어 트랜잭션 해시 생성
- 전체 ERC20 토큰 전송 플로우 완성

## 🔧 새로운 구현의 핵심 기능

### 1. 추상화된 createRawTransaction 메서드
```java
// WalletService 인터페이스
String createRawTransaction(String transactionData);

// EthereumWallet 구현체
@Override
public String createRawTransaction(String transactionData) {
    // JSON 파싱 및 Ethereum RawTransaction 생성
    // JSON 형태로 반환
}

// SolanaWalletService 구현체
@Override
public String createRawTransaction(String transactionData) {
    // JSON 파싱 및 Solana RawTransaction 생성
    // JSON 형태로 반환
}
```

### 2. JSON 입력/출력 구조
```json
// 입력 JSON
{
    "fromAddress": "0x...",
    "toAddress": "0x...",
    "tokenAddress": "0x...",
    "amount": "100.0",
    "nonce": "5"
}

// 출력 JSON (Ethereum)
{
    "nonce": "5",
    "gasPrice": "20000000000",
    "gasLimit": "100000",
    "to": "0x...",
    "value": "0",
    "data": "0xa9059cbb...",
    "networkType": "ETHEREUM",
    "transactionType": "ERC20_TRANSFER"
}
```

### 3. 기존 메서드들
```java
// RawTransaction 생성 (기존)
org.web3j.crypto.RawTransaction rawTransaction = ethereumWalletService.createERC20RawTransaction(
    fromWalletAddress, toWalletAddress, tokenContract, amount, nonce);

// 서명
byte[] signedTx = ethereumWalletService.signERC20Transaction(
    rawTransaction, decryptedPrivateKey);

// 브로드캐스트
String hexValue = org.web3j.utils.Numeric.toHexString(signedTx);
String txHash = blockchainClientFactory.getClient(networkType).broadcastTransaction(hexValue);
```

## 📝 로그 확인

스크립트 실행 시 다음과 같은 로그를 확인할 수 있습니다:

```
[INFO] 🚀 향상된 ERC20 토큰 전송 테스트 시작
[SUCCESS] 회원가입 성공 - 사용자 ID: xxx
[SUCCESS] 포인트 수령 성공 - 받은 양: 200
[SUCCESS] 지갑 생성 성공 - 주소: 0x...
[SUCCESS] Exchange 요청 성공 - 요청 ID: xxx
[SUCCESS] Exchange 처리 성공!
[TRANSACTION] 🎉 트랜잭션 해시: 0x...
[SUCCESS] ERC20 토큰 전송이 성공적으로 브로드캐스트되었습니다!
[SUCCESS] 🎊 향상된 ERC20 토큰 전송 테스트 완료!
```

## 🎯 결론

새로운 `executeERC20Transfer` 구현과 `createRawTransaction` 메서드가 성공적으로 완료되었습니다. 실제로 실행되어 트랜잭션 해시를 생성하고 ERC20 토큰을 블록체인에 브로드캐스트합니다. EthereumClient와 동일한 방식으로 ERC20 토큰 전송을 수행하며, 추상화된 인터페이스로 확장성과 유지보수성이 크게 향상되었습니다.

**🎉 최종 성과:**
- ✅ 새로운 사용자 생성부터 ERC20 토큰 수령까지 전체 플로우 완성
- ✅ 실제 트랜잭션 해시 생성 및 확인
- ✅ 블록체인 브로드캐스트 성공
- ✅ 포인트 잔액 문제 해결
- ✅ 자동화된 테스트 스크립트 제공
- ✅ **추상화된 `createRawTransaction` 메서드 구현**
- ✅ **JSON 기반의 확장 가능한 인터페이스**
- ✅ **다중 네트워크 지원 (Ethereum, Solana)** 