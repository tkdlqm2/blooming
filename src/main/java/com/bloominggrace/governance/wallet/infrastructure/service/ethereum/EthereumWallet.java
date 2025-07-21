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
        KeyPairProvider.KeyPairResult keyPair = KeyPairProvider.generateKeyPair(NetworkType.ETHEREUM);
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
            BigInteger nonce = new BigInteger(rawTransactionJson.get("nonce").asText());
            log.info("Parsed nonce from RawTransaction JSON: {}", nonce);

            // 2. JSON에서 필요한 필드들 추출
            String toAddress = rawTransactionJson.get("toAddress").asText();
            String value = rawTransactionJson.get("value").asText();
            String data = rawTransactionJson.has("data") ? rawTransactionJson.get("data").asText() : "0x";
            
            // 3. Web3j RawTransaction 생성
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                getGasPrice(),
                BlockchainMetadata.Ethereum.GAS_LIMIT,
                toAddress,
                new BigInteger(value),
                data
            );

            // 4. Credentials 생성
            Credentials credentials = Credentials.create(privateKey);

            // 5. 트랜잭션 서명 (EIP-1559 형식)
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, 11155111L, credentials);

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

    /**
     * 가스 가격 가져오기 (0.0000001 ETH로 조정 - 5배 증가)
     */
    private BigInteger getGasPrice() {
        // BlockchainMetadata에서 가스 가격 가져오기
        return BlockchainMetadata.Ethereum.GAS_PRICE;
    }

} 