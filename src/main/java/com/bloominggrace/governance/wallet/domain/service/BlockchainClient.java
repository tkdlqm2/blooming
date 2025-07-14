package com.bloominggrace.governance.wallet.domain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 블록체인 클라이언트 인터페이스
 * 다양한 블록체인 네트워크와의 상호작용을 위한 공통 인터페이스
 */
public interface BlockchainClient {
    
    /**
     * 지갑 주소의 잔액을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @return 잔액
     */
    BigDecimal getBalance(String walletAddress);
    
    /**
     * 토큰 잔액을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param tokenAddress 토큰 컨트랙트 주소
     * @return 토큰 잔액
     */
    BigDecimal getTokenBalance(String walletAddress, String tokenAddress);
    
    /**
     * 네이티브 토큰을 전송합니다.
     * 
     * @param fromAddress 보내는 주소
     * @param toAddress 받는 주소
     * @param amount 전송 금액
     * @param privateKey 개인키
     * @return 트랜잭션 해시
     */
    String sendTransaction(String fromAddress, String toAddress, BigDecimal amount, String privateKey);
    
    /**
     * 토큰을 전송합니다.
     * 
     * @param fromAddress 보내는 주소
     * @param toAddress 받는 주소
     * @param tokenAddress 토큰 컨트랙트 주소
     * @param amount 전송 금액
     * @param privateKey 개인키
     * @return 트랜잭션 해시
     */
    String sendToken(String fromAddress, String toAddress, String tokenAddress, BigDecimal amount, String privateKey);
    
    /**
     * 트랜잭션 정보를 조회합니다.
     * 
     * @param transactionHash 트랜잭션 해시
     * @return 트랜잭션 정보
     */
    Optional<TransactionInfo> getTransaction(String transactionHash);
    
    /**
     * 지갑의 트랜잭션 히스토리를 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param limit 조회할 트랜잭션 수
     * @return 트랜잭션 목록
     */
    List<TransactionInfo> getTransactionHistory(String walletAddress, int limit);
    
    /**
     * 주소의 유효성을 검증합니다.
     * 
     * @param walletAddress 검증할 주소
     * @return 유효성 여부
     */
    boolean isValidAddress(String walletAddress);
    
    /**
     * 지원하는 네트워크 타입을 반환합니다.
     * 
     * @return 네트워크 타입
     */
    String getSupportedNetworkType();
    
    /**
     * 네트워크 연결 상태를 확인합니다.
     * 
     * @return 연결 상태
     */
    boolean isNetworkConnected();
    
    /**
     * 현재 블록 번호를 조회합니다.
     * 
     * @return 블록 번호
     */
    long getCurrentBlockNumber();
    
    /**
     * 가스 가격을 조회합니다.
     * 
     * @return 가스 가격
     */
    BigDecimal getGasPrice();
    
    /**
     * 토큰을 민팅합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param amount 민팅할 금액
     * @param description 설명
     * @return 트랜잭션 해시
     */
    String mintToken(String walletAddress, BigDecimal amount, String description);
    
    /**
     * 토큰을 스테이킹합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param amount 스테이킹할 금액
     * @return 트랜잭션 해시
     */
    String stakeToken(String walletAddress, BigDecimal amount);
    
    /**
     * 토큰을 언스테이킹합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param amount 언스테이킹할 금액
     * @return 트랜잭션 해시
     */
    String unstakeToken(String walletAddress, BigDecimal amount);
    
    /**
     * 토큰을 전송합니다.
     * 
     * @param fromAddress 보내는 주소
     * @param toAddress 받는 주소
     * @param amount 전송할 금액
     * @param description 설명
     * @return 트랜잭션 해시
     */
    String transferToken(String fromAddress, String toAddress, BigDecimal amount, String description);
    
    /**
     * 토큰을 소각합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param amount 소각할 금액
     * @param description 설명
     * @return 트랜잭션 해시
     */
    String burnToken(String walletAddress, BigDecimal amount, String description);
    
    /**
     * 스테이킹된 토큰 잔액을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param tokenAddress 토큰 주소
     * @return 스테이킹된 토큰 잔액
     */
    BigDecimal getStakedTokenBalance(String walletAddress, String tokenAddress);

    /**
     * 주어진 메시지에 대해 개인키로 서명합니다.
     *
     * @param message 서명할 메시지 (byte[])
     * @param privateKey 개인키 (hex string)
     * @return 서명 결과 (byte[])
     */
    byte[] sign(byte[] message, String privateKey);
    
    /**
     * 트랜잭션 정보를 담는 내부 클래스
     */
    class TransactionInfo {
        private final String transactionHash;
        private final String fromAddress;
        private final String toAddress;
        private final BigDecimal amount;
        private final String status;
        private final long blockNumber;
        private final long timestamp;
        
        public TransactionInfo(String transactionHash, String fromAddress, String toAddress, 
                             BigDecimal amount, String status, long blockNumber, long timestamp) {
            this.transactionHash = transactionHash;
            this.fromAddress = fromAddress;
            this.toAddress = toAddress;
            this.amount = amount;
            this.status = status;
            this.blockNumber = blockNumber;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getTransactionHash() { return transactionHash; }
        public String getFromAddress() { return fromAddress; }
        public String getToAddress() { return toAddress; }
        public BigDecimal getAmount() { return amount; }
        public String getStatus() { return status; }
        public long getBlockNumber() { return blockNumber; }
        public long getTimestamp() { return timestamp; }
    }
} 