package com.bloominggrace.governance.wallet.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.security.domain.service.EncryptionService;
import com.bloominggrace.governance.user.application.service.UserService;
import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.application.dto.CreateWalletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WalletApplicationService {

    private final WalletRepository walletRepository;
    private final WalletServiceFactory walletServiceFactory;
    private final UserService userService;
    private final EncryptionService encryptionService;

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

    /**
     * 지갑을 잠금 해제하고 프라이빗 키의 유효성을 검증합니다.
     * 
     * @param walletAddress 검증할 지갑 주소
     * @param networkType 네트워크 타입
     * @return UnlockResult 검증 결과
     */
    public WalletService.UnlockResult unlockWallet(String walletAddress, NetworkType networkType) {
        log.info("Attempting to unlock wallet: {} on network: {}", walletAddress, networkType);
        
        try {
            WalletService walletService = walletServiceFactory.getWalletService(networkType);
            return walletService.unlockWallet(walletAddress);
        } catch (Exception e) {
            log.error("Failed to unlock wallet: {} on network: {}", walletAddress, networkType, e);
            return WalletService.UnlockResult.failure(walletAddress, "Failed to unlock wallet: " + e.getMessage());
        }
    }
} 