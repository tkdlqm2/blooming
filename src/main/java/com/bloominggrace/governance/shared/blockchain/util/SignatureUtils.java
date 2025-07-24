package com.bloominggrace.governance.shared.blockchain.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 서명 관련 유틸리티 클래스
 * 서명 생성, 검증, 인코딩/디코딩 기능을 제공합니다.
 */
public final class SignatureUtils {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    private SignatureUtils() {
        // 유틸리티 클래스는 인스턴스화 불가
    }
    
    /**
     * 바이트 배열을 Base64로 인코딩
     * 
     * @param data 인코딩할 바이트 배열
     * @return Base64 인코딩된 문자열
     */
    public static String encodeBase64(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        return Base64.getEncoder().encodeToString(data);
    }
    
    /**
     * Base64 문자열을 바이트 배열로 디코딩
     * 
     * @param base64 디코딩할 Base64 문자열
     * @return 디코딩된 바이트 배열
     * @throws IllegalArgumentException 잘못된 Base64 형식인 경우
     */
    public static byte[] decodeBase64(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            return new byte[0];
        }
        
        try {
            return Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 string: " + base64, e);
        }
    }
    
    /**
     * 바이트 배열을 Base64 URL 안전 형식으로 인코딩
     * 
     * @param data 인코딩할 바이트 배열
     * @return Base64 URL 안전 인코딩된 문자열
     */
    public static String encodeBase64Url(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
    
    /**
     * Base64 URL 안전 문자열을 바이트 배열로 디코딩
     * 
     * @param base64Url 디코딩할 Base64 URL 안전 문자열
     * @return 디코딩된 바이트 배열
     * @throws IllegalArgumentException 잘못된 Base64 형식인 경우
     */
    public static byte[] decodeBase64Url(String base64Url) {
        if (base64Url == null || base64Url.trim().isEmpty()) {
            return new byte[0];
        }
        
        try {
            return Base64.getUrlDecoder().decode(base64Url.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 URL string: " + base64Url, e);
        }
    }
    
    /**
     * ECDSA 서명을 DER 형식으로 인코딩
     * 
     * @param r r 값
     * @param s s 값
     * @return DER 인코딩된 서명
     */
    public static byte[] encodeECDSASignature(BigInteger r, BigInteger s) {
        byte[] rBytes = r.toByteArray();
        byte[] sBytes = s.toByteArray();
        
        // DER 인코딩: 0x30 + length + 0x02 + rLength + r + 0x02 + sLength + s
        int totalLength = 2 + rBytes.length + 2 + sBytes.length;
        byte[] signature = new byte[totalLength + 2];
        
        int index = 0;
        signature[index++] = 0x30; // SEQUENCE
        signature[index++] = (byte) totalLength;
        signature[index++] = 0x02; // INTEGER
        signature[index++] = (byte) rBytes.length;
        System.arraycopy(rBytes, 0, signature, index, rBytes.length);
        index += rBytes.length;
        signature[index++] = 0x02; // INTEGER
        signature[index++] = (byte) sBytes.length;
        System.arraycopy(sBytes, 0, signature, index, sBytes.length);
        
        return signature;
    }
    
    /**
     * ECDSA 서명을 R, S 값으로 분해
     * 
     * @param signature DER 인코딩된 서명
     * @return [r, s] 배열
     * @throws IllegalArgumentException 잘못된 서명 형식인 경우
     */
    public static BigInteger[] decodeECDSASignature(byte[] signature) {
        if (signature == null || signature.length < 6) {
            throw new IllegalArgumentException("Invalid ECDSA signature");
        }
        
        try {
            int index = 0;
            
            // SEQUENCE 확인
            if (signature[index++] != 0x30) {
                throw new IllegalArgumentException("Invalid DER sequence");
            }
            
            int sequenceLength = signature[index++] & 0xFF;
            if (signature.length != sequenceLength + 2) {
                throw new IllegalArgumentException("Invalid signature length");
            }
            
            // R 값 파싱
            if (signature[index++] != 0x02) {
                throw new IllegalArgumentException("Invalid R integer marker");
            }
            int rLength = signature[index++] & 0xFF;
            byte[] rBytes = new byte[rLength];
            System.arraycopy(signature, index, rBytes, 0, rLength);
            index += rLength;
            
            // S 값 파싱
            if (signature[index++] != 0x02) {
                throw new IllegalArgumentException("Invalid S integer marker");
            }
            int sLength = signature[index++] & 0xFF;
            byte[] sBytes = new byte[sLength];
            System.arraycopy(signature, index, sBytes, 0, sLength);
            
            BigInteger r = new BigInteger(rBytes);
            BigInteger s = new BigInteger(sBytes);
            
            return new BigInteger[]{r, s};
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode ECDSA signature", e);
        }
    }
    
    /**
     * Ethereum 스타일 서명을 R, S, V 값으로 분해
     * 
     * @param signature 65바이트 Ethereum 서명
     * @return [r, s, v] 배열
     * @throws IllegalArgumentException 잘못된 서명 형식인 경우
     */
    public static BigInteger[] decodeEthereumSignature(byte[] signature) {
        if (signature == null || signature.length != 65) {
            throw new IllegalArgumentException("Ethereum signature must be 65 bytes");
        }
        
        byte[] rBytes = new byte[32];
        byte[] sBytes = new byte[32];
        
        System.arraycopy(signature, 0, rBytes, 0, 32);
        System.arraycopy(signature, 32, sBytes, 0, 32);
        
        BigInteger r = new BigInteger(1, rBytes);
        BigInteger s = new BigInteger(1, sBytes);
        BigInteger v = BigInteger.valueOf(signature[64] & 0xFF);
        
        return new BigInteger[]{r, s, v};
    }
    
    /**
     * R, S, V 값을 Ethereum 스타일 서명으로 조합
     * 
     * @param r r 값
     * @param s s 값
     * @param v v 값 (recovery id)
     * @return 65바이트 Ethereum 서명
     */
    public static byte[] encodeEthereumSignature(BigInteger r, BigInteger s, BigInteger v) {
        byte[] signature = new byte[65];
        
        byte[] rBytes = BigIntUtils.to32Bytes(r);
        byte[] sBytes = BigIntUtils.to32Bytes(s);
        
        System.arraycopy(rBytes, 0, signature, 0, 32);
        System.arraycopy(sBytes, 0, signature, 32, 32);
        signature[64] = v.byteValue();
        
        return signature;
    }
    
    /**
     * 서명을 16진수 문자열로 변환
     * 
     * @param signature 서명 바이트 배열
     * @return 16진수 문자열
     */
    public static String signatureToHex(byte[] signature) {
        return HexUtils.bytesToHex(signature);
    }
    
    /**
     * 16진수 문자열을 서명 바이트 배열로 변환
     * 
     * @param hex 16진수 문자열
     * @return 서명 바이트 배열
     */
    public static byte[] hexToSignature(String hex) {
        return HexUtils.hexToBytes(hex);
    }
    
    /**
     * 서명을 0x 접두사가 있는 16진수 문자열로 변환
     * 
     * @param signature 서명 바이트 배열
     * @return 0x 접두사가 있는 16진수 문자열
     */
    public static String signatureToHexWithPrefix(byte[] signature) {
        return HexUtils.bytesToHexWithPrefix(signature);
    }
    
    /**
     * 0x 접두사가 있는 16진수 문자열을 서명 바이트 배열로 변환
     * 
     * @param hex 0x 접두사가 있는 16진수 문자열
     * @return 서명 바이트 배열
     */
    public static byte[] hexWithPrefixToSignature(String hex) {
        return HexUtils.hexWithPrefixToBytes(hex);
    }
    
    /**
     * 안전한 난수 생성
     * 
     * @param length 생성할 바이트 길이
     * @return 난수 바이트 배열
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * 안전한 난수 BigInteger 생성
     * 
     * @param bitLength 생성할 비트 길이
     * @return 난수 BigInteger
     */
    public static BigInteger generateRandomBigInteger(int bitLength) {
        return new BigInteger(bitLength, SECURE_RANDOM);
    }
    
    /**
     * 서명 길이 검증
     * 
     * @param signature 검증할 서명
     * @param expectedLength 예상 길이
     * @return 길이가 일치하는 경우 true
     */
    public static boolean hasValidLength(byte[] signature, int expectedLength) {
        return signature != null && signature.length == expectedLength;
    }
    
    /**
     * Ethereum 서명 길이 검증 (65바이트)
     * 
     * @param signature 검증할 서명
     * @return 유효한 경우 true
     */
    public static boolean isValidEthereumSignature(byte[] signature) {
        return hasValidLength(signature, 65);
    }
    
    /**
     * Solana 서명 길이 검증 (64바이트)
     * 
     * @param signature 검증할 서명
     * @return 유효한 경우 true
     */
    public static boolean isValidSolanaSignature(byte[] signature) {
        return hasValidLength(signature, 64);
    }
    
    /**
     * 서명을 문자열로 변환 (Base64)
     * 
     * @param signature 서명 바이트 배열
     * @return Base64 인코딩된 문자열
     */
    public static String signatureToString(byte[] signature) {
        return encodeBase64(signature);
    }
    
    /**
     * 문자열을 서명 바이트 배열로 변환 (Base64)
     * 
     * @param str Base64 인코딩된 문자열
     * @return 서명 바이트 배열
     */
    public static byte[] stringToSignature(String str) {
        return decodeBase64(str);
    }
} 