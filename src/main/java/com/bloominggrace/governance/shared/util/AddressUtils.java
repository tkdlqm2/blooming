package com.bloominggrace.governance.shared.util;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;

/**
 * 블록체인 주소 관련 유틸리티 클래스
 * 주소 검증, 생성, 변환 기능을 제공합니다.
 */
public final class AddressUtils {
    
    private AddressUtils() {
        // 유틸리티 클래스는 인스턴스화 불가
    }
    
    /**
     * 주소 형식으로 네트워크 타입 추정
     * 
     * @param address 주소
     * @return 추정된 네트워크 타입
     * @throws IllegalArgumentException 알 수 없는 주소 형식인 경우
     */
    public static NetworkType guessNetworkType(String address) {
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        // Ethereum: 0x prefix and 42 chars
        if (address.startsWith("0x") && address.length() == 42) {
            return NetworkType.ETHEREUM;
        }
        // Solana: 32-44 chars, base58
        if (address.length() >= 32 && address.length() <= 44 && address.matches("^[1-9A-HJ-NP-Za-km-z]+$")) {
            return NetworkType.SOLANA;
        }
        throw new IllegalArgumentException("Unknown address format: " + address);
    }
    
    /**
     * Ethereum 주소를 체크섬 형식으로 변환
     * 
     * @param address 원본 Ethereum 주소
     * @return 체크섬이 적용된 Ethereum 주소
     */
    public static String toEthereumChecksumAddress(String address) {
        if (address == null || !address.startsWith("0x") || address.length() != 42) {
            throw new IllegalArgumentException("Invalid Ethereum address: " + address);
        }
        String hexPart = address.substring(2).toLowerCase();
        byte[] hash = HashUtils.keccak256(hexPart);
        StringBuilder checksumAddress = new StringBuilder("0x");
        for (int i = 0; i < hexPart.length(); i++) {
            char c = hexPart.charAt(i);
            int hashByte = hash[i / 2] & 0xFF;
            int nibble = (i % 2 == 0) ? (hashByte >> 4) : (hashByte & 0x0F);
            if (nibble >= 8) {
                checksumAddress.append(Character.toUpperCase(c));
            } else {
                checksumAddress.append(c);
            }
        }
        return checksumAddress.toString();
    }
    
    /**
     * 주소를 정규화 (소문자로 변환, 0x 접두사 유지)
     * 
     * @param address 정규화할 주소
     * @return 정규화된 주소
     */
    public static String normalizeAddress(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }
        
        if (address.startsWith("0x")) {
            return "0x" + address.substring(2).toLowerCase();
        }
        
        return address.toLowerCase();
    }
    
    /**
     * 주소를 표준 형식으로 변환 (Ethereum의 경우 체크섬 적용)
     * 
     * @param address 변환할 주소
     * @param networkType 네트워크 타입
     * @return 표준 형식의 주소
     */
    public static String toStandardAddress(String address, NetworkType networkType) {
        return switch (networkType) {
            case ETHEREUM -> toEthereumChecksumAddress(address);
            case SOLANA -> address; // Solana는 Base58 형식 유지
        };
    }
    
    /**
     * 주소 길이 검증
     * 
     * @param address 검증할 주소
     * @param networkType 네트워크 타입
     * @return 길이가 올바른 경우 true
     */
    public static boolean hasValidLength(String address, NetworkType networkType) {
        if (address == null) {
            return false;
        }
        
        return switch (networkType) {
            case ETHEREUM -> address.length() == 42; // 0x + 40자리 hex
            case SOLANA -> address.length() >= 32 && address.length() <= 44; // Base58
        };
    }
    
    /**
     * 주소 접두사 검증
     * 
     * @param address 검증할 주소
     * @param networkType 네트워크 타입
     * @return 접두사가 올바른 경우 true
     */
    public static boolean hasValidPrefix(String address, NetworkType networkType) {
        if (address == null) {
            return false;
        }
        
        return switch (networkType) {
            case ETHEREUM -> address.startsWith("0x");
            case SOLANA -> !address.startsWith("0x"); // Solana는 0x 접두사 없음
        };
    }
    
    /**
     * 주소에서 0x 접두사 제거
     * 
     * @param address 주소
     * @return 0x 접두사가 제거된 주소
     */
    public static String removePrefix(String address) {
        if (address != null && address.startsWith("0x")) {
            return address.substring(2);
        }
        return address;
    }
    
    /**
     * 주소에 0x 접두사 추가
     * 
     * @param address 주소
     * @return 0x 접두사가 추가된 주소
     */
    public static String addPrefix(String address) {
        if (address != null && !address.startsWith("0x")) {
            return "0x" + address;
        }
        return address;
    }
    
    /**
     * 주소의 첫 번째 문자를 대문자로 변환 (Ethereum 체크섬 스타일)
     * 
     * @param address 주소
     * @return 첫 번째 문자가 대문자인 주소
     */
    public static String capitalizeFirst(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }
        
        if (address.startsWith("0x")) {
            return "0x" + Character.toUpperCase(address.charAt(2)) + address.substring(3);
        }
        
        return Character.toUpperCase(address.charAt(0)) + address.substring(1);
    }
} 