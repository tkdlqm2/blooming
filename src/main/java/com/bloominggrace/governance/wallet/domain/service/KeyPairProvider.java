package com.bloominggrace.governance.wallet.domain.service;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import java.security.SecureRandom;
import java.util.UUID;

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
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
    
    /**
     * Generates a key pair and address for the specified network type.
     * This method delegates to network-specific services for address generation.
     * 
     * @param networkType the blockchain network type
     * @return KeyPairResult containing the generated key pair and address
     */
    public static KeyPairResult generateKeyPair(NetworkType networkType) {
        // Generate common seed and private key
        byte[] seed = generateSeed();
        String privateKey = generatePrivateKey(getPrivateKeyLength(networkType));
        
        // Generate network-specific address
        String address = generateNetworkAddress(networkType, seed);
        
        return new KeyPairResult(privateKey, address);
    }
    
    /**
     * Gets the private key length for the specified network type.
     * 
     * @param networkType the blockchain network type
     * @return private key length in bytes
     */
    private static int getPrivateKeyLength(NetworkType networkType) {
        return switch (networkType) {
            case ETHEREUM -> 32;  // Ethereum uses 32-byte private keys
            case SOLANA -> 64;    // Solana uses 64-byte private keys
        };
    }
    
    /**
     * Generates a network-specific address based on the seed.
     * 
     * @param networkType the blockchain network type
     * @param seed the seed for address generation
     * @return network-specific address
     */
    private static String generateNetworkAddress(NetworkType networkType, byte[] seed) {
        return switch (networkType) {
            case ETHEREUM -> generateEthereumAddress(seed);
            case SOLANA -> generateSolanaAddress(seed);
        };
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
     * Generates a Solana-style address.
     * 
     * @param seed the seed for address generation
     * @return Solana address
     */
    private static String generateSolanaAddress(byte[] seed) {
        // Solana addresses are typically 44 characters long
        // Use seed to generate deterministic but random-looking address
        String addressHex = bytesToHex(seed);
        return addressHex.substring(0, Math.min(44, addressHex.length()));
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