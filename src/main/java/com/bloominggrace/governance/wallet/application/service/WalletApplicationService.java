package com.bloominggrace.governance.wallet.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.service.EncryptionService;
import com.bloominggrace.governance.user.application.service.UserService;
import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.domain.service.BlockchainClient;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import com.bloominggrace.governance.wallet.application.service.WalletServiceFactory;
import com.bloominggrace.governance.wallet.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.application.dto.CreateWalletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class WalletApplicationService {

    private final WalletRepository walletRepository;
    private final WalletServiceFactory walletServiceFactory;
    private final BlockchainClientFactory blockchainClientFactory;
    private final UserService userService;
    private final EncryptionService encryptionService;

    /**
     * 사용자 ID와 네트워크를 기반으로 암호화된 프라이빗 키를 DB에서 가져와 복호화합니다.
     * 
     * @param userId 사용자 ID
     * @param networkType 네트워크 타입
     * @return 복호화된 프라이빗 키
     * @throws RuntimeException 지갑을 찾을 수 없거나 복호화에 실패한 경우
     */
    public String getDecryptedPrivateKey(UserId userId, NetworkType networkType) {
        // 사용자 ID와 네트워크로 지갑을 찾습니다
        Optional<Wallet> walletOpt = walletRepository.findByUserIdAndNetworkType(userId, networkType);
        
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("지갑을 찾을 수 없습니다. 사용자 ID: " + userId + ", 네트워크: " + networkType);
        }
        
        Wallet wallet = walletOpt.get();
        String encryptedPrivateKey = wallet.getEncryptedPrivateKey();
        
        try {
            // 암호화된 프라이빗 키를 복호화합니다
            return encryptionService.decrypt(encryptedPrivateKey);
        } catch (Exception e) {
            throw new RuntimeException("프라이빗 키 복호화에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 지갑 주소를 기반으로 암호화된 프라이빗 키를 DB에서 가져와 복호화합니다.
     * 
     * @param walletAddress 지갑 주소
     * @return 복호화된 프라이빗 키
     * @throws RuntimeException 지갑을 찾을 수 없거나 복호화에 실패한 경우
     */
    public String getDecryptedPrivateKeyByAddress(String walletAddress) {
        // 지갑 주소로 지갑을 찾습니다
        Optional<Wallet> walletOpt = walletRepository.findByWalletAddress(walletAddress);
        
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("지갑을 찾을 수 없습니다. 주소: " + walletAddress);
        }
        
        Wallet wallet = walletOpt.get();
        String encryptedPrivateKey = wallet.getEncryptedPrivateKey();
        
        try {
            // 암호화된 프라이빗 키를 복호화합니다
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
        
        // 사용자가 존재하는지 확인
        Optional<User> userOpt = userService.findById(userId.getValue());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + request.getUserId());
        }
        
        // 네트워크별 지갑 서비스를 가져와서 지갑을 생성합니다
        WalletService walletService = walletServiceFactory.getWalletService(networkType.name());
        Wallet wallet = walletService.createWallet(userId, networkType.name());
        
        // 사용자와 연결
        wallet.setUser(userOpt.get());
        wallet = walletRepository.save(wallet);
        
        return WalletDto.from(wallet);
    }

    /**
     * 네이티브 토큰을 전송합니다. (DB에서 프라이빗 키를 자동으로 가져와 복호화)
     */
    public String sendTransaction(String fromAddress, String toAddress, BigDecimal amount, NetworkType networkType) {
        // 지갑 주소로 암호화된 프라이빗 키를 가져와 복호화
        String decryptedPrivateKey = getDecryptedPrivateKeyByAddress(fromAddress);
        
        // 블록체인 클라이언트를 사용하여 트랜잭션을 전송합니다
        BlockchainClient blockchainClient = blockchainClientFactory.getBlockchainClient(networkType.name());
        return blockchainClient.sendTransaction(fromAddress, toAddress, amount, decryptedPrivateKey);
    }

    /**
     * 토큰을 전송합니다. (DB에서 프라이빗 키를 자동으로 가져와 복호화)
     */
    public String sendToken(String fromAddress, String toAddress, String tokenAddress, BigDecimal amount, NetworkType networkType) {
        // 지갑 주소로 암호화된 프라이빗 키를 가져와 복호화
        String decryptedPrivateKey = getDecryptedPrivateKeyByAddress(fromAddress);
        
        // 블록체인 클라이언트를 사용하여 토큰을 전송합니다
        BlockchainClient blockchainClient = blockchainClientFactory.getBlockchainClient(networkType.name());
        return blockchainClient.sendToken(fromAddress, toAddress, tokenAddress, amount, decryptedPrivateKey);
    }

    /**
     * 메시지를 서명합니다. (DB에서 프라이빗 키를 자동으로 가져와 복호화)
     */
    public byte[] signMessage(String walletAddress, byte[] message, NetworkType networkType) {
        // 지갑 주소로 암호화된 프라이빗 키를 가져와 복호화
        String decryptedPrivateKey = getDecryptedPrivateKeyByAddress(walletAddress);
        
        // 네트워크별 지갑 서비스를 가져와서 메시지를 서명합니다
        WalletService walletService = walletServiceFactory.getWalletService(networkType.name());
        return walletService.sign(message, decryptedPrivateKey);
    }

    /**
     * 사용자 ID와 네트워크를 기반으로 메시지를 서명합니다.
     */
    public byte[] signMessageByUser(UserId userId, byte[] message, NetworkType networkType) {
        // 사용자 ID와 네트워크로 암호화된 프라이빗 키를 가져와 복호화
        String decryptedPrivateKey = getDecryptedPrivateKey(userId, networkType);
        
        // 사용자의 지갑 주소를 가져옵니다
        Optional<Wallet> walletOpt = walletRepository.findByUserIdAndNetworkType(userId, networkType);
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("지갑을 찾을 수 없습니다. 사용자 ID: " + userId + ", 네트워크: " + networkType);
        }
        
        String walletAddress = walletOpt.get().getWalletAddress();
        
        // 네트워크별 지갑 서비스를 가져와서 메시지를 서명합니다
        WalletService walletService = walletServiceFactory.getWalletService(networkType.name());
        return walletService.sign(message, decryptedPrivateKey);
    }

    /**
     * 잔액을 조회합니다.
     */
    public BigDecimal getBalance(String address, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getBlockchainClient(networkType.name());
        return blockchainClient.getBalance(address);
    }

    /**
     * 토큰 잔액을 조회합니다.
     */
    public BigDecimal getTokenBalance(String walletAddress, String tokenAddress, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getBlockchainClient(networkType.name());
        return blockchainClient.getTokenBalance(walletAddress, tokenAddress);
    }

    /**
     * 트랜잭션을 조회합니다.
     */
    public Optional<BlockchainClient.TransactionInfo> getTransaction(String txHash, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getBlockchainClient(networkType.name());
        return blockchainClient.getTransaction(txHash);
    }

    /**
     * 가스 가격을 조회합니다.
     */
    public BigDecimal getGasPrice(NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getBlockchainClient(networkType.name());
        return blockchainClient.getGasPrice();
    }

    /**
     * 네트워크 연결 상태를 확인합니다.
     */
    public boolean isNetworkConnected(NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getBlockchainClient(networkType.name());
        return blockchainClient.isNetworkConnected();
    }

    /**
     * 주소 유효성을 검증합니다.
     */
    public boolean validateAddress(String address, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getBlockchainClient(networkType.name());
        return blockchainClient.isValidAddress(address);
    }

    // 기존 findById, findAll, deleteWallet 등은 네트워크 구분 없이 repository 직접 접근
    @Transactional(readOnly = true)
    public Optional<Wallet> findById(UUID id) {
        return walletRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Wallet> findAll() {
        return walletRepository.findAll();
    }

    public void deleteWallet(UUID id) {
        walletRepository.deleteById(id);
    }
    
    /**
     * 지갑 활성화 상태를 변경합니다.
     */
    public Wallet updateWalletActiveStatus(String walletAddress, boolean active, NetworkType networkType) {
        WalletService walletService = walletServiceFactory.getWalletService(networkType.name());
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
     * 네트워크 타입으로 지갑 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Wallet> findByNetworkType(NetworkType networkType) {
        return walletRepository.findByNetworkType(networkType);
    }
} 