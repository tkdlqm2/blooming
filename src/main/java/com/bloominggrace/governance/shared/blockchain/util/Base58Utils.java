package com.bloominggrace.governance.shared.blockchain.util;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Base58 인코딩/디코딩 유틸리티 클래스
 * Bitcoin과 Solana에서 사용하는 Base58 인코딩을 지원합니다.
 */
public class Base58Utils {
    
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(58);
    
    /**
     * 바이트 배열을 Base58 문자열로 인코딩합니다.
     * 
     * @param input 인코딩할 바이트 배열
     * @return Base58 인코딩된 문자열
     */
    public static String encode(byte[] input) {
        if (input.length == 0) {
            return "";
        }
        
        // 앞의 0들을 세어서 나중에 추가할 개수를 계산
        int zeros = 0;
        while (zeros < input.length && input[zeros] == 0) {
            zeros++;
        }
        
        // 바이트 배열을 BigInteger로 변환
        BigInteger number = new BigInteger(1, input);
        StringBuilder result = new StringBuilder();
        
        // Base58로 변환
        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = number.divideAndRemainder(BASE);
            number = divmod[0];
            result.insert(0, ALPHABET.charAt(divmod[1].intValue()));
        }
        
        // 앞의 0들을 '1'로 변환하여 추가
        for (int i = 0; i < zeros; i++) {
            result.insert(0, '1');
        }
        
        return result.toString();
    }
    
    /**
     * Base58 문자열을 바이트 배열로 디코딩합니다.
     * 
     * @param input 디코딩할 Base58 문자열
     * @return 디코딩된 바이트 배열
     */
    public static byte[] decode(String input) {
        if (input.isEmpty()) {
            return new byte[0];
        }
        
        // 앞의 '1'들을 세어서 나중에 추가할 0의 개수를 계산
        int zeros = 0;
        while (zeros < input.length() && input.charAt(zeros) == '1') {
            zeros++;
        }
        
        // Base58 문자열을 BigInteger로 변환
        BigInteger number = BigInteger.ZERO;
        for (int i = zeros; i < input.length(); i++) {
            char c = input.charAt(i);
            int digit = ALPHABET.indexOf(c);
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid Base58 character: " + c);
            }
            number = number.multiply(BASE).add(BigInteger.valueOf(digit));
        }
        
        // BigInteger를 바이트 배열로 변환
        byte[] bytes = number.toByteArray();
        
        // 앞의 0들을 추가
        if (zeros > 0) {
            byte[] result = new byte[zeros + bytes.length];
            Arrays.fill(result, 0, zeros, (byte) 0);
            System.arraycopy(bytes, 0, result, zeros, bytes.length);
            return result;
        }
        
        return bytes;
    }
    
    /**
     * Base58 문자열이 유효한지 검증합니다.
     * 
     * @param input 검증할 Base58 문자열
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValid(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        for (char c : input.toCharArray()) {
            if (ALPHABET.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
} 