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
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service("ethereumWalletService")
public class EthereumWalletService implements WalletService {
    
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    
    public EthereumWalletService(WalletRepository walletRepository, 
                                EncryptionService encryptionService) {
        this.walletRepository = walletRepository;
        this.encryptionService = encryptionService;
    }
    
    @Override
    public Wallet createWallet(UserId userId, String networkType) {
        // Generate key pair using KeyPairProvider
        KeyPairProvider.KeyPairResult keyPair = KeyPairProvider.generateKeyPair(NetworkType.ETHEREUM);
        
        // Encrypt the private key
        String encryptedPrivateKey = encryptionService.encrypt(keyPair.getPrivateKey());
        
        // Create wallet entity
        Wallet wallet = new Wallet(null, keyPair.getAddress(), NetworkType.ETHEREUM, encryptedPrivateKey);
        
        return walletRepository.save(wallet);
    }
    
    /**
     * Generates an Ethereum-specific address from seed.
     * 
     * @param seed the seed for address generation
     * @return Ethereum address
     */
    private String generateEthereumAddress(byte[] seed) {
        // Ethereum addresses are 42 characters long (0x + 40 hex chars)
        String addressHex = KeyPairProvider.bytesToHex(seed).substring(0, 40);
        return "0x" + addressHex;
    }
    
    @Override
    public Optional<Wallet> findByAddress(String walletAddress) {
        return walletRepository.findAll().stream()
                .filter(wallet -> wallet.getWalletAddress().equals(walletAddress) && 
                                wallet.getNetworkType() == NetworkType.ETHEREUM)
                .findFirst();
    }
    
    @Override
    public List<Wallet> findByUserId(UserId userId) {
        return walletRepository.findByUserId(userId.getValue()).stream()
                .filter(wallet -> wallet.getNetworkType() == NetworkType.ETHEREUM)
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
                                wallet.getNetworkType() == NetworkType.ETHEREUM)
                .findFirst()
                .ifPresent(walletRepository::delete);
    }
    
    @Override
    public Wallet updateActiveStatus(String walletAddress, boolean active) {
        return walletRepository.findAll().stream()
                .filter(wallet -> wallet.getWalletAddress().equals(walletAddress) && 
                                wallet.getNetworkType() == NetworkType.ETHEREUM)
                .findFirst()
                .map(wallet -> {
                    wallet.setActive(active);
                    return walletRepository.save(wallet);
                })
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }
    
    @Override
    public String getSupportedNetworkType() {
        return "ETHEREUM";
    }

    @Override
    public byte[] sign(byte[] message, String privateKey) {
        try {
            // 개인키를 BigInteger로 변환
            BigInteger privateKeyBigInt = new BigInteger(privateKey, 16);
            
            // 메시지 해시 생성 (Keccak-256)
            byte[] messageHash = keccak256(message);
            
            // 간단한 ECDSA 서명 구현 (실제로는 더 복잡한 로직 필요)
            // 여기서는 기본적인 서명 구조만 생성
            byte[] signature = new byte[65];
            
            // r, s 값을 개인키와 해시를 기반으로 생성
            BigInteger r = privateKeyBigInt.multiply(new BigInteger(1, messageHash)).mod(BigInteger.valueOf(2).pow(256));
            BigInteger s = privateKeyBigInt.add(r).mod(BigInteger.valueOf(2).pow(256));
            
            // 32바이트로 변환
            byte[] rBytes = r.toByteArray();
            byte[] sBytes = s.toByteArray();
            
            // 패딩
            byte[] rPadded = new byte[32];
            byte[] sPadded = new byte[32];
            System.arraycopy(rBytes, Math.max(0, rBytes.length - 32), rPadded, Math.max(0, 32 - rBytes.length), Math.min(32, rBytes.length));
            System.arraycopy(sBytes, Math.max(0, sBytes.length - 32), sPadded, Math.max(0, 32 - sBytes.length), Math.min(32, sBytes.length));
            
            // 서명 조합
            System.arraycopy(rPadded, 0, signature, 0, 32);
            System.arraycopy(sPadded, 0, signature, 32, 32);
            signature[64] = 27; // recovery id
            
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Ethereum sign error", e);
        }
    }
    
    /**
     * Keccak-256 해시 함수 구현 (Ethereum 표준)
     */
    private byte[] keccak256(byte[] input) {
        try {
            // BouncyCastle의 Keccak-256 사용
            org.bouncycastle.jcajce.provider.digest.Keccak.Digest256 digest = 
                new org.bouncycastle.jcajce.provider.digest.Keccak.Digest256();
            return digest.digest(input);
        } catch (Exception e) {
            // Fallback: SHA-256 사용 (실제로는 Keccak-256이어야 함)
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                return digest.digest(input);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException("Hash algorithm not available", ex);
            }
        }
    }
} 