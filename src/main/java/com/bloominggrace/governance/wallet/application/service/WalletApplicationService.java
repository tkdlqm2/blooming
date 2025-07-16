package com.bloominggrace.governance.wallet.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.model.SignedTransaction;
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
import com.bloominggrace.governance.wallet.application.service.WalletServiceFactory;
import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.application.dto.CreateWalletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.bloominggrace.governance.wallet.domain.service.KeyPairProvider.bytesToHex;

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
    **/
    public <T> TransactionBody<T> createTransactionBody(
            String fromAddress,
            String toAddress,
            BigDecimal amount,
            String tokenAddress,
            Object transactionData,
            TransactionRequest.TransactionType transactionType,
            NetworkType networkType) {
        
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
     * 2단계: TransactionBody 서명하여 signedRawTransaction 반환
     */
    public <T> byte[] signTransactionBody(TransactionBody<T> transactionBody, String walletAddress) {
        // 지갑 주소로 암호화된 프라이빗 키를 가져와 복호화
        String decryptedPrivateKey = getDecryptedPrivateKeyByAddress(walletAddress);
        
        // 네트워크별 지갑 서비스를 가져와서 트랜잭션을 서명하고 인코딩합니다
        NetworkType networkType = NetworkType.valueOf(transactionBody.getNetworkType());
        WalletService walletService = walletServiceFactory.getWalletService(networkType);
        
        // 추상화된 인터페이스를 통해 각 네트워크별 구현체에서 구체적인 서명 및 인코딩 로직 처리
        return walletService.signTransactionBody(transactionBody, decryptedPrivateKey);
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
        Optional<Wallet> walletOpt = walletRepository.findByUserIdAndNetworkType(userId, networkType);
        
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
            return encryptionService.decrypt(encryptedPrivateKey);
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
    /**
     * 사용자 ID와 네트워크를 기반으로 메시지를 서명합니다.
     */
    public byte[] signMessageByUser(UserId userId, byte[] message, NetworkType networkType) {
        String decryptedPrivateKey = getDecryptedPrivateKey(userId, networkType);
        WalletService walletService = walletServiceFactory.getWalletService(networkType);
        return walletService.sign(message, decryptedPrivateKey);
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