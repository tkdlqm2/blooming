package com.bloominggrace.governance.shared.util;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BigInteger 관련 유틸리티 클래스
 * BigInteger 연산, 변환, 검증 기능을 제공합니다.
 */
public final class BigIntUtils {
    
    private BigIntUtils() {
        // 유틸리티 클래스는 인스턴스화 불가
    }
    
    /**
     * 문자열을 BigInteger로 변환
     * 
     * @param value 변환할 문자열
     * @return BigInteger
     * @throws NumberFormatException 잘못된 숫자 형식인 경우
     */
    public static BigInteger toBigInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigInteger.ZERO;
        }
        
        String trimmed = value.trim();
        
        // 16진수 확인
        if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
            return new BigInteger(trimmed.substring(2), 16);
        }
        
        // 10진수로 변환
        return new BigInteger(trimmed);
    }
    
    /**
     * 16진수 문자열을 BigInteger로 변환
     * 
     * @param hex 16진수 문자열
     * @return BigInteger
     */
    public static BigInteger fromHex(String hex) {
        if (hex == null || hex.trim().isEmpty()) {
            return BigInteger.ZERO;
        }
        
        String trimmed = hex.trim();
        
        // 0x 접두사 제거
        if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
            trimmed = trimmed.substring(2);
        }
        
        return new BigInteger(trimmed, 16);
    }
    
    /**
     * BigInteger를 16진수 문자열로 변환
     * 
     * @param bigInt 변환할 BigInteger
     * @return 16진수 문자열
     */
    public static String toHex(BigInteger bigInt) {
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
     * @return 패딩된 16진수 문자열
     */
    public static String toHexPadded(BigInteger bigInt, int length) {
        String hex = toHex(bigInt);
        return HexUtils.padLeft(hex, length);
    }
    
    /**
     * BigInteger를 0x 접두사가 있는 16진수 문자열로 변환
     * 
     * @param bigInt 변환할 BigInteger
     * @return 0x 접두사가 있는 16진수 문자열
     */
    public static String toHexWithPrefix(BigInteger bigInt) {
        return "0x" + toHex(bigInt);
    }
    
    /**
     * BigInteger를 바이트 배열로 변환
     * 
     * @param bigInt 변환할 BigInteger
     * @return 바이트 배열
     */
    public static byte[] toBytes(BigInteger bigInt) {
        if (bigInt == null) {
            return new byte[0];
        }
        return bigInt.toByteArray();
    }
    
    /**
     * 바이트 배열을 BigInteger로 변환
     * 
     * @param bytes 변환할 바이트 배열
     * @return BigInteger
     */
    public static BigInteger fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return BigInteger.ZERO;
        }
        return new BigInteger(bytes);
    }
    
    /**
     * BigInteger를 지정된 길이의 바이트 배열로 변환 (패딩 포함)
     * 
     * @param bigInt 변환할 BigInteger
     * @param length 원하는 바이트 배열 길이
     * @return 패딩된 바이트 배열
     */
    public static byte[] toBytesPadded(BigInteger bigInt, int length) {
        byte[] bytes = toBytes(bigInt);
        
        if (bytes.length > length) {
            throw new IllegalArgumentException("BigInteger too large for specified length");
        }
        
        byte[] result = new byte[length];
        System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
        return result;
    }
    
    /**
     * BigInteger를 32바이트 배열로 변환 (Ethereum용)
     * 
     * @param bigInt 변환할 BigInteger
     * @return 32바이트 배열
     */
    public static byte[] to32Bytes(BigInteger bigInt) {
        return toBytesPadded(bigInt, 32);
    }
    
    /**
     * BigInteger를 20바이트 배열로 변환 (Ethereum 주소용)
     * 
     * @param bigInt 변환할 BigInteger
     * @return 20바이트 배열
     */
    public static byte[] to20Bytes(BigInteger bigInt) {
        return toBytesPadded(bigInt, 20);
    }
    
    /**
     * BigInteger를 BigDecimal로 변환
     * 
     * @param bigInt 변환할 BigInteger
     * @return BigDecimal
     */
    public static BigDecimal toBigDecimal(BigInteger bigInt) {
        if (bigInt == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(bigInt);
    }
    
    /**
     * BigInteger를 지정된 소수점 자릿수로 BigDecimal로 변환
     * 
     * @param bigInt 변환할 BigInteger
     * @param scale 소수점 자릿수
     * @return BigDecimal
     */
    public static BigDecimal toBigDecimal(BigInteger bigInt, int scale) {
        if (bigInt == null) {
            return BigDecimal.ZERO.setScale(scale);
        }
        return new BigDecimal(bigInt).setScale(scale, RoundingMode.HALF_UP);
    }
    
    /**
     * BigInteger가 0인지 확인
     * 
     * @param bigInt 확인할 BigInteger
     * @return 0인 경우 true
     */
    public static boolean isZero(BigInteger bigInt) {
        return bigInt == null || bigInt.equals(BigInteger.ZERO);
    }
    
    /**
     * BigInteger가 양수인지 확인
     * 
     * @param bigInt 확인할 BigInteger
     * @return 양수인 경우 true
     */
    public static boolean isPositive(BigInteger bigInt) {
        return bigInt != null && bigInt.compareTo(BigInteger.ZERO) > 0;
    }
    
    /**
     * BigInteger가 음수인지 확인
     * 
     * @param bigInt 확인할 BigInteger
     * @return 음수인 경우 true
     */
    public static boolean isNegative(BigInteger bigInt) {
        return bigInt != null && bigInt.compareTo(BigInteger.ZERO) < 0;
    }
    
    /**
     * 두 BigInteger의 최대값 반환
     * 
     * @param a 첫 번째 BigInteger
     * @param b 두 번째 BigInteger
     * @return 최대값
     */
    public static BigInteger max(BigInteger a, BigInteger b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) > 0 ? a : b;
    }
    
    /**
     * 두 BigInteger의 최소값 반환
     * 
     * @param a 첫 번째 BigInteger
     * @param b 두 번째 BigInteger
     * @return 최소값
     */
    public static BigInteger min(BigInteger a, BigInteger b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) < 0 ? a : b;
    }
    
    /**
     * BigInteger를 안전하게 더하기
     * 
     * @param a 첫 번째 BigInteger
     * @param b 두 번째 BigInteger
     * @return 합계
     */
    public static BigInteger safeAdd(BigInteger a, BigInteger b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.add(b);
    }
    
    /**
     * BigInteger를 안전하게 빼기
     * 
     * @param a 첫 번째 BigInteger
     * @param b 두 번째 BigInteger
     * @return 차이
     */
    public static BigInteger safeSubtract(BigInteger a, BigInteger b) {
        if (a == null) return b != null ? b.negate() : BigInteger.ZERO;
        if (b == null) return a;
        return a.subtract(b);
    }
    
    /**
     * BigInteger를 안전하게 곱하기
     * 
     * @param a 첫 번째 BigInteger
     * @param b 두 번째 BigInteger
     * @return 곱
     */
    public static BigInteger safeMultiply(BigInteger a, BigInteger b) {
        if (a == null || b == null) return BigInteger.ZERO;
        return a.multiply(b);
    }
    
    /**
     * BigInteger를 안전하게 나누기
     * 
     * @param a 첫 번째 BigInteger
     * @param b 두 번째 BigInteger
     * @return 몫
     * @throws ArithmeticException b가 0인 경우
     */
    public static BigInteger safeDivide(BigInteger a, BigInteger b) {
        if (a == null) return BigInteger.ZERO;
        if (b == null || b.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Division by zero");
        }
        return a.divide(b);
    }
    
    /**
     * BigInteger를 지정된 범위로 제한
     * 
     * @param value 제한할 BigInteger
     * @param min 최소값
     * @param max 최대값
     * @return 제한된 값
     */
    public static BigInteger clamp(BigInteger value, BigInteger min, BigInteger max) {
        if (value == null) return min;
        if (value.compareTo(min) < 0) return min;
        if (value.compareTo(max) > 0) return max;
        return value;
    }
    
    /**
     * BigInteger의 비트 길이 반환
     * 
     * @param bigInt 확인할 BigInteger
     * @return 비트 길이
     */
    public static int bitLength(BigInteger bigInt) {
        return bigInt != null ? bigInt.bitLength() : 0;
    }
    
    /**
     * BigInteger가 지정된 비트 길이를 초과하는지 확인
     * 
     * @param bigInt 확인할 BigInteger
     * @param maxBits 최대 비트 길이
     * @return 초과하는 경우 true
     */
    public static boolean exceedsBitLength(BigInteger bigInt, int maxBits) {
        return bigInt != null && bigInt.bitLength() > maxBits;
    }
} 