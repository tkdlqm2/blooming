package com.bloominggrace.governance.wallet.domain.service;

import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.blockchain.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.blockchain.domain.model.SignedTransaction;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * 지갑 서비스 추상 클래스
 * 지갑 생성, 조회, 관리 기능을 담당하는 도메인 서비스
 */
public abstract class WalletService {
    
    protected ApplicationContext applicationContext;
    
    public WalletService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 지갑 주소로 복호화된 개인키를 가져옵니다.
     * 공통 함수로 모든 구현체에서 사용할 수 있습니다.
     *
     * @param fromWalletAddress 지갑 주소
     * @return 복호화된 개인키
     */
    public String getDecryptedPrivateKey(String fromWalletAddress) {
        // 일반 지갑의 경우 WalletApplicationService를 통해 개인키 조회
        WalletApplicationService walletApplicationService =
            applicationContext.getBean(WalletApplicationService.class);
        
        Optional<Wallet> walletOpt = walletApplicationService.getWalletByAddress(fromWalletAddress);
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("Wallet not found for address: " + fromWalletAddress);
        }
        Wallet wallet = walletOpt.get();
        return walletApplicationService.getDecryptedPrivateKey(
            new UserId(wallet.getUser().getId()),
            wallet.getNetworkType()
        );
    }
    
    /**
     * 새로운 지갑을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @param networkType 네트워크 타입 (ETHEREUM, SOLANA 등)
     * @return 생성된 지갑
     */
    public abstract Wallet createWallet(UserId userId, NetworkType networkType);
    
    /**
     * 지갑 주소로 지갑을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @return 지갑 정보
     */
    public abstract Optional<Wallet> findByAddress(String walletAddress);
    
    /**
     * 사용자의 모든 지갑을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자의 지갑 목록
     */
    public abstract List<Wallet> findByUserId(UserId userId);
    
    /**
     * 지갑을 저장합니다.
     * 
     * @param wallet 저장할 지갑
     * @return 저장된 지갑
     */
    public abstract Wallet save(Wallet wallet);

    
    /**
     * 지갑 활성화 상태를 변경합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param active 활성화 여부
     * @return 업데이트된 지갑
     */
    public abstract Wallet updateActiveStatus(String walletAddress, boolean active);


    /**
     * 주어진 메시지에 대해 개인키로 서명합니다.
     *
     * @param privateKey 개인키 (hex string)
     * @return 서명 결과 (byte[])
     */
    public abstract <T> byte[] sign(TransactionBody<T> transactionBody, String privateKey);



    /**
     * 지갑 주소의 유효성을 검증합니다.
     *
     * @param address 검증할 지갑 주소
     * @return 유효성 여부
     */
    public abstract boolean isValidAddress(String address);

    /**
     * 지갑을 잠금 해제하고 프라이빗 키의 유효성을 검증합니다.
     * 프라이빗 키를 복호화하여 다시 주소를 생성하고, 요청된 주소와 일치하는지 확인합니다.
     *
     * @param walletAddress 검증할 지갑 주소
     * @return UnlockResult 검증 결과
     */
    public abstract UnlockResult unlockWallet(String walletAddress);

    /**
     * 지갑 잠금 해제 결과를 담는 클래스
     */
    public static class UnlockResult {
        private final boolean success;
        private final String walletAddress;
        private final String message;
        private final String derivedAddress;
        private final boolean addressMatch;

        public UnlockResult(boolean success, String walletAddress, String message, String derivedAddress, boolean addressMatch) {
            this.success = success;
            this.walletAddress = walletAddress;
            this.message = message;
            this.derivedAddress = derivedAddress;
            this.addressMatch = addressMatch;
        }

        public static UnlockResult success(String walletAddress, String derivedAddress, boolean addressMatch) {
            return new UnlockResult(true, walletAddress, "Wallet unlocked successfully", derivedAddress, addressMatch);
        }

        public static UnlockResult failure(String walletAddress, String message) {
            return new UnlockResult(false, walletAddress, message, null, false);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getWalletAddress() { return walletAddress; }
        public String getMessage() { return message; }
        public String getDerivedAddress() { return derivedAddress; }
        public boolean isAddressMatch() { return addressMatch; }
    }
} 