package com.bloominggrace.governance.shared.util;

import java.math.BigInteger;

/**
 * 16진수 변환 관련 유틸리티 클래스
 * 바이트 배열과 16진수 문자열 간의 변환을 제공합니다.
 */
public final class HexUtils {
    
    private static final String HEX_CHARS = "0123456789abcdef";
    private static final String HEX_CHARS_UPPER = "0123456789ABCDEF";
    
    private HexUtils() {
        // 유틸리티 클래스는 인스턴스화 불가
    }
    
    /**
     * 바이트 배열을 16진수 문자열로 변환 (소문자)
     * 
     * @param bytes 변환할 바이트 배열
     * @return 16진수 문자열 (소문자)
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(HEX_CHARS.charAt((b >> 4) & 0x0F));
            result.append(HEX_CHARS.charAt(b & 0x0F));
        }
        return result.toString();
    }
    
    /**
     * 바이트 배열을 16진수 문자열로 변환 (대문자)
     * 
     * @param bytes 변환할 바이트 배열
     * @return 16진수 문자열 (대문자)
     */
    public static String bytesToHexUpper(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(HEX_CHARS_UPPER.charAt((b >> 4) & 0x0F));
            result.append(HEX_CHARS_UPPER.charAt(b & 0x0F));
        }
        return result.toString();
    }
    
    /**
     * 16진수 문자열을 바이트 배열로 변환
     * 
     * @param hex 변환할 16진수 문자열
     * @return 바이트 배열
     * @throws IllegalArgumentException 잘못된 16진수 문자열인 경우
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        
        // 0x 접두사 제거
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        
        // 길이가 짝수가 아니면 앞에 0 추가
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        
        int len = hex.length();
        byte[] data = new byte[len / 2];
        
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex string: " + hex);
            }
            
            data[i / 2] = (byte) ((high << 4) + low);
        }
        
        return data;
    }
    
    /**
     * 16진수 문자열을 BigInteger로 변환
     * 
     * @param hex 변환할 16진수 문자열
     * @return BigInteger
     */
    public static BigInteger hexToBigInteger(String hex) {
        if (hex == null || hex.isEmpty()) {
            return BigInteger.ZERO;
        }
        
        // 0x 접두사 제거
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        
        return new BigInteger(hex, 16);
    }
    
    /**
     * BigInteger를 16진수 문자열로 변환
     * 
     * @param bigInt 변환할 BigInteger
     * @return 16진수 문자열 (소문자)
     */
    public static String bigIntegerToHex(BigInteger bigInt) {
        if (bigInt == null) {
            return "0";
        }
        return bigInt.toString(16);
    }
    
    /**
     * BigInteger를 16진수 문자열로 변환 (지정된 길이로 패딩)
     * 
     * @param bigInt 변환할 BigInteger
     * @param length 원하는 16진수 문자열 길이
     * @return 16진수 문자열 (소문자, 왼쪽에 0으로 패딩)
     */
    public static String bigIntegerToHexPadded(BigInteger bigInt, int length) {
        String hex = bigIntegerToHex(bigInt);
        return padLeft(hex, length);
    }
    
    /**
     * 16진수 문자열이 유효한지 검증
     * 
     * @param hex 검증할 16진수 문자열
     * @return 유효한 경우 true
     */
    public static boolean isValidHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }
        
        // 0x 접두사 제거
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        
        // 길이가 짝수가 아니면 유효하지 않음
        if (hex.length() % 2 != 0) {
            return false;
        }
        
        // 모든 문자가 16진수 문자인지 확인
        return hex.matches("^[0-9a-fA-F]+$");
    }
    
    /**
     * 16진수 문자열을 왼쪽에 0으로 패딩
     * 
     * @param hex 패딩할 16진수 문자열
     * @param length 원하는 길이
     * @return 패딩된 16진수 문자열
     */
    public static String padLeft(String hex, int length) {
        if (hex == null) {
            hex = "";
        }
        
        while (hex.length() < length) {
            hex = "0" + hex;
        }
        return hex;
    }
    
    /**
     * 16진수 문자열을 오른쪽에 0으로 패딩
     * 
     * @param hex 패딩할 16진수 문자열
     * @param length 원하는 길이
     * @return 패딩된 16진수 문자열
     */
    public static String padRight(String hex, int length) {
        if (hex == null) {
            hex = "";
        }
        
        while (hex.length() < length) {
            hex = hex + "0";
        }
        return hex;
    }
    
    /**
     * 바이트 배열을 0x 접두사가 있는 16진수 문자열로 변환
     * 
     * @param bytes 변환할 바이트 배열
     * @return 0x 접두사가 있는 16진수 문자열
     */
    public static String bytesToHexWithPrefix(byte[] bytes) {
        return "0x" + bytesToHex(bytes);
    }
    
    /**
     * 0x 접두사가 있는 16진수 문자열을 바이트 배열로 변환
     * 
     * @param hex 변환할 16진수 문자열 (0x 접두사 포함 가능)
     * @return 바이트 배열
     */
    public static byte[] hexWithPrefixToBytes(String hex) {
        return hexToBytes(hex);
    }
    
    /**
     * 16진수 문자열을 32바이트 배열로 변환 (Ethereum 주소용)
     * 
     * @param hex 변환할 16진수 문자열
     * @return 32바이트 배열
     */
    public static byte[] hexTo32Bytes(String hex) {
        byte[] bytes = hexToBytes(hex);
        if (bytes.length > 32) {
            throw new IllegalArgumentException("Hex string too long for 32 bytes");
        }
        
        byte[] result = new byte[32];
        System.arraycopy(bytes, 0, result, 32 - bytes.length, bytes.length);
        return result;
    }
    
    /**
     * 16진수 문자열을 20바이트 배열로 변환 (Ethereum 주소용)
     * 
     * @param hex 변환할 16진수 문자열
     * @return 20바이트 배열
     */
    public static byte[] hexTo20Bytes(String hex) {
        byte[] bytes = hexToBytes(hex);
        if (bytes.length > 20) {
            throw new IllegalArgumentException("Hex string too long for 20 bytes");
        }
        
        byte[] result = new byte[20];
        System.arraycopy(bytes, 0, result, 20 - bytes.length, bytes.length);
        return result;
    }
} 