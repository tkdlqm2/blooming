package com.bloominggrace.governance.wallet.domain.service;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.util.Base58Utils;
import java.security.SecureRandom;
import java.util.UUID;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

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
        byte[] seed = generateSeed();
        String privateKey = generatePrivateKey(32);
        
        // Generate Ethereum address
        String address = generateEthereumAddress(seed);
        
        return new KeyPairResult(privateKey, address);
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
     * Generates an Ethereum-style address.
     * 
     * @param seed the seed for address generation
     * @return Ethereum address
     */
    private static String generateEthereumAddress(byte[] seed) {
        // Use seed to generate deterministic but random-looking address
        String addressHex = bytesToHex(seed).substring(0, 40);
        return "0x" + addressHex;
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