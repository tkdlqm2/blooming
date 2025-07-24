package com.bloominggrace.governance.shared.domain.model;

import java.math.BigInteger;

/**
 * Solana 트랜잭션 바디 DTO
 */
public class SolanaTransactionBody {
    private final String recentBlockhash;    // 최신 블록 해시
    private final Long fee;                  // 트랜잭션 수수료
    private final String programId;          // 프로그램 ID (SPL 토큰 프로그램 등)
    private final String fromAddress;        // 보내는 주소
    private final String toAddress;          // 받는 주소
    private final BigInteger amount;         // 전송할 토큰 양
    private final String data;               // 트랜잭션 데이터

    public SolanaTransactionBody(String recentBlockhash, Long fee, String programId, 
                                String fromAddress, String toAddress, BigInteger amount, String data) {
        this.recentBlockhash = recentBlockhash;
        this.fee = fee;
        this.programId = programId;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.data = data;
    }

    // Getters
    public String getRecentBlockhash() { return recentBlockhash; }
    public Long getFee() { return fee; }
    public String getProgramId() { return programId; }
    public String getFromAddress() { return fromAddress; }
    public String getToAddress() { return toAddress; }
    public BigInteger getAmount() { return amount; }
    public String getData() { return data; }

    @Override
    public String toString() {
        return "SolanaTransactionBody{" +
                "recentBlockhash='" + recentBlockhash + '\'' +
                ", fee=" + fee +
                ", programId='" + programId + '\'' +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", amount=" + amount +
                ", data='" + data + '\'' +
                '}';
    }
} 