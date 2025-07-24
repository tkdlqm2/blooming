package com.bloominggrace.governance.shared.infrastructure.service;

import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.EthereumBlockchainClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminWalletService {

    // 의존성 주입
    private final BlockchainClientFactory blockchainClientFactory;
    private final WalletRepository walletRepository;

    // 간단한 캐시
    private final Map<NetworkType, BigInteger> proposalCountCache = new ConcurrentHashMap<>();
    private final Map<NetworkType, AdminWalletInfo> adminWalletCache = new ConcurrentHashMap<>();

    // 상수
    private static final String ADMIN_USER_ID = "f2aec616-1dcb-4e56-923d-16e07a58ae3c";

    // ========== ProposalCount 관련 ==========

    /**
     * ProposalCount 조회 (캐시 우선)
     */
    public BigInteger getProposalCount(NetworkType networkType) {
        try {
            // 캐시에 있으면 반환
            BigInteger cached = proposalCountCache.get(networkType);
            if (cached != null) {
                log.debug("📋 ProposalCount from cache: {}", cached);
                cached.add(BigInteger.ONE);
                return cached;
            }

            // 캐시에 없으면 블록체인에서 조회
            log.info("🔄 Loading ProposalCount from blockchain for {}", networkType);
            BigInteger count = blockchainClientFactory.getClient(networkType).getProposalCount().add(BigInteger.ONE);

            // 캐시에 저장
            proposalCountCache.put(networkType, count);
            log.info("✅ ProposalCount cached: {}", count);

            return count;

        } catch (Exception e) {
            log.error("❌ Failed to get ProposalCount: {}", e.getMessage());
            throw new RuntimeException("Failed to get ProposalCount", e);
        }
    }

    /**
     * 다음 ProposalId (proposalCount + 1)
     */
    public BigInteger getNextProposalId(NetworkType networkType) {
        return getProposalCount(networkType).add(BigInteger.ONE);
    }

    // ========== Admin 지갑 관련 ==========

    /**
     * Admin 지갑 정보 조회 (캐시 우선)
     */
    public AdminWalletInfo getAdminWallet(NetworkType networkType) {
        try {
            // 캐시에 있으면 반환
            AdminWalletInfo cached = adminWalletCache.get(networkType);
            if (cached != null) {
                log.debug("📋 Admin wallet from cache");
                return cached;
            }

            // 캐시에 없으면 로딩
            log.info("🔄 Loading Admin wallet for {}", networkType);
            AdminWalletInfo wallet = loadAdminWallet(networkType);

            // 캐시에 저장
            adminWalletCache.put(networkType, wallet);
            log.info("✅ Admin wallet cached");

            return wallet;

        } catch (Exception e) {
            log.error("❌ Failed to get Admin wallet: {}", e.getMessage());
            throw new RuntimeException("Failed to get Admin wallet", e);
        }
    }

    /**
     * Admin 지갑 로딩 (DB → Hardcoded 순서)
     */
    private AdminWalletInfo loadAdminWallet(NetworkType networkType) {
        try {
            // 1. DB에서 찾기
            UUID adminId = UUID.fromString(ADMIN_USER_ID);
            var walletOpt = walletRepository.findByUser_IdAndNetworkType(adminId, networkType);

            if (walletOpt.isPresent()) {
                Wallet wallet = walletOpt.get();
                log.info("📊 Admin wallet found in DB: {}", maskAddress(wallet.getWalletAddress()));

                return new AdminWalletInfo(
                        wallet.getWalletAddress(),
                        wallet.getEncryptedPrivateKey(),
                        "DATABASE"
                );
            }

            // 2. DB에 없으면 하드코딩된 값 사용
            log.warn("⚠️ Using hardcoded admin wallet for {}", networkType);
            return getHardcodedWallet(networkType);

        } catch (Exception e) {
            log.error("❌ DB load failed, using hardcoded wallet: {}", e.getMessage());
            return getHardcodedWallet(networkType);
        }
    }

    /**
     * 하드코딩된 지갑 정보
     */
    private AdminWalletInfo getHardcodedWallet(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                return new AdminWalletInfo(
                        "0x55D5c49e36f8A89111687C9DC8355121068f0cD8",
                        "jgTiIrbjpucJ2NUHooYdbdNj7UPGgh6wXEd1CyyPjilgjUyIPfLDc14Uc4JtKcbERMapTSfGGwIZLSDKo6MYAq3484rzkrmLSYUAIUm6hX4=",
                        "HARDCODED"
                );
            case SOLANA:
                return new AdminWalletInfo(
                        "1111111111111111111GKcUfEEzaVazsaWLw3LNAzcnR76Dw6b6xGgSvvX7uVdJ",
                        "e5VDLJh2hMbScH1aH3PrPXuVQyyYdoTG0nB9Wh9z6z0n+LmM7dqMul+Wrz+OXvxQC91GI065xCqyrJZFUFe4eKMDlW0wSjm4wUxEZFpRtNc/nivR495rn0y6a/5GJa/+nzysv9kqqO8QAXA4o5lOBI9D4GolW5GbLe4vnJ1klg0=",
                        "HARDCODED"
                );
            default:
                throw new IllegalArgumentException("Unsupported network: " + networkType);
        }
    }

    // ========== 캐시 관리 ==========

    /**
     * ProposalCount 캐시 무효화
     */
    public void clearProposalCountCache(NetworkType networkType) {
        proposalCountCache.remove(networkType);
        log.info("🗑️ ProposalCount cache cleared for {}", networkType);
    }

    /**
     * Admin 지갑 캐시 무효화
     */
    public void clearAdminWalletCache(NetworkType networkType) {
        adminWalletCache.remove(networkType);
        log.info("🗑️ Admin wallet cache cleared for {}", networkType);
    }

    /**
     * 모든 캐시 무효화
     */
    public void clearAllCache() {
        proposalCountCache.clear();
        adminWalletCache.clear();
        log.info("🗑️ All caches cleared");
    }

    /**
     * ProposalCount 새로고침
     */
    public BigInteger refreshProposalCount(NetworkType networkType) {
        clearProposalCountCache(networkType);
        return getProposalCount(networkType);
    }

    // ========== 유틸리티 ==========

    /**
     * 지갑 주소 마스킹
     */
    private String maskAddress(String address) {
        if (address == null || address.length() < 10) return "****";
        return address.substring(0, 6) + "****" + address.substring(address.length() - 4);
    }

    // ========== 간단한 데이터 클래스 ==========

    public static class AdminWalletInfo {
        private final String walletAddress;
        private final String encryptedPrivateKey;
        private final String source;

        public AdminWalletInfo(String walletAddress, String encryptedPrivateKey, String source) {
            this.walletAddress = walletAddress;
            this.encryptedPrivateKey = encryptedPrivateKey;
            this.source = source;
        }

        public String getWalletAddress() {
            return walletAddress;
        }

        public String getEncryptedPrivateKey() {
            return encryptedPrivateKey;
        }

        public String getSource() {
            return source;
        }

        public String getMaskedAddress() {
            if (walletAddress == null || walletAddress.length() < 10) return "****";
            return walletAddress.substring(0, 6) + "****" + walletAddress.substring(walletAddress.length() - 4);
        }
    }
}