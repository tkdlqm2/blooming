package com.bloominggrace.governance.shared.domain.model;

import java.math.BigInteger;

/**
 * 이더리움 트랜잭션 바디 DTO
 */
public class EthereumTransactionBody {
    private final BigInteger chainId;        // Sepolia: 11155111
    private final BigInteger nonce;          // 트랜잭션 nonce
    private final BigInteger maxFeePerGas;   // 20 gwei
    private final BigInteger gasLimit;       // 21000
    private final String to;                 // 받는 주소
    private final String from;               // 보내는 주소
    private final BigInteger value;          // 전송할 ETH 양 (wei 단위)
    private final String data;               // 컨트랙트 호출 데이터 (선택적)

    public EthereumTransactionBody(BigInteger chainId, BigInteger nonce, BigInteger maxFeePerGas, 
                                 BigInteger gasLimit, String to, String from, BigInteger value, String data) {
        this.chainId = chainId;
        this.nonce = nonce;
        this.maxFeePerGas = maxFeePerGas;
        this.gasLimit = gasLimit;
        this.to = to;
        this.from = from;
        this.value = value;
        this.data = data;
    }

    // Getters
    public BigInteger getChainId() { return chainId; }
    public BigInteger getNonce() { return nonce; }
    public BigInteger getMaxFeePerGas() { return maxFeePerGas; }
    public BigInteger getGasLimit() { return gasLimit; }
    public String getTo() { return to; }
    public String getFrom() { return from; }
    public BigInteger getValue() { return value; }
    public String getData() { return data; }

    @Override
    public String toString() {
        return "EthereumTransactionBody{" +
                "chainId=" + chainId +
                ", nonce=" + nonce +
                ", maxFeePerGas=" + maxFeePerGas +
                ", gasLimit=" + gasLimit +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", value=" + value +
                ", data='" + data + '\'' +
                '}';
    }
} 