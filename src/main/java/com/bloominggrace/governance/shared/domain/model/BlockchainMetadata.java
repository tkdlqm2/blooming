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
        public static final String ERC20_CONTRACT_ADDRESS = "0x1234567890123456789012345678901234567890";
        public static final String ERC20_SYMBOL = "ETH";
        public static final int ERC20_DECIMALS = 18;
        
        // 관리자 지갑 정보
        public static final String ADMIN_WALLET_ADDRESS = "0xabcdef1234567890abcdef1234567890abcdef12";

        // 가스 설정
        public static final BigInteger GAS_LIMIT = BigInteger.valueOf(31000L);
        public static final BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L); // 20 Gwei

        
        // ERC-20 토큰 전송 가스 한도
        public static final BigInteger ERC20_TRANSFER_GAS_LIMIT = BigInteger.valueOf(100000L);
        
        // 거버넌스 트랜잭션 가스 한도
        public static final BigInteger PROPOSAL_CREATION_GAS_LIMIT = BigInteger.valueOf(500000L);
        public static final BigInteger VOTE_GAS_LIMIT = BigInteger.valueOf(200000L);
        
        // 수수료 설정
        public static final BigDecimal TRANSACTION_FEE = BigDecimal.valueOf(0.001); // ETH
        public static final BigDecimal EXCHANGE_FEE_PERCENTAGE = BigDecimal.valueOf(0.5); // 0.5%
        
        // 네트워크 설정
        public static final String NETWORK_NAME = "Sepolia";
        public static final long CHAIN_ID = 11155111L;
        public static final String EXPLORER_URL = "https://sepolia.etherscan.io";
        
        // 컨트랙트 설정
        public static final String GOVERNANCE_CONTRACT_ADDRESS = "0x9876543210987654321098765432109876543210";
        public static final String STAKING_CONTRACT_ADDRESS = "0xfedcba0987654321098765432109876543210fed";
        
        // 토큰 설정
        public static final String NATIVE_TOKEN_SYMBOL = "ETH";
        public static final int NATIVE_TOKEN_DECIMALS = 18;
        public static final BigDecimal MIN_TRANSACTION_AMOUNT = BigDecimal.valueOf(0.0001);
        public static final BigDecimal MAX_TRANSACTION_AMOUNT = BigDecimal.valueOf(1000.0);
    }
    
    // ===== 솔라나 메타데이터 =====
    public static class Solana {
        // SPL 토큰 정보
        public static final String SPL_TOKEN_MINT_ADDRESS = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"; // USDC
        public static final String SPL_TOKEN_SYMBOL = "SOL";
        public static final int SPL_TOKEN_DECIMALS = 9;
        
        // 관리자 지갑 정보
        public static final String ADMIN_WALLET_ADDRESS = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM";
        public static final String ADMIN_PRIVATE_KEY = "4NwwJRqKdwmGzQPYx7Srir3gFKok4nvcRpkYAoPYWSJbHsaHL1t7o2Bo3EjNYb1WmUw5HqTrn3T1c6H7C5T5J9K8";
        
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
        
        // 토큰 설정
        public static final String NATIVE_TOKEN_SYMBOL = "SOL";
        public static final int NATIVE_TOKEN_DECIMALS = 9;
        public static final BigDecimal MIN_TRANSACTION_AMOUNT = BigDecimal.valueOf(0.000001);
        public static final BigDecimal MAX_TRANSACTION_AMOUNT = BigDecimal.valueOf(10000.0);
        
        // 트랜잭션 설정
        public static final long DEFAULT_COMMITMENT = 1L; // confirmed
        public static final int MAX_RETRIES = 3;
        public static final long RETRY_DELAY_MS = 1000L;
    }

} 