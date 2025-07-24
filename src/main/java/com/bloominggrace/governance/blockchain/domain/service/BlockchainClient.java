package com.bloominggrace.governance.blockchain.domain.service;

import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;

import java.math.BigInteger;

/**
 * 블록체인 네트워크와의 통신을 담당하는 인터페이스
 * 각 네트워크별로 구현체가 필요합니다.
 */
public interface BlockchainClient {
    
    /**
     * 블록체인 네트워크 타입을 반환합니다.
     */
    NetworkType getNetworkType();
    
    /**
     * 최신 블록 해시를 조회합니다.
     * @return 최신 블록 해시
     */
    String getLatestBlockHash();
    
    /**
     * 가스 가격을 조회합니다 (Ethereum 네트워크용).
     * @return 가스 가격 (wei 단위)
     */
    String getGasPrice();
    
    /**
     * 가스 한도를 조회합니다 (Ethereum 네트워크용).
     * @param fromAddress 발신자 주소
     * @param toAddress 수신자 주소 (선택적)
     * @param data 트랜잭션 데이터 (선택적)
     * @return 가스 한도
     */
    String estimateGas(String fromAddress, String toAddress, String data);
    
    /**
     * 계정의 nonce를 조회합니다 (Ethereum 네트워크용).
     * @param address 계정 주소
     * @return nonce 값
     */
    String getNonce(String address);
    
    /**
     * 계정 잔액을 조회합니다.
     * @param address 계정 주소
     * @return 잔액 (wei 단위)
     */
    String getBalance(String address);
    
    /**
     * 토큰 잔액을 조회합니다.
     * @param tokenAddress 토큰 컨트랙트 주소
     * @param walletAddress 지갑 주소
     * @return 토큰 잔액
     */
    String getTokenBalance(String tokenAddress, String walletAddress);
    
    /**
     * 트랜잭션을 브로드캐스트합니다.
     * @param signedTransaction 서명된 트랜잭션 (hex 문자열)
     * @return 트랜잭션 해시
     */
    String broadcastTransaction(String signedTransaction);
    
    /**
     * 트랜잭션 상태를 조회합니다.
     * @param transactionHash 트랜잭션 해시
     * @return 트랜잭션 상태 (pending, confirmed, failed 등)
     */
    String getTransactionStatus(String transactionHash);
    
    /**
     * 트랜잭션 영수증을 조회합니다.
     * @param transactionHash 트랜잭션 해시
     * @return 트랜잭션 영수증 정보
     */
    String getTransactionReceipt(String transactionHash);
    
    /**
     * 블록 정보를 조회합니다.
     * @param blockHash 블록 해시
     * @return 블록 정보
     */
    String getBlockByHash(String blockHash);
    
    /**
     * 블록 번호로 블록 정보를 조회합니다.
     * @param blockNumber 블록 번호
     * @return 블록 정보
     */
    String getBlockByNumber(String blockNumber);

    /**
     * 최신 블록 번호를 조회합니다.
     * @return 최신 블록 번호
     */
    String getLatestBlockNumber();


    BigInteger getProposalCount();
    
    /**
     * 블록의 타임스탬프를 조회합니다.
     * @param blockNumber 블록 번호 (null이면 최신 블록)
     * @return 블록 타임스탬프 (Unix timestamp)
     */
    Long getBlockTimestamp(String blockNumber);
} 