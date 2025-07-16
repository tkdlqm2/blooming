package com.bloominggrace.governance.token.application.dto;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;

/**
 * 토큰 계정 생성 요청 DTO
 */
public class CreateTokenAccountRequest {
    private String walletAddress;
    private String userId;
    private NetworkType network;
    private String contract;
    private String symbol;

    public CreateTokenAccountRequest() {}

    public CreateTokenAccountRequest(String walletAddress, String userId, NetworkType network, String contract, String symbol) {
        this.walletAddress = walletAddress;
        this.userId = userId;
        this.network = network;
        this.contract = contract;
        this.symbol = symbol;
    }

    // Getters and Setters
    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public NetworkType getNetwork() {
        return network;
    }

    public void setNetwork(NetworkType network) {
        this.network = network;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
} 