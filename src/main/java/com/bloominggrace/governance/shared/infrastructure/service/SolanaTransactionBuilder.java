package com.bloominggrace.governance.shared.infrastructure.service;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.shared.domain.model.SolanaTransactionBody;
import com.bloominggrace.governance.shared.domain.service.TransactionBuilder;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Solana 트랜잭션 빌더 구현체
 */
@Service
public class SolanaTransactionBuilder implements TransactionBuilder {

    private final AdminWalletService adminWalletService;
    private final BlockchainClient solanaBlockchainClient;

    // Solana 네트워크 설정
    private static final Long DEFAULT_FEE = 5000L;
    private static final String SPL_TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";

    @Autowired
    public SolanaTransactionBuilder(AdminWalletService adminWalletService, 
                                  BlockchainClient solanaBlockchainClient) {
        this.adminWalletService = adminWalletService;
        this.solanaBlockchainClient = solanaBlockchainClient;
    }

    @Override
    public Object buildTransactionBody(String fromAddress, String toAddress, BigDecimal value, NetworkType networkType) {
        if (networkType != NetworkType.SOLANA) {
            throw new IllegalArgumentException("Network type must be SOLANA for SolanaTransactionBuilder");
        }

        String recentBlockhash = solanaBlockchainClient.getLatestBlockHash();
        BigInteger amountInLamports = convertSolToLamports(value);

        return new SolanaTransactionBody(
                recentBlockhash,
                DEFAULT_FEE,
                null, // SOL 전송은 프로그램 ID가 없음
                fromAddress,
                toAddress,
                amountInLamports,
                null // SOL 전송은 데이터가 없음
        );
    }

    @Override
    public Object buildMintTransactionBody(String fromAddress, String toAddress, BigDecimal tokenAmount, 
                                         String tokenContractAddress, NetworkType networkType) {
        if (networkType != NetworkType.SOLANA) {
            throw new IllegalArgumentException("Network type must be SOLANA for SolanaTransactionBuilder");
        }

        String recentBlockhash = solanaBlockchainClient.getLatestBlockHash();
        BigInteger tokenAmountInSmallestUnit = convertTokenToSmallestUnit(tokenAmount);

        // SPL 토큰 민팅을 위한 데이터 생성
        String mintData = generateSolanaMintData(toAddress, tokenAmountInSmallestUnit);

        return new SolanaTransactionBody(
                recentBlockhash,
                DEFAULT_FEE,
                SPL_TOKEN_PROGRAM_ID,
                fromAddress,
                tokenContractAddress, // 토큰 컨트랙트 주소
                tokenAmountInSmallestUnit,
                mintData
        );
    }

    /**
     * SOL을 Lamports로 변환합니다.
     */
    private BigInteger convertSolToLamports(BigDecimal solAmount) {
        return solAmount.multiply(BigDecimal.valueOf(1_000_000_000L)).toBigInteger();
    }

    /**
     * 토큰을 가장 작은 단위로 변환합니다.
     */
    private BigInteger convertTokenToSmallestUnit(BigDecimal tokenAmount) {
        return tokenAmount.multiply(BigDecimal.valueOf(1_000_000_000L)).toBigInteger();
    }

    /**
     * Solana SPL 토큰 민팅을 위한 데이터를 생성합니다.
     */
    private String generateSolanaMintData(String toAddress, BigInteger amount) {
        // Solana SPL 토큰 민팅 명령어 데이터 생성
        // 실제 구현에서는 Solana SDK를 사용하여 더 정확한 데이터를 생성해야 합니다.
        return "mint_data_" + toAddress + "_" + amount.toString();
    }
} 