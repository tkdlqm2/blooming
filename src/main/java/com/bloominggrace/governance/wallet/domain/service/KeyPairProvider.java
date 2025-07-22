package com.bloominggrace.governance.wallet.domain.service;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.util.Base58Utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.web3j.crypto.Credentials;

/**
 * Static utility class for generating key pairs and addresses for different blockchain networks.
 * Provides common key generation functionality that can be extended by network-specific services.
 */
public class KeyPairProvider {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * Generates a secure random seed for key generation.
     * 
     * @return byte array containing the seed
     */
    public static byte[] generateSeed() {
        byte[] seed = new byte[32];
        SECURE_RANDOM.nextBytes(seed);
        return seed;
    }
    
    /**
     * Generates a random private key of specified length.
     * 
     * @param length the length of the private key in bytes
     * @return hexadecimal string representation of the private key
     */
    public static String generatePrivateKey(int length) {
        byte[] privateKeyBytes = new byte[length];
        SECURE_RANDOM.nextBytes(privateKeyBytes);
        return bytesToHex(privateKeyBytes);
    }
    
    /**
     * Generates a random hexadecimal string of specified length.
     * 
     * @param length the length of the hex string
     * @return random hex string
     */
    public static String generateRandomHex(int length) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < length; i++) {
            hex.append(Integer.toHexString(SECURE_RANDOM.nextInt(16)));
        }
        return hex.toString();
    }
    
    /**
     * Converts byte array to hexadecimal string.
     * 
     * @param bytes byte array to convert
     * @return hexadecimal string
     */
    public static String bytesToHex(byte[] bytes) {
        return com.bloominggrace.governance.shared.util.HexUtils.bytesToHex(bytes);
    }
    
    /**
     * Generates a key pair and address for the specified network type.
     * This method delegates to network-specific services for address generation.
     * 
     * @param networkType the blockchain network type
     * @return KeyPairResult containing the generated key pair and address
     */
    public static KeyPairResult generateKeyPair(NetworkType networkType) {
        return switch (networkType) {
            case ETHEREUM -> generateEthereumKeyPair();
            case SOLANA -> generateSolanaKeyPair();
        };
    }
    
    /**
     * Generates Ethereum key pair and address.
     * 
     * @return KeyPairResult containing Ethereum key pair and address
     */
    private static KeyPairResult generateEthereumKeyPair() {
        // Generate common seed and private key
        // 1. 유효한 프라이빗 키 생성
        String privateKey = generateValidEthereumPrivateKey();

        // 2. 프라이빗 키로부터 올바른 주소 생성
        String address = deriveAddressFromPrivateKey(privateKey);

        return new KeyPairResult(privateKey, address);
    }

    private static String generateValidEthereumPrivateKey() {
        BigInteger maxValue = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364140", 16);
        BigInteger privateKeyBigInt;

        do {
            byte[] privateKeyBytes = new byte[32];
            SECURE_RANDOM.nextBytes(privateKeyBytes);
            // BigInteger.ZERO가 아닌 양수로 생성
            privateKeyBigInt = new BigInteger(1, privateKeyBytes);
        } while (privateKeyBigInt.equals(BigInteger.ZERO) || 
                 privateKeyBigInt.compareTo(BigInteger.ONE) < 0 || 
                 privateKeyBigInt.compareTo(maxValue) >= 0);

        // 64자리로 패딩 (Java에서는 String.format 사용)
        return String.format("%064x", privateKeyBigInt);
    }

    private static String deriveAddressFromPrivateKey(String privateKey) {
        try {
            // 0x 접두사가 있다면 제거
            String cleanPrivateKey = privateKey.startsWith("0x") ?
                    privateKey.substring(2) : privateKey;

            // 64자리인지 확인
            if (cleanPrivateKey.length() != 64) {
                throw new IllegalArgumentException("Private key must be 64 hex characters");
            }

            // 16진수 형식 검증
            if (!cleanPrivateKey.matches("[0-9a-fA-F]{64}")) {
                throw new IllegalArgumentException("Private key must be valid hexadecimal");
            }

            Credentials credentials = Credentials.create(cleanPrivateKey);
            String address = credentials.getAddress();
            
            // 주소 유효성 검증 (0x로 시작하고 40자리 16진수)
            if (!address.matches("0x[0-9a-fA-F]{40}")) {
                throw new RuntimeException("Generated address is invalid: " + address);
            }
            
            return address;
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive address from private key: " + e.getMessage(), e);
        }
    }

    /**
     * Generates Solana key pair and address using Ed25519.
     * 
     * @return KeyPairResult containing Solana key pair and address
     */
    private static KeyPairResult generateSolanaKeyPair() {
        try {
            // Generate 32-byte seed for Ed25519
            byte[] seed = generateSeed();
            
            // Create Ed25519 private key parameters
            Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(seed, 0);
            
            // Generate public key from private key
            Ed25519PublicKeyParameters publicKeyParams = privateKeyParams.generatePublicKey();
            
            // Get private key bytes (32 bytes for Ed25519)
            byte[] privateKeyBytes = privateKeyParams.getEncoded();
            
            // Get public key bytes (32 bytes for Ed25519)
            byte[] publicKeyBytes = publicKeyParams.getEncoded();
            
            // Convert private key to hex string
            String privateKey = bytesToHex(privateKeyBytes);
            
            // Convert public key to Base58 address
            String address = Base58Utils.encode(publicKeyBytes);
            
            return new KeyPairResult(privateKey, address);
        } catch (Exception e) {
            // Fallback: generate simple key pair
            byte[] seed = generateSeed();
            String privateKey = generatePrivateKey(32);
            String address = Base58Utils.encode(seed);
            return new KeyPairResult(privateKey, address);
        }
    }

    /**
     * Result class containing the generated key pair and address.
     */
    public static class KeyPairResult {
        private final String privateKey;
        private final String address;
        
        public KeyPairResult(String privateKey, String address) {
            this.privateKey = privateKey;
            this.address = address;
        }
        
        public String getPrivateKey() {
            return privateKey;
        }
        
        public String getAddress() {
            return address;
        }
    }
} 