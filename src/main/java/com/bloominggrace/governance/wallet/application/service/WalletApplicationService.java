package com.bloominggrace.governance.wallet.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.TransactionRequest;
import com.bloominggrace.governance.shared.domain.model.TransactionBodyFactoryProvider;
import com.bloominggrace.governance.shared.domain.model.TransactionBodyFactory;
import com.bloominggrace.governance.shared.domain.service.EncryptionService;
import com.bloominggrace.governance.user.application.service.UserService;
import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.application.dto.CreateWalletRequest;
import com.bloominggrace.governance.shared.infrastructure.service.AdminWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.bloominggrace.governance.wallet.domain.service.KeyPairProvider.bytesToHex;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WalletApplicationService {

    private final WalletRepository walletRepository;
    private final WalletServiceFactory walletServiceFactory;
    private final BlockchainClientFactory blockchainClientFactory;
    private final UserService userService;
    private final EncryptionService encryptionService;
    private final TransactionBodyFactoryProvider transactionBodyFactoryProvider;
    // AdminWalletService는 static 메서드로 변경되어 의존성 제거

    // ===== 트랜잭션 처리 단계별 메서드 =====

    /**
     * 새로운 팩토리 패턴을 사용한 트랜잭션 본문 생성
     */
    public <T> TransactionBody<T> createTransactionBody(TransactionRequest request) {
        TransactionBodyFactory factory = transactionBodyFactoryProvider.getFactory(request.getNetworkType());
        return factory.createTransactionBody(request);
    }

    // ===== 통합된 TransactionBody 생성 메서드 =====

    /**
     * 통합된 TransactionBody 생성 메서드 (Object 기반 추상화)
     * 공통 필드: fromAddress, toAddress, amount, tokenAddress, networkType
     * 트랜잭션별 고유 데이터: transactionData (Object)
     * 이더리움의 경우 nonce 자동 관리
    **/
    public <T> TransactionBody<T> createTransactionBody(
            String fromAddress,
            String toAddress,
            BigDecimal amount,
            String tokenAddress,
            Object transactionData,
            TransactionRequest.TransactionType transactionType,
            NetworkType networkType) {
        
        log.info("createTransactionBody called with fromAddress: {}, networkType: {}", fromAddress, networkType);
        
        // 이더리움의 경우 nonce 자동 관리
        if (networkType == NetworkType.ETHEREUM) {
            // Admin 지갑 주소 직접 확인
            String adminWalletAddress = "0x55D5c49e36f8A89111687C9DC8355121068f0cD8";
            
            log.info("Checking if {} matches admin wallet address: {}", fromAddress, adminWalletAddress);
            log.info("Address comparison: {} equalsIgnoreCase {} = {}", fromAddress, adminWalletAddress, fromAddress.equalsIgnoreCase(adminWalletAddress));
            
            // 강제로 Admin 지갑 nonce 관리 활성화 (테스트용)
            if (fromAddress.equalsIgnoreCase(adminWalletAddress) || true) {
                log.info("Using Admin wallet nonce management for address: {}", fromAddress);
                log.info("About to call createTransactionBodyWithNonce");
                // Admin 지갑인 경우 nonce 자동 관리
                TransactionBody<T> result = createTransactionBodyWithNonce(fromAddress, toAddress, amount, tokenAddress, 
                    transactionData, transactionType, networkType);
                log.info("createTransactionBodyWithNonce returned with nonce: {}", result.getNonce());
                return result;
            } else {
                log.info("Not using Admin wallet nonce management - address mismatch: {} != {}", fromAddress, adminWalletAddress);
            }
        }
        
        log.info("Creating TransactionRequest without nonce management");
        TransactionRequest request = TransactionRequest.builder()
            .fromAddress(fromAddress)
            .toAddress(toAddress)
            .amount(amount)
            .tokenAddress(tokenAddress)
            .networkType(networkType.name())
            .type(transactionType)
            .transactionData(transactionData)
            .build();
        
        return createTransactionBody(request);
    }
    
    /**
     * Nonce가 포함된 TransactionBody 생성 메서드 (이더리움 전용)
     */
    private <T> TransactionBody<T> createTransactionBodyWithNonce(
            String fromAddress,
            String toAddress,
            BigDecimal amount,
            String tokenAddress,
            Object transactionData,
            TransactionRequest.TransactionType transactionType,
            NetworkType networkType) {
        
        log.info("Creating transaction body with nonce for admin wallet: {}", fromAddress);
        
        // Admin 지갑의 nonce 강제로 블록체인에서 다시 로딩
        log.info("Calling forceReloadAdminWalletNonce for network: {}", networkType);
        var nonce = AdminWalletService.forceReloadAdminWalletNonce(networkType);
        log.info("Force reloaded nonce for admin wallet: {} = {}", fromAddress, nonce);
        
        TransactionRequest request = TransactionRequest.builder()
            .fromAddress(fromAddress)
            .toAddress(toAddress)
            .amount(amount)
            .tokenAddress(tokenAddress)
            .networkType(networkType.name())
            .type(transactionType)
            .transactionData(transactionData)
            .nonce(nonce) // nonce 추가
            .build();
        
        log.info("Created TransactionRequest with nonce: {}", nonce);
        return createTransactionBody(request);
    }

    /**
     * 2단계: TransactionBody 서명하여 signedRawTransaction 반환
     */
    public <T> byte[] signTransactionBody(TransactionBody<T> transactionBody, String walletAddress) {
        // 지갑 주소로 암호화된 프라이빗 키를 가져와 복호화
        String decryptedPrivateKey = getDecryptedPrivateKeyByAddress(walletAddress);
        
        // 네트워크별 지갑 서비스를 가져와서 트랜잭션을 서명하고 인코딩합니다
        NetworkType networkType = NetworkType.valueOf(transactionBody.getNetworkType());
        WalletService walletService = walletServiceFactory.getWalletService(networkType);
        
        // 추상화된 인터페이스를 통해 각 네트워크별 구현체에서 구체적인 서명 및 인코딩 로직 처리
        byte[] signedTransaction = walletService.sign(transactionBody, decryptedPrivateKey);
        
        // 이더리움의 경우 Admin 지갑인지 확인하고 nonce 증가
        if (networkType == NetworkType.ETHEREUM) {
            try {
                var adminWallet = AdminWalletService.getAdminWallet(networkType);
                if (adminWallet != null && adminWallet.getWalletAddress().equalsIgnoreCase(walletAddress)) {
                    // Admin 지갑인 경우 nonce 증가
                    AdminWalletService.incrementAdminWalletNonce(networkType);
                }
            } catch (Exception e) {
                // Admin 지갑이 아닌 경우 무시
            }
        }
        
        return signedTransaction;
    }

    /**
     * 2단계: TransactionBody 서명하여 signedRawTransaction 반환 (암호화된 private key 직접 사용)
     */
    public <T> byte[] signTransactionBodyWithPrivateKey(TransactionBody<T> transactionBody, String encryptedPrivateKey) {
        try {
            // 암호화된 프라이빗 키를 복호화
            String decryptedPrivateKey = encryptionService.decrypt(encryptedPrivateKey);
            
            // 네트워크별 지갑 서비스를 가져와서 트랜잭션을 서명하고 인코딩합니다
            NetworkType networkType = NetworkType.valueOf(transactionBody.getNetworkType());
            WalletService walletService = walletServiceFactory.getWalletService(networkType);
            
            // 추상화된 인터페이스를 통해 각 네트워크별 구현체에서 구체적인 서명 및 인코딩 로직 처리
            return walletService.sign(transactionBody, decryptedPrivateKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign transaction with private key: " + e.getMessage(), e);
        }
    }

    /**
     * 3단계: signedRawTransaction 브로드캐스트
     */
    public String broadcastSignedTransaction(byte[] signedRawTransaction, String networkType) {
        NetworkType networkTypeEnum = NetworkType.valueOf(networkType);
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkTypeEnum);
        return blockchainClient.broadcastTransaction(bytesToHex(signedRawTransaction));
    }

    // ===== 기존 유틸리티 메서드들 =====

    /**
     * 사용자 ID와 네트워크를 기반으로 암호화된 프라이빗 키를 DB에서 가져와 복호화합니다.
     */
    public String getDecryptedPrivateKey(UserId userId, NetworkType networkType) {
        Optional<Wallet> walletOpt = walletRepository.findByUser_IdAndNetworkType(userId.getValue(), networkType);
        
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("지갑을 찾을 수 없습니다. 사용자 ID: " + userId + ", 네트워크: " + networkType);
        }
        
        Wallet wallet = walletOpt.get();
        String encryptedPrivateKey = wallet.getEncryptedPrivateKey();
        
        try {
            return encryptionService.decrypt(encryptedPrivateKey);
        } catch (Exception e) {
            throw new RuntimeException("프라이빗 키 복호화에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * UUID 사용자 ID와 네트워크를 기반으로 암호화된 프라이빗 키를 DB에서 가져와 복호화합니다.
     */
    public String getDecryptedPrivateKey(UUID userId, NetworkType networkType) {
        return getDecryptedPrivateKey(new UserId(userId), networkType);
    }

    /**
     * 지갑 주소를 기반으로 암호화된 프라이빗 키를 DB에서 가져와 복호화합니다.
     */
    private String getDecryptedPrivateKeyByAddress(String walletAddress) {
        Optional<Wallet> walletOpt = walletRepository.findByWalletAddress(walletAddress);
        
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("지갑을 찾을 수 없습니다. 주소: " + walletAddress);
        }
        
        Wallet wallet = walletOpt.get();
        String encryptedPrivateKey = wallet.getEncryptedPrivateKey();
        
        try {
            String decrypted = encryptionService.decrypt(encryptedPrivateKey);
            if (walletAddress.equalsIgnoreCase("0x55D5c49e36f8A89111687C9DC8355121068f0cD8")) {
                log.info("[ADMIN TEST] 복호화된 Admin Private Key: {}", decrypted);
                System.out.println("[ADMIN TEST] 복호화된 Admin Private Key: " + decrypted);
            }
            return decrypted;
        } catch (Exception e) {
            throw new RuntimeException("프라이빗 키 복호화에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 ID와 네트워크를 기반으로 지갑을 생성합니다.
     */
    public WalletDto createWallet(CreateWalletRequest request) {
        UserId userId = new UserId(UUID.fromString(request.getUserId()));
        NetworkType networkType = NetworkType.valueOf(request.getNetworkType().toUpperCase());
        
        Optional<User> userOpt = userService.findById(userId.getValue());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + request.getUserId());
        }
        
        WalletService walletService = walletServiceFactory.getWalletService(networkType);
        Wallet wallet = walletService.createWallet(userId, networkType);
        
        wallet.setUser(userOpt.get());
        wallet = walletRepository.save(wallet);
        
        return WalletDto.from(wallet);
    }
    // ===== 기존 CRUD 메서드들 =====

    @Transactional(readOnly = true)
    public Optional<Wallet> findById(UUID id) {
        return walletRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Wallet> findAll() {
        return walletRepository.findAll();
    }

    /**
     * 지갑 활성화 상태를 변경합니다.
     */
    public Wallet updateWalletActiveStatus(String walletAddress, boolean active, NetworkType networkType) {
        WalletService walletService = walletServiceFactory.getWalletService(networkType);
        return walletService.updateActiveStatus(walletAddress, active);
    }

    /**
     * 사용자 ID로 지갑 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Wallet> findByUserId(UserId userId) {
        return walletRepository.findByUserId(userId);
    }

    /**
     * 지갑 주소로 지갑을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<Wallet> getWalletByAddress(String walletAddress) {
        return walletRepository.findByWalletAddress(walletAddress);
    }
} 