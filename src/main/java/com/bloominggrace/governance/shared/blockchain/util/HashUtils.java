package com.bloominggrace.governance.shared.blockchain.util;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 해시 함수 관련 유틸리티 클래스
 * Keccak-256, SHA-256 등 다양한 해시 알고리즘을 제공합니다.
 */
public final class HashUtils {
    
    private HashUtils() {
        // 유틸리티 클래스는 인스턴스화 불가
    }
    
    /**
     * Keccak-256 해시 함수 (Ethereum 표준)
     * 
     * @param input 해시할 바이트 배열
     * @return Keccak-256 해시 결과
     */
    public static byte[] keccak256(byte[] input) {
        try {
            Keccak.Digest256 digest = new Keccak.Digest256();
            return digest.digest(input);
        } catch (Exception e) {
            throw new RuntimeException("Keccak-256 hash algorithm not available", e);
        }
    }
    
    /**
     * Keccak-256 해시 함수 (문자열 입력)
     * 
     * @param input 해시할 문자열
     * @return Keccak-256 해시 결과
     */
    public static byte[] keccak256(String input) {
        return keccak256(input.getBytes());
    }
    
    /**
     * SHA-256 해시 함수
     * 
     * @param input 해시할 바이트 배열
     * @return SHA-256 해시 결과
     */
    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * SHA-256 해시 함수 (문자열 입력)
     * 
     * @param input 해시할 문자열
     * @return SHA-256 해시 결과
     */
    public static byte[] sha256(String input) {
        return sha256(input.getBytes());
    }
    
    /**
     * SHA-1 해시 함수
     * 
     * @param input 해시할 바이트 배열
     * @return SHA-1 해시 결과
     */
    public static byte[] sha1(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }
    
    /**
     * SHA-1 해시 함수 (문자열 입력)
     * 
     * @param input 해시할 문자열
     * @return SHA-1 해시 결과
     */
    public static byte[] sha1(String input) {
        return sha1(input.getBytes());
    }
    
    /**
     * MD5 해시 함수 (보안상 권장하지 않음, 호환성용)
     * 
     * @param input 해시할 바이트 배열
     * @return MD5 해시 결과
     */
    public static byte[] md5(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * MD5 해시 함수 (문자열 입력, 보안상 권장하지 않음, 호환성용)
     * 
     * @param input 해시할 문자열
     * @return MD5 해시 결과
     */
    public static byte[] md5(String input) {
        return md5(input.getBytes());
    }
    
    /**
     * RIPEMD-160 해시 함수 (Bitcoin 주소 생성에 사용)
     * 
     * @param input 해시할 바이트 배열
     * @return RIPEMD-160 해시 결과
     */
    public static byte[] ripemd160(byte[] input) {
        try {
            // BouncyCastle의 RIPEMD160 사용
            org.bouncycastle.jcajce.provider.digest.RIPEMD160.Digest digest = 
                new org.bouncycastle.jcajce.provider.digest.RIPEMD160.Digest();
            return digest.digest(input);
        } catch (Exception e) {
            throw new RuntimeException("RIPEMD-160 algorithm not available", e);
        }
    }
    
    /**
     * RIPEMD-160 해시 함수 (문자열 입력)
     * 
     * @param input 해시할 문자열
     * @return RIPEMD-160 해시 결과
     */
    public static byte[] ripemd160(String input) {
        return ripemd160(input.getBytes());
    }
    
    /**
     * 이중 SHA-256 해시 (Bitcoin에서 사용)
     * 
     * @param input 해시할 바이트 배열
     * @return 이중 SHA-256 해시 결과
     */
    public static byte[] doubleSha256(byte[] input) {
        return sha256(sha256(input));
    }
    
    /**
     * 이중 SHA-256 해시 (문자열 입력)
     * 
     * @param input 해시할 문자열
     * @return 이중 SHA-256 해시 결과
     */
    public static byte[] doubleSha256(String input) {
        return doubleSha256(input.getBytes());
    }
} 