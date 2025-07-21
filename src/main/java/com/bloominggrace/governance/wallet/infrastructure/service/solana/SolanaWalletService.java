package com.bloominggrace.governance.wallet.infrastructure.service.solana;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.service.EncryptionService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.domain.service.KeyPairProvider;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.SignedTransaction;
import com.bloominggrace.governance.shared.domain.model.SolanaTransactionData;
import com.bloominggrace.governance.shared.util.HashUtils;
import com.bloominggrace.governance.shared.util.HexUtils;
import com.bloominggrace.governance.shared.util.SignatureUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.math.BigInteger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.user.infrastructure.repository.UserRepository;
import com.bloominggrace.governance.shared.util.Base58Utils;
import org.springframework.context.ApplicationContext;

@Service("solanaWalletService")
public class SolanaWalletService extends WalletService {
    
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;
    
    public SolanaWalletService(
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
        KeyPairProvider.KeyPairResult keyPair = KeyPairProvider.generateKeyPair(NetworkType.SOLANA);
        String encryptedPrivateKey = encryptionService.encrypt(keyPair.getPrivateKey());
        User user = userRepository.findById(userId.getValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Wallet wallet = new Wallet(user, keyPair.getAddress(), NetworkType.SOLANA, encryptedPrivateKey);
        return walletRepository.save(wallet);
    }
    

    
    @Override
    public Optional<Wallet> findByAddress(String walletAddress) {
        return walletRepository.findAll().stream()
                .filter(wallet -> wallet.getWalletAddress().equals(walletAddress) && 
                                wallet.getNetworkType() == NetworkType.SOLANA)
                .findFirst();
    }
    
    @Override
    public List<Wallet> findByUserId(UserId userId) {
        return walletRepository.findByUserId(userId.getValue()).stream()
                .filter(wallet -> wallet.getNetworkType() == NetworkType.SOLANA)
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
                                wallet.getNetworkType() == NetworkType.SOLANA)
                .findFirst()
                .map(wallet -> {
                    wallet.setActive(active);
                    return walletRepository.save(wallet);
                })
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }
    


        @Override
    public <T> byte[] sign(TransactionBody<T> transactionBody, String privateKey) {
        try {
            // 1. Solana 특화 필드들을 동적으로 설정
            SolanaTransactionData solanaData = createSolanaTransactionData(transactionBody);
            
            // 2. Solana 트랜잭션 메시지 생성 (서명 전)
            byte[] messageBytes = createSolanaMessage(solanaData);
            
            // 3. Ed25519 서명 생성
            Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(
                hexStringToByteArray(privateKey), 0
            );
            Ed25519Signer signer = new Ed25519Signer();
            signer.init(true, privateKeyParams);
            signer.update(messageBytes, 0, messageBytes.length);
            byte[] signature = signer.generateSignature();
            
            // 4. signedRawTransaction 생성 (서명 + 메시지)
            return createSignedRawTransaction(signature, messageBytes);
        } catch (Exception e) {
            throw new RuntimeException("Solana sign error", e);
        }
    }

    /**
     * Solana 트랜잭션 데이터 생성 (네트워크별 특화 필드 포함)
     */
    private <T> SolanaTransactionData createSolanaTransactionData(TransactionBody<T> transactionBody) {
        // 기본값을 사용하여 트랜잭션 데이터 생성
        String recentBlockhash = getRecentBlockhash();
        
        // 기본 수수료 (5000 lamports)
        long fee = 5000L;
        
        return new SolanaTransactionData(
            recentBlockhash,
            fee,
            null // programId는 선택적
        );
    }
    
    /**
     * 최근 블록해시 가져오기 (기본값 사용)
     */
    private String getRecentBlockhash() {
        // 기본값 사용
        return "11111111111111111111111111111111";
    }
    
    /**
     * 트랜잭션 타입에 따른 Solana 지시사항 생성
     */
    private <T> String getProgramId(TransactionBody<T> transactionBody) {
        switch (transactionBody.getType()) {
            case PROPOSAL_CREATE:
            case PROPOSAL_VOTE:
                return "GovernanceProgram111111111111111111111111111"; // 가상의 거버넌스 프로그램 ID
            default:
                return "SystemProgram111111111111111111111111111111"; // Solana 시스템 프로그램
        }
    }
    
    /**
     * Solana 트랜잭션 메시지 생성 (서명용)
     */
    private byte[] createSolanaMessage(SolanaTransactionData solanaData) {
        // 실제 구현에서는 Solana 메시지 구조에 맞게 생성해야 함
        // 여기서는 간단한 바이트 배열 조합으로 대체
        String messageData = String.format("%s:%d:%s",
            solanaData.getRecentBlockhash(),
            solanaData.getFee(),
            solanaData.getProgramId() != null ? solanaData.getProgramId() : ""
        );
        return messageData.getBytes();
    }
    
    /**
     * Solana signedRawTransaction 생성
     */
    private byte[] createSignedRawTransaction(byte[] signature, byte[] messageBytes) {
        // 실제 구현에서는 Solana 트랜잭션 구조에 맞게 생성해야 함
        // 여기서는 간단한 바이트 배열 조합으로 대체
        String signedData = "solana_signed:" + bytesToHex(signature) + ":" + bytesToHex(messageBytes);
        return signedData.getBytes();
    }
    

    
    /**
     * 바이트 배열을 16진수 문자열로 변환
     */
    private String bytesToHex(byte[] bytes) {
        return HexUtils.bytesToHex(bytes);
    }
    
    /**
     * 16진수 문자열을 바이트 배열로 변환
     */
    private byte[] hexStringToByteArray(String s) {
        return HexUtils.hexToBytes(s);
    }

    /**
     * Solana 주소 유효성 검증
     */
    public boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }
        
        // Solana 주소는 32-44자 길이여야 함
        if (address.length() < 32 || address.length() > 44) {
            return false;
        }
        
        // Base58 형식 검증
        return Base58Utils.isValid(address);
    }
} 