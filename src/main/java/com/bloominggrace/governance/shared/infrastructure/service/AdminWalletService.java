package com.bloominggrace.governance.shared.infrastructure.service;

import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.util.JsonRpcClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AdminWalletService {
    
    // Static 캐시로 변경
    private static final Map<NetworkType, AdminWalletInfo> ADMIN_WALLETS = new ConcurrentHashMap<>();
    private static final Map<String, BigInteger> NONCE_CACHE = new ConcurrentHashMap<>();
    
    // Static 필드로 변경
    private static WalletRepository walletRepository;
    private static BlockchainClientFactory blockchainClientFactory;
    private static JsonRpcClient jsonRpcClient;
    private static ObjectMapper objectMapper;
    private static String ethereumRpcUrl;
    
    @Autowired
    public AdminWalletService(WalletRepository walletRepository, 
                            BlockchainClientFactory blockchainClientFactory,
                            JsonRpcClient jsonRpcClient,
                            ObjectMapper objectMapper,
                            @Value("${blockchain.ethereum.rpc-url}") String ethereumRpcUrl) {
        // Static 필드 초기화
        AdminWalletService.walletRepository = walletRepository;
        AdminWalletService.blockchainClientFactory = blockchainClientFactory;
        AdminWalletService.jsonRpcClient = jsonRpcClient;
        AdminWalletService.objectMapper = objectMapper;
        AdminWalletService.ethereumRpcUrl = ethereumRpcUrl;
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
            // User 관계를 통해 조회하는 새로운 메서드 사용
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
     * Admin 지갑의 nonce를 블록체인에서 실시간으로 가져옵니다 (캐싱 없음).
     */
    public static BigInteger getAdminWalletNonce(NetworkType networkType) {
        AdminWalletInfo adminWallet = getAdminWallet(networkType);
        if (adminWallet == null) {
            throw new RuntimeException("Admin wallet not found for network: " + networkType);
        }
        
        String walletAddress = adminWallet.getWalletAddress();
        log.info("Loading nonce from blockchain for wallet: {} (network: {})", walletAddress, networkType);
        
        try {
            BigInteger nonce = loadNonceFromBlockchain(networkType, walletAddress);
            log.info("Nonce loaded from blockchain for wallet: {} = {}", walletAddress, nonce);
            return nonce;
        } catch (Exception e) {
            log.error("Failed to load nonce from blockchain for wallet: {}", walletAddress, e);
            // 블록체인에서 로딩 실패 시 0으로 시작
            log.warn("Admin wallet nonce initialized to 0 for wallet: {}", walletAddress);
            return BigInteger.ZERO;
        }
    }
    
    /**
     * Admin 지갑의 nonce를 증가시킵니다 (트랜잭션 사용 후).
     * 캐싱을 사용하지 않으므로 실제로는 아무것도 하지 않습니다.
     * 다음 트랜잭션 시 블록체인에서 최신 논스를 가져옵니다.
     */
    public static void incrementAdminWalletNonce(NetworkType networkType) {
        AdminWalletInfo adminWallet = getAdminWallet(networkType);
        if (adminWallet == null) {
            throw new RuntimeException("Admin wallet not found for network: " + networkType);
        }
        
        String walletAddress = adminWallet.getWalletAddress();
        log.info("Nonce increment requested for wallet: {} (network: {}). Next transaction will use fresh nonce from blockchain.", walletAddress, networkType);
    }
    
    /**
     * 블록체인에서 nonce를 로딩합니다.
     */
    private static BigInteger loadNonceFromBlockchain(NetworkType networkType, String walletAddress) {
        try {
            log.info("Loading nonce from blockchain for network: {}, wallet: {}", networkType, walletAddress);
            
            if (networkType == NetworkType.ETHEREUM) {
                log.info("Using direct JSON-RPC call for Ethereum nonce");
                return loadEthereumNonceDirectly(walletAddress);
            } else {
                log.info("Using BlockchainClient for nonce (network: {})", networkType);
                // Solana는 기존 방식 사용
                BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
                String nonceStr = blockchainClient.getNonce(walletAddress);
                
                // hex 문자열을 BigInteger로 변환
                if (nonceStr.startsWith("0x")) {
                    nonceStr = nonceStr.substring(2);
                }
                
                BigInteger nonce = new BigInteger(nonceStr, 16);
                log.info("Loaded nonce from blockchain for wallet: {} = {}", walletAddress, nonce);
                return nonce;
            }
        } catch (Exception e) {
            log.error("Failed to load nonce from blockchain for wallet: {}", walletAddress, e);
            // 실패 시 0으로 시작
            return BigInteger.ZERO;
        }
    }
    
    /**
     * Ethereum nonce를 직접 JSON-RPC로 로딩합니다.
     */
    private static BigInteger loadEthereumNonceDirectly(String walletAddress) throws IOException, InterruptedException {
        log.info("Loading nonce directly from Ethereum RPC for wallet: {}", walletAddress);
        
        try {
            // JsonRpcClient를 사용하여 요청 전송
            Map<String, Object> responseMap = jsonRpcClient.sendSimpleRequest(
                ethereumRpcUrl,
                "eth_getTransactionCount",
                new Object[]{walletAddress, "latest"},
                new TypeReference<Map<String, Object>>() {}
            );
            
            // 에러 체크
            JsonRpcClient.checkForError(responseMap);
            
            // 결과 추출
            String result = (String) JsonRpcClient.extractResult(responseMap);
            
            // hex를 decimal로 변환
            BigInteger nonce = JsonRpcClient.hexToBigInteger(result);
            log.info("Loaded nonce directly from Ethereum RPC for wallet: {} = {}", walletAddress, nonce);
            return nonce;
            
        } catch (Exception e) {
            log.error("Failed to load nonce directly from Ethereum RPC for wallet: {}", walletAddress, e);
            throw e;
        }
    }
    
    /**
     * 특정 지갑의 nonce 캐시를 초기화합니다.
     * 캐싱을 사용하지 않으므로 아무것도 하지 않습니다.
     */
    public static void clearNonceCache(NetworkType networkType) {
        AdminWalletInfo adminWallet = getAdminWallet(networkType);
        if (adminWallet != null) {
            String walletAddress = adminWallet.getWalletAddress();
            NONCE_CACHE.remove(walletAddress);
            log.info("Nonce cache cleared for wallet: {} (network: {})", walletAddress, networkType);
        }
    }
    
    /**
     * 특정 지갑 주소의 nonce 캐시를 초기화합니다.
     */
    public static void clearNonceCacheByAddress(String walletAddress) {
        NONCE_CACHE.remove(walletAddress);
        log.info("Nonce cache cleared for wallet address: {}", walletAddress);
    }
    
    /**
     * Admin 지갑의 nonce를 강제로 블록체인에서 다시 로딩합니다.
     */
    public static BigInteger forceReloadAdminWalletNonce(NetworkType networkType) {
        AdminWalletInfo adminWallet = getAdminWallet(networkType);
        if (adminWallet == null) {
            throw new RuntimeException("Admin wallet not found for network: " + networkType);
        }
        
        String walletAddress = adminWallet.getWalletAddress();
        log.info("Force reloading nonce from blockchain for wallet: {} (network: {})", walletAddress, networkType);
        
        // 캐시 클리어
        NONCE_CACHE.remove(walletAddress);
        
        // 블록체인에서 다시 로딩
        BigInteger nonce = loadNonceFromBlockchain(networkType, walletAddress);
        log.info("Force reloaded nonce from blockchain for wallet: {} = {}", walletAddress, nonce);
        return nonce;
    }
    
    /**
     * Admin 지갑의 nonce를 강제로 0으로 설정합니다.
     */
    public static BigInteger forceSetAdminWalletNonceToZero(NetworkType networkType) {
        AdminWalletInfo adminWallet = getAdminWallet(networkType);
        if (adminWallet == null) {
            throw new RuntimeException("Admin wallet not found for network: " + networkType);
        }
        
        String walletAddress = adminWallet.getWalletAddress();
        log.warn("Force setting nonce to 0 for wallet: {} (network: {})", walletAddress, networkType);
        
        NONCE_CACHE.put(walletAddress, BigInteger.ZERO);
        return BigInteger.ZERO;
    }
    
    /**
     * 모든 nonce 캐시를 초기화합니다.
     */
    public static void clearAllNonceCache() {
        NONCE_CACHE.clear();
        log.info("All nonce cache cleared");
    }
    
    /**
     * Admin 지갑의 nonce를 증가시킵니다.
     */
    public static BigInteger incrementAdminNonce(NetworkType networkType) {
        AdminWalletInfo adminWallet = getAdminWallet(networkType);
        if (adminWallet == null) {
            throw new RuntimeException("Admin wallet not found for network: " + networkType);
        }
        
        String walletAddress = adminWallet.getWalletAddress();
        BigInteger currentNonce = NONCE_CACHE.getOrDefault(walletAddress, BigInteger.ZERO);
        BigInteger newNonce = currentNonce.add(BigInteger.ONE);
        
        NONCE_CACHE.put(walletAddress, newNonce);
        log.info("Incremented admin nonce for wallet: {} = {} -> {}", walletAddress, currentNonce, newNonce);
        
        return newNonce;
    }
    
    /**
     * Admin 지갑의 현재 nonce를 가져옵니다.
     */
    public static BigInteger getCurrentAdminNonce(NetworkType networkType) {
        AdminWalletInfo adminWallet = getAdminWallet(networkType);
        if (adminWallet == null) {
            throw new RuntimeException("Admin wallet not found for network: " + networkType);
        }
        
        String walletAddress = adminWallet.getWalletAddress();
        BigInteger currentNonce = NONCE_CACHE.getOrDefault(walletAddress, BigInteger.ZERO);
        log.debug("Current admin nonce for wallet: {} = {}", walletAddress, currentNonce);
        
        return currentNonce;
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