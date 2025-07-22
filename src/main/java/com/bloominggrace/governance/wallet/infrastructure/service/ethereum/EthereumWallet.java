package com.bloominggrace.governance.wallet.infrastructure.service.ethereum;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.service.EncryptionService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.domain.service.KeyPairProvider;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.EthereumTransactionData;
import com.bloominggrace.governance.shared.util.HashUtils;
import com.bloominggrace.governance.shared.util.HexUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.user.infrastructure.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Sign;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.TypeReference;
import org.web3j.utils.Numeric;

import java.util.Arrays;
import java.util.Collections;

import java.util.List;
import java.util.Optional;
import java.math.BigInteger;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.bloominggrace.governance.shared.infrastructure.service.AdminWalletService;
import com.bloominggrace.governance.shared.domain.model.BlockchainMetadata;
import org.springframework.context.ApplicationContext;

@Slf4j
@Service("ethereumWalletService")
public class EthereumWallet extends WalletService {
    
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;
    
    public EthereumWallet(
            ApplicationContext applicationContext,
            WalletRepository walletRepository,
            EncryptionService encryptionService,
            UserRepository userRepository) {
        super(applicationContext);
        this.walletRepository = walletRepository;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
    }

    @Override
    public Wallet createWallet(UserId userId, NetworkType networkType) {
        // 사용자당 이더리움 지갑 중복 검증
        List<Wallet> existingWallets = findByUserId(userId);
        if (!existingWallets.isEmpty()) {
            throw new RuntimeException("User already has an Ethereum wallet: " + existingWallets.get(0).getWalletAddress());
        }
        
        // 지갑 주소 중복 검증
        KeyPairProvider.KeyPairResult keyPair = KeyPairProvider.generateKeyPair(NetworkType.ETHEREUM);
        
        // 생성된 주소가 이미 존재하는지 확인 (매우 낮은 확률이지만 안전을 위해)
        Optional<Wallet> existingWallet = findByAddress(keyPair.getAddress());
        if (existingWallet.isPresent()) {
            throw new RuntimeException("Generated wallet address already exists: " + keyPair.getAddress());
        }
        
        String encryptedPrivateKey = encryptionService.encrypt(keyPair.getPrivateKey());
        User user = userRepository.findById(userId.getValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Wallet wallet = new Wallet(user, keyPair.getAddress(), NetworkType.ETHEREUM, encryptedPrivateKey);
        return walletRepository.save(wallet);
    }

    
    @Override
    public Optional<Wallet> findByAddress(String walletAddress) {
        return walletRepository.findAll().stream()
                .filter(wallet -> wallet.getWalletAddress().equals(walletAddress) && 
                                wallet.getNetworkType() == NetworkType.ETHEREUM)
                .findFirst();
    }
    
    @Override
    public List<Wallet> findByUserId(UserId userId) {
        return walletRepository.findByUserId(userId.getValue()).stream()
                .filter(wallet -> wallet.getNetworkType() == NetworkType.ETHEREUM)
                .toList();
    }
    
    @Override
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }
    

    
    @Override
    public Wallet updateActiveStatus(String walletAddress, boolean active) {
        return walletRepository.findAll().stream()
                .filter(wallet -> wallet.getWalletAddress().equals(walletAddress) && 
                                wallet.getNetworkType() == NetworkType.ETHEREUM)
                .findFirst()
                .map(wallet -> {
                    wallet.setActive(active);
                    return walletRepository.save(wallet);
                })
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }
    /**
     * 주어진 메시지에 대해 개인키로 서명합니다.
     */
    @Override
    public <T> byte[] sign(TransactionBody<T> transactionBody, String privateKey) {
        try {
            // 1. RawTransaction JSON에서 nonce를 직접 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rawTransactionJson = objectMapper.readTree(transactionBody.getData());

            // JSON에서 nonce 추출 (문자열로 저장되어 있으므로 BigInteger로 변환)
            BigInteger nonce;
            JsonNode nonceNode = rawTransactionJson.get("nonce");
            if (nonceNode.isTextual()) {
                nonce = new BigInteger(nonceNode.asText());
            } else if (nonceNode.isNumber()) {
                nonce = BigInteger.valueOf(nonceNode.asLong());
            } else {
                throw new IllegalArgumentException("Invalid nonce format in transaction data");
            }

            log.info("Parsed nonce from RawTransaction JSON: {}", nonce);

            // 2. JSON에서 필요한 필드들 추출
            String toAddress = rawTransactionJson.get("toAddress").asText();
            String value = rawTransactionJson.get("value").asText();
            String data = rawTransactionJson.has("data") ? rawTransactionJson.get("data").asText() : "0x";
            String gas_limit = rawTransactionJson.get("gasLimit").asText();
            String gas_price = rawTransactionJson.get("gasPrice").asText();


            // 3. Web3j RawTransaction 생성
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                new BigInteger(gas_price),
                new BigInteger(gas_limit),
                toAddress,
                new BigInteger(value),
                data
            );

            // 4. Credentials 생성
            Credentials credentials = Credentials.create(privateKey);

            // 5. 트랜잭션 서명 (EIP-1559 형식)
            long chainId = getChainId(); // 메서드로 분리
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);


            log.info("Transaction signed successfully with nonce: {}", nonce);
            return signedMessage;
        } catch (Exception e) {
            throw new RuntimeException("Ethereum sign error", e);
        }
    }

    @Override
    public boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }

    @Override
    public UnlockResult unlockWallet(String walletAddress) {
        try {
            log.info("Attempting to unlock Ethereum wallet: {}", walletAddress);
            
            // 1. 지갑 주소 유효성 검증
            if (!isValidAddress(walletAddress)) {
                return UnlockResult.failure(walletAddress, "Invalid Ethereum address format");
            }
            
            // 2. 데이터베이스에서 지갑 조회
            Optional<Wallet> walletOpt = findByAddress(walletAddress);
            if (walletOpt.isEmpty()) {
                return UnlockResult.failure(walletAddress, "Wallet not found in database");
            }
            
            Wallet wallet = walletOpt.get();
            log.info("Found wallet in database for address: {}", walletAddress);
            
            // 3. 암호화된 프라이빗 키 복호화
            String encryptedPrivateKey = wallet.getEncryptedPrivateKey();
            String decryptedPrivateKey;
            
            try {
                decryptedPrivateKey = encryptionService.decrypt(encryptedPrivateKey);
                log.info("Successfully decrypted private key for wallet: {}", walletAddress);
            } catch (Exception e) {
                log.error("Failed to decrypt private key for wallet: {}", walletAddress, e);
                return UnlockResult.failure(walletAddress, "Failed to decrypt private key: " + e.getMessage());
            }
            
            // 4. 프라이빗 키 유효성 검증
            if (decryptedPrivateKey == null || decryptedPrivateKey.trim().isEmpty()) {
                return UnlockResult.failure(walletAddress, "Decrypted private key is null or empty");
            }
            
            // 0x 접두사 제거
            String cleanPrivateKey = decryptedPrivateKey.startsWith("0x") ? 
                decryptedPrivateKey.substring(2) : decryptedPrivateKey;
            
            // 64자리 16진수 검증
            if (!cleanPrivateKey.matches("[0-9a-fA-F]{64}")) {
                return UnlockResult.failure(walletAddress, "Invalid private key format");
            }
            
            // 5. 프라이빗 키로부터 주소 재생성
            String derivedAddress;
            try {
                Credentials credentials = Credentials.create(cleanPrivateKey);
                derivedAddress = credentials.getAddress();
                log.info("Successfully derived address from private key: {}", derivedAddress);
            } catch (Exception e) {
                log.error("Failed to derive address from private key for wallet: {}", walletAddress, e);
                return UnlockResult.failure(walletAddress, "Failed to derive address from private key: " + e.getMessage());
            }
            
            // 6. 주소 일치 여부 확인
            boolean addressMatch = walletAddress.equalsIgnoreCase(derivedAddress);
            
            if (addressMatch) {
                log.info("✅ Wallet unlock successful - Address match confirmed for: {}", walletAddress);
                return UnlockResult.success(walletAddress, derivedAddress, true);
            } else {
                log.warn("⚠️ Wallet unlock failed - Address mismatch for: {}", walletAddress);
                log.warn("Expected: {}, Derived: {}", walletAddress, derivedAddress);
                return UnlockResult.success(walletAddress, derivedAddress, false);
            }
            
        } catch (Exception e) {
            log.error("Unexpected error during wallet unlock for: {}", walletAddress, e);
            return UnlockResult.failure(walletAddress, "Unexpected error: " + e.getMessage());
        }
    }

    private long getChainId() {
        // 환경변수나 설정파일에서 읽어오도록 개선 가능
        return BlockchainMetadata.Ethereum.CHAIN_ID; // 기본값 사용
    }
    /**
     * 가스 가격 가져오기 (0.0000001 ETH로 조정 - 5배 증가)
     */
    private BigInteger getGasPrice() {
        // BlockchainMetadata에서 가스 가격 가져오기
        return BlockchainMetadata.Ethereum.GAS_PRICE;
    }

} 