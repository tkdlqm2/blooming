package com.bloominggrace.governance.shared.blockchain.domain.model.ethereum;

import com.bloominggrace.governance.shared.blockchain.util.HashUtils;
import com.bloominggrace.governance.shared.blockchain.util.HexUtils;

import java.math.BigInteger;
public class EthereumTransactionData {
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private BigInteger value;
    private String toAddress;
    private String contractAddress;
    private String data;
    private BigInteger nonce;

    // Default constructor
    public EthereumTransactionData() {}

    // All-args constructor
    public EthereumTransactionData(BigInteger gasPrice, BigInteger gasLimit, BigInteger value, 
                                 String toAddress, String contractAddress, String data, BigInteger nonce) {
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.value = value;
        this.toAddress = toAddress;
        this.contractAddress = contractAddress;
        this.data = data;
        this.nonce = nonce;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BigInteger gasPrice;
        private BigInteger gasLimit;
        private BigInteger value;
        private String toAddress;
        private String contractAddress;
        private String data;
        private BigInteger nonce;

        public Builder gasPrice(BigInteger gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public Builder gasLimit(BigInteger gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public Builder value(BigInteger value) {
            this.value = value;
            return this;
        }

        public Builder toAddress(String toAddress) {
            this.toAddress = toAddress;
            return this;
        }

        public Builder contractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public Builder nonce(BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public EthereumTransactionData build() {
            return new EthereumTransactionData(gasPrice, gasLimit, value, toAddress, contractAddress, data, nonce);
        }
    }

    // Getters and Setters
    public BigInteger getGasPrice() { return gasPrice; }
    public void setGasPrice(BigInteger gasPrice) { this.gasPrice = gasPrice; }

    public BigInteger getGasLimit() { return gasLimit; }
    public void setGasLimit(BigInteger gasLimit) { this.gasLimit = gasLimit; }

    public BigInteger getValue() { return value; }
    public void setValue(BigInteger value) { this.value = value; }

    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public BigInteger getNonce() { return nonce; }
    public void setNonce(BigInteger nonce) { this.nonce = nonce; }

    /**
     * ERC-20 토큰 전송을 위한 데이터 생성
     */
    public static String createERC20TransferData(String toAddress, BigInteger amount) {
        // ERC-20 transfer 함수 시그니처: transfer(address,uint256)
        String methodSignature = "transfer(address,uint256)";
        String methodId = HexUtils.bytesToHex(HashUtils.keccak256(methodSignature.getBytes())).substring(0, 8);
        
        // 파라미터 인코딩
        String encodedToAddress = padLeft(toAddress.substring(2), 64); // 0x 제거 후 32바이트 패딩
        String encodedAmount = padLeft(amount.toString(16), 64); // 32바이트 패딩
        
        return "0x" + methodId + encodedToAddress + encodedAmount;
    }

    /**
     * ERC-20 토큰 잔액 조회를 위한 데이터 생성
     */
    public static String createERC20BalanceOfData(String walletAddress) {
        // ERC-20 balanceOf 함수 시그니처: balanceOf(address)
        String methodSignature = "balanceOf(address)";
        String methodId = HexUtils.bytesToHex(HashUtils.keccak256(methodSignature.getBytes())).substring(0, 8);
        
        // 파라미터 인코딩
        String encodedAddress = padLeft(walletAddress.substring(2), 64); // 0x 제거 후 32바이트 패딩
        
        return "0x" + methodId + encodedAddress;
    }

    private static String padLeft(String value, int length) {
        return String.format("%" + length + "s", value).replace(' ', '0');
    }
} 