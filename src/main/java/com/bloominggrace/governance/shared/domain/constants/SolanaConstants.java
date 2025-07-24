package com.bloominggrace.governance.shared.domain.constants;

import java.math.BigDecimal;

/**
 * Solana 관련 상수들을 관리하는 클래스
 * RPC 메서드명, 프로그램 ID, 토큰 정보 등을 포함
 */
public final class SolanaConstants {
    
    private SolanaConstants() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
    
    /**
     * Solana JSON-RPC 메서드명
     */
    public static final class RpcMethods {
        private RpcMethods() {}
        
        // 계정 관련
        public static final String GET_ACCOUNT_INFO = "getAccountInfo";
        public static final String GET_BALANCE = "getBalance";
        public static final String GET_TOKEN_ACCOUNTS_BY_OWNER = "getTokenAccountsByOwner";
        public static final String GET_TOKEN_ACCOUNT_BALANCE = "getTokenAccountBalance";
        
        // 블록 관련
        public static final String GET_BLOCK = "getBlock";
        public static final String GET_BLOCK_HEIGHT = "getBlockHeight";
        public static final String GET_BLOCK_TIME = "getBlockTime";
        public static final String GET_LATEST_BLOCKHASH = "getLatestBlockhash";
        
        // 트랜잭션 관련
        public static final String SEND_TRANSACTION = "sendTransaction";
        public static final String GET_TRANSACTION = "getTransaction";
        public static final String GET_SIGNATURE_STATUSES = "getSignatureStatuses";
        public static final String CONFIRM_TRANSACTION = "confirmTransaction";
        
        // 프로그램 관련
        public static final String GET_PROGRAM_ACCOUNTS = "getProgramAccounts";
        public static final String CALL_PROGRAM = "callProgram";
        
        // 네트워크 관련
        public static final String GET_VERSION = "getVersion";
        public static final String GET_SLOT = "getSlot";
        public static final String GET_SLOT_LEADER = "getSlotLeader";
    }
    
    /**
     * RPC 파라미터 값들
     */
    public static final class RpcParams {
        private RpcParams() {}
        
        // 인코딩
        public static final String ENCODING_BASE58 = "base58";
        public static final String ENCODING_BASE64 = "base64";
        public static final String ENCODING_JSON = "jsonParsed";
        
        // 커밋먼트
        public static final String COMMITMENT_CONFIRMED = "confirmed";
        public static final String COMMITMENT_FINALIZED = "finalized";
        public static final String COMMITMENT_PROCESSED = "processed";
        
        // 트랜잭션 타입
        public static final String TRANSACTION_TYPE_SIGNED = "signed";
        public static final String TRANSACTION_TYPE_UNSIGNED = "unsigned";
    }
    
    /**
     * 프로그램 ID (실제 배포된 주소)
     */
    public static final class Programs {
        private Programs() {}
        
        // 거버넌스 프로그램
        public static final String GOVERNANCE_PROGRAM_ID = "GovER5Lthms3bLBqWub97yVrMmEogzX7xNjdXpPPCVZw";
        
        // 스테이킹 프로그램
        public static final String STAKING_PROGRAM_ID = "Stake11111111111111111111111111111111111111112";
        
        // 토큰 프로그램
        public static final String TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
        
        // 시스템 프로그램
        public static final String SYSTEM_PROGRAM_ID = "11111111111111111111111111111111";
        
        // Associated Token Account 프로그램
        public static final String ASSOCIATED_TOKEN_PROGRAM_ID = "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL";
    }
    
    /**
     * 토큰 관련 상수
     */
    public static final class Token {
        private Token() {}
        
        // SPL 토큰 정보
        public static final String SPL_TOKEN_MINT_ADDRESS = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"; // USDC
        public static final String SPL_TOKEN_SYMBOL = "SOL";
        public static final int SPL_TOKEN_DECIMALS = 9;
        public static final String SPL_TOKEN_NAME = "Solana Token";
        
        // 토큰 계정 타입
        public static final String TOKEN_ACCOUNT_TYPE_MINT = "mint";
        public static final String TOKEN_ACCOUNT_TYPE_ACCOUNT = "account";
        
        // 토큰 명령어
        public static final String TOKEN_INSTRUCTION_INITIALIZE_MINT = "initializeMint";
        public static final String TOKEN_INSTRUCTION_MINT_TO = "mintTo";
        public static final String TOKEN_INSTRUCTION_TRANSFER = "transfer";
        public static final String TOKEN_INSTRUCTION_APPROVE = "approve";
        public static final String TOKEN_INSTRUCTION_BURN = "burn";
    }
    
    /**
     * 네트워크 관련 상수
     */
    public static final class Network {
        private Network() {}
        
        public static final String NETWORK_NAME = "Devnet";
        public static final String MAINNET_URL = "https://api.mainnet-beta.solana.com";
        public static final String DEVNET_URL = "https://api.devnet.solana.com";
        public static final String TESTNET_URL = "https://api.testnet.solana.com";
        public static final String LOCALNET_URL = "http://localhost:8899";
        
        // 블록 생성 시간 (밀리초)
        public static final long BLOCK_TIME_MILLISECONDS = 400L; // 400ms
        
        // 확인 블록 수
        public static final int CONFIRMATION_BLOCKS = 32;
        
        // 슬롯 관련
        public static final long SLOTS_PER_EPOCH = 432000L;
        public static final long SLOTS_PER_SECOND = 2L; // 정수로 변경
    }
    
    /**
     * 관리자 지갑 정보
     */
    public static final class Admin {
        private Admin() {}
        
        public static final String WALLET_ADDRESS = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM";
    }
    
    /**
     * 수수료 설정
     */
    public static final class Fees {
        private Fees() {}
        
        public static final BigDecimal TRANSACTION_FEE = BigDecimal.valueOf(0.000005); // SOL
        public static final BigDecimal EXCHANGE_FEE_PERCENTAGE = BigDecimal.valueOf(0.3); // 0.3%
        public static final BigDecimal MINIMUM_TRANSACTION_FEE = BigDecimal.valueOf(0.000001); // SOL
        
        // 램트 수수료 (bytes 단위)
        public static final long RENT_PER_BYTE_YEAR = 3480L; // lamports per byte-year
        public static final long MINIMUM_RENT_EXEMPTION = 2039280L; // lamports
    }
    
    /**
     * 거버넌스 관련 상수
     */
    public static final class Governance {
        private Governance() {}
        
        // 투표 기간 (슬롯 단위)
        public static final long DEFAULT_VOTING_PERIOD_SLOTS = 432000L; // 약 1주일
        public static final long MIN_VOTING_PERIOD_SLOTS = 86400L; // 약 1일
        public static final long MAX_VOTING_PERIOD_SLOTS = 1296000L; // 약 3주일
        
        // 제안 수수료
        public static final BigDecimal PROPOSAL_FEE = BigDecimal.valueOf(0.01); // SOL
        
        // 쿼럼 설정
        public static final BigDecimal DEFAULT_QUORUM_PERCENTAGE = BigDecimal.valueOf(10); // 10%
        public static final BigDecimal MIN_QUORUM_PERCENTAGE = BigDecimal.valueOf(5); // 5%
        public static final BigDecimal MAX_QUORUM_PERCENTAGE = BigDecimal.valueOf(50); // 50%
        
        // 거버넌스 토큰
        public static final String GOVERNANCE_TOKEN_MINT = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v";
        public static final String GOVERNANCE_TOKEN_SYMBOL = "GOV";
        public static final int GOVERNANCE_TOKEN_DECIMALS = 9;
    }
    
    /**
     * 유틸리티 상수
     */
    public static final class Utils {
        private Utils() {}
        
        // Lamports 변환
        public static final long LAMPORTS_PER_SOL = 1_000_000_000L; // 10^9
        public static final BigDecimal LAMPORTS_PER_SOL_DECIMAL = BigDecimal.valueOf(1_000_000_000L);
        
        // 주소 형식
        public static final int ADDRESS_LENGTH = 44; // Base58 인코딩된 32바이트 공개키
        
        // 해시 형식
        public static final int SIGNATURE_LENGTH = 88; // Base58 인코딩된 64바이트 서명
        
        // 최대 값들
        public static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(999999999.999999);
        public static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(0.000001);
        
        // 트랜잭션 크기 제한
        public static final int MAX_TRANSACTION_SIZE = 1232; // bytes
        public static final int MAX_SIGNATURES = 16;
    }
    
    /**
     * 익스플로러 URL
     */
    public static final class Explorer {
        private Explorer() {}
        
        public static final String MAINNET_URL = "https://explorer.solana.com";
        public static final String DEVNET_URL = "https://explorer.solana.com/?cluster=devnet";
        public static final String TESTNET_URL = "https://explorer.solana.com/?cluster=testnet";
    }
} 