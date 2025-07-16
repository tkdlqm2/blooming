package com.bloominggrace.governance.shared.domain.model;

import lombok.Value;

import java.math.BigInteger;

/**
 * 이더리움 네트워크별 트랜잭션 데이터
 */
@Value
public class EthereumTransactionData {
    BigInteger gasPrice;      // 가스 가격 (wei)
    BigInteger gasLimit;      // 가스 한도
    BigInteger value;         // 전송할 이더 값 (wei)
    String contractAddress;   // 스마트 컨트랙트 주소 (선택적)
    
    public EthereumTransactionData(BigInteger gasPrice, BigInteger gasLimit, BigInteger value, String contractAddress) {
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.value = value;
        this.contractAddress = contractAddress;
    }
    
    public EthereumTransactionData(BigInteger gasPrice, BigInteger gasLimit, BigInteger value) {
        this(gasPrice, gasLimit, value, null);
    }
    
    public static EthereumTransactionDataBuilder builder() {
        return new EthereumTransactionDataBuilder();
    }
    
    public static class EthereumTransactionDataBuilder {
        private BigInteger gasPrice;
        private BigInteger gasLimit;
        private BigInteger value;
        private String contractAddress;
        
        public EthereumTransactionDataBuilder gasPrice(BigInteger gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }
        
        public EthereumTransactionDataBuilder gasLimit(BigInteger gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }
        
        public EthereumTransactionDataBuilder value(BigInteger value) {
            this.value = value;
            return this;
        }
        
        public EthereumTransactionDataBuilder contractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }
        
        public EthereumTransactionData build() {
            return new EthereumTransactionData(gasPrice, gasLimit, value, contractAddress);
        }
    }
} 