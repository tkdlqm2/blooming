package com.bloominggrace.governance.shared.infrastructure.service;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AdminWalletService {

    // Static 캐시로 변경
    private static final Map<NetworkType, AdminWalletInfo> ADMIN_WALLETS = new ConcurrentHashMap<>();

    // Static 필드로 변경
    private static WalletRepository walletRepository;

    @Autowired
    public AdminWalletService(WalletRepository walletRepository) {
        // Static 필드 초기화
        AdminWalletService.walletRepository = walletRepository;
    }

    /**
     * Admin 지갑 정보를 가져옵니다. 메모리에 없으면 DB에서 로딩합니다.
     */
    public static AdminWalletInfo getAdminWallet(NetworkType networkType) {
        AdminWalletInfo adminWallet = ADMIN_WALLETS.get(networkType);
        
        if (adminWallet == null) {
            log.info("Admin wallet not found in memory for network: {}. Loading from database...", networkType);
            adminWallet = loadAdminWalletFromDatabase(networkType);
            if (adminWallet != null) {
                ADMIN_WALLETS.put(networkType, adminWallet);
                log.info("Admin wallet loaded and cached for network: {}", networkType);
            }
        }
        
        return adminWallet;
    }
    
    /**
     * DB에서 admin 지갑 정보를 로딩합니다.
     */
    private static AdminWalletInfo loadAdminWalletFromDatabase(NetworkType networkType) {
        try {
            // Admin 사용자 ID (data.sql에서 정의된 admin 사용자)
            String adminUserId = "f2aec616-1dcb-4e56-923d-16e07a58ae3c";
            UUID adminUserIdUUID = UUID.fromString(adminUserId);

            // DB에서 Admin 사용자의 해당 네트워크 지갑을 찾기
            var adminWalletOpt = walletRepository.findByUser_IdAndNetworkType(adminUserIdUUID, networkType);

            if (adminWalletOpt.isPresent()) {
                Wallet adminWallet = adminWalletOpt.get();
                log.info("Admin wallet found in database for network {}: {}", networkType, adminWallet.getWalletAddress());
                return new AdminWalletInfo(
                    adminWallet.getWalletAddress(),
                    adminWallet.getEncryptedPrivateKey()
                );
            } else {
                // Admin 지갑이 없으면 하드코딩된 Admin 지갑 사용
                log.warn("Admin wallet not found in database for network: {}. Using hardcoded admin wallet.", networkType);
                return getHardcodedAdminWallet(networkType);
            }
        } catch (Exception e) {
            log.error("Failed to load admin wallet from database for network: {}", networkType, e);
            // DB 로딩 실패 시 하드코딩된 Admin 지갑 사용
            return getHardcodedAdminWallet(networkType);
        }
    }

    /**
     * 하드코딩된 Admin 지갑 정보를 반환합니다.
     */
    private static AdminWalletInfo getHardcodedAdminWallet(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                // Admin 지갑 주소: 0x55D5c49e36f8A89111687C9DC8355121068f0cD8
                return new AdminWalletInfo(
                    "0x55D5c49e36f8A89111687C9DC8355121068f0cD8",
                    "jgTiIrbjpucJ2NUHooYdbdNj7UPGgh6wXEd1CyyPjilgjUyIPfLDc14Uc4JtKcbERMapTSfGGwIZLSDKo6MYAq3484rzkrmLSYUAIUm6hX4="
                );
            case SOLANA:
                // Solana Admin 지갑 (임시)
                return new AdminWalletInfo(
                    "1111111111111111111GKcUfEEzaVazsaWLw3LNAzcnR76Dw6b6xGgSvvX7uVdJ",
                    "e5VDLJh2hMbScH1aH3PrPXuVQyyYdoTG0nB9Wh9z6z0n+LmM7dqMul+Wrz+OXvxQC91GI065xCqyrJZFUFe4eKMDlW0wSjm4wUxEZFpRtNc/nivR495rn0y6a/5GJa/+nzysv9kqqO8QAXA4o5lOBI9D4GolW5GbLe4vnJ1klg0="
                );
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }

    /**
     * Admin 지갑 정보를 메모리에 설정합니다.
     */
    public static void setAdminWallet(NetworkType networkType, String walletAddress, String encryptedPrivateKey) {
        AdminWalletInfo adminWallet = new AdminWalletInfo(walletAddress, encryptedPrivateKey);
        ADMIN_WALLETS.put(networkType, adminWallet);
        log.info("Admin wallet set in memory for network: {}", networkType);
    }

    /**
     * Admin 지갑 정보를 메모리에서 제거합니다.
     */
    public static void clearAdminWallet(NetworkType networkType) {
        ADMIN_WALLETS.remove(networkType);
        log.info("Admin wallet cleared from memory for network: {}", networkType);
    }
    
    /**
     * Admin 지갑 정보를 담는 내부 클래스
     */
    public static class AdminWalletInfo {
        private final String walletAddress;
        private final String encryptedPrivateKey;
        
        public AdminWalletInfo(String walletAddress, String encryptedPrivateKey) {
            this.walletAddress = walletAddress;
            this.encryptedPrivateKey = encryptedPrivateKey;
        }
        
        public String getWalletAddress() {
            return walletAddress;
        }
        
        public String getEncryptedPrivateKey() {
            return encryptedPrivateKey;
        }
    }
} 