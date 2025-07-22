package com.bloominggrace.governance.shared.domain.model;

import lombok.Getter;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 블록체인별 메타데이터를 관리하는 클래스
 * 패키지 전역에서 static으로 접근 가능
 */
public class BlockchainMetadata {
    
    // ===== 이더리움 메타데이터 =====
    public static class Ethereum {
        // ERC20 토큰 컨트랙트 주소
        public static final String ERC20_CONTRACT_ADDRESS = "0xeafF00556BC06464511319dAb26D6CAC148b89d0";
        public static final String ERC20_SYMBOL = "ETH";
        public static final int ERC20_DECIMALS = 18;
        
        // 관리자 지갑 정보
        public static final String ADMIN_WALLET_ADDRESS = "0x55D5c49e36f8A89111687C9DC8355121068f0cD8";

        // 기본 가스 설정 (현재 네트워크 최적화)
        public static final BigInteger GAS_PRICE = BigInteger.valueOf(10_000_000_000L);  // 20→15 Gwei 절약
        public static final BigInteger GAS_LIMIT = BigInteger.valueOf(150_000L);         // 200k→150k 최적화

        // ERC-20 토큰 전송 (안전성 강화)
        public static final BigInteger ERC20_TRANSFER_GAS_LIMIT = BigInteger.valueOf(120_000L); // 100k→120k

        // 거버넌스 트랜잭션 (실제 사용량 반영)
        public static final BigInteger PROPOSAL_CREATION_GAS_LIMIT = BigInteger.valueOf(800_000L);
        public static final BigInteger VOTE_GAS_LIMIT = BigInteger.valueOf(180_000L);              // 200k→180k 최적화
        
        // 수수료 설정
        public static final BigDecimal TRANSACTION_FEE = BigDecimal.valueOf(0.001); // ETH
        public static final BigDecimal EXCHANGE_FEE_PERCENTAGE = BigDecimal.valueOf(0.5); // 0.5%
        
        // 네트워크 설정
        public static final String NETWORK_NAME = "Sepolia";
        public static final long CHAIN_ID = 11155111L;

        // 컨트랙트 설정
        public static final String GOVERNANCE_CONTRACT_ADDRESS = "0xA8B86ecf2d57F7b53312Ae5ecb3215C698550Afd";
    }
    
    // ===== 솔라나 메타데이터 =====
    public static class Solana {
        // SPL 토큰 정보
        public static final String SPL_TOKEN_MINT_ADDRESS = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"; // USDC
        public static final String SPL_TOKEN_SYMBOL = "SOL";
        public static final int SPL_TOKEN_DECIMALS = 9;
        
        // 관리자 지갑 정보
        public static final String ADMIN_WALLET_ADDRESS = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM";

        // 수수료 설정
        public static final BigDecimal TRANSACTION_FEE = BigDecimal.valueOf(0.000005); // SOL
        public static final BigDecimal EXCHANGE_FEE_PERCENTAGE = BigDecimal.valueOf(0.3); // 0.3%
        
        // 네트워크 설정
        public static final String NETWORK_NAME = "Devnet";
        public static final String EXPLORER_URL = "https://explorer.solana.com";
        public static final String RPC_ENDPOINT = "https://api.devnet.solana.com";
        
        // 프로그램 ID
        public static final String GOVERNANCE_PROGRAM_ID = "GovER5Lthms3bLBqWub97yVrMmEogzX7xNjdXpPPCVZw";
        public static final String STAKING_PROGRAM_ID = "Stake11111111111111111111111111111111111111112";
        public static final String TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
    }

} 