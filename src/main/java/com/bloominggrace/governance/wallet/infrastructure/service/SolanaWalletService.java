package com.bloominggrace.governance.wallet.infrastructure.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.service.EncryptionService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.domain.service.KeyPairProvider;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service("solanaWalletService")
public class SolanaWalletService implements WalletService {
    
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    
    public SolanaWalletService(WalletRepository walletRepository, 
                              EncryptionService encryptionService) {
        this.walletRepository = walletRepository;
        this.encryptionService = encryptionService;
    }
    
    @Override
    public Wallet createWallet(UserId userId, String networkType) {
        // Generate key pair using KeyPairProvider
        KeyPairProvider.KeyPairResult keyPair = KeyPairProvider.generateKeyPair(NetworkType.SOLANA);
        
        // Encrypt the private key
        String encryptedPrivateKey = encryptionService.encrypt(keyPair.getPrivateKey());
        
        // Create wallet entity
        Wallet wallet = new Wallet(null, keyPair.getAddress(), NetworkType.SOLANA, encryptedPrivateKey);
        
        return walletRepository.save(wallet);
    }
    
    /**
     * Generates a Solana-specific address from seed.
     * 
     * @param seed the seed for address generation
     * @return Solana address
     */
    private String generateSolanaAddress(byte[] seed) {
        // Solana addresses are typically 44 characters long
        String addressHex = KeyPairProvider.bytesToHex(seed);
        return addressHex.substring(0, Math.min(44, addressHex.length()));
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
    public void deleteByAddress(String walletAddress) {
        walletRepository.findAll().stream()
                .filter(wallet -> wallet.getWalletAddress().equals(walletAddress) && 
                                wallet.getNetworkType() == NetworkType.SOLANA)
                .findFirst()
                .ifPresent(walletRepository::delete);
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
    public String getSupportedNetworkType() {
        return "SOLANA";
    }

    @Override
    public byte[] sign(byte[] message, String privateKey) {
        try {
            // 개인키를 바이트 배열로 변환 (Solana는 64바이트 개인키 사용)
            byte[] privateKeyBytes = hexStringToByteArray(privateKey);
            
            // Ed25519 개인키 파라미터 생성
            Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(privateKeyBytes, 0);
            
            // Ed25519 서명기 생성
            Ed25519Signer signer = new Ed25519Signer();
            signer.init(true, privateKeyParams);
            
            // 메시지 서명
            signer.update(message, 0, message.length);
            byte[] signature = signer.generateSignature();
            
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Solana sign error", e);
        }
    }
    
    /**
     * 16진수 문자열을 바이트 배열로 변환
     */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
} 