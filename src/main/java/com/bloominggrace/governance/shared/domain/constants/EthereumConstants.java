package com.bloominggrace.governance.shared.domain.constants;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Ethereum 관련 상수들을 관리하는 클래스
 * RPC 메서드명, 컨트랙트 주소, 가스 설정 등을 포함
 */
public final class EthereumConstants {
    
    private EthereumConstants() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
    
    /**
     * Ethereum JSON-RPC 메서드명
     */
    public static final class RpcMethods {
        private RpcMethods() {}
        
        // 블록 관련
        public static final String GET_BLOCK_BY_NUMBER = "eth_getBlockByNumber";
        public static final String GET_BLOCK_BY_HASH = "eth_getBlockByHash";
        public static final String GET_BLOCK_NUMBER = "eth_blockNumber";
        
        // 트랜잭션 관련
        public static final String GET_TRANSACTION_COUNT = "eth_getTransactionCount";
        public static final String GET_TRANSACTION_RECEIPT = "eth_getTransactionReceipt";
        public static final String SEND_RAW_TRANSACTION = "eth_sendRawTransaction";
        
        // 가스 관련
        public static final String GET_GAS_PRICE = "eth_gasPrice";
        public static final String ESTIMATE_GAS = "eth_estimateGas";
        
        // 잔액 관련
        public static final String GET_BALANCE = "eth_getBalance";
        
        // 컨트랙트 호출
        public static final String CALL = "eth_call";
    }
    
    /**
     * RPC 파라미터 값들
     */
    public static final class RpcParams {
        private RpcParams() {}
        
        // 블록 태그
        public static final String LATEST = "latest";
        public static final String PENDING = "pending";
        public static final String EARLIEST = "earliest";
        
        // 블록 번호
        public static final String BLOCK_LATEST = "latest";
        public static final String BLOCK_PENDING = "pending";
        
        // 블록 정보 포함 여부
        public static final boolean INCLUDE_TRANSACTIONS = true;
        public static final boolean EXCLUDE_TRANSACTIONS = false;
    }
    
    /**
     * 컨트랙트 주소 (실제 배포된 주소)
     */
    public static final class Contracts {
        private Contracts() {}
        
        public static final String GOVERNANCE_CONTRACT_ADDRESS = "0x4E5EE91796498E843a7Ae952BC86B1a1547C60bB";
        public static final String ERC20_CONTRACT_ADDRESS = "0xd2Dfe16C1F31493530D297D58E32c337fd27615D";
    }
    
    /**
     * 토큰 관련 상수
     */
    public static final class Token {
        private Token() {}
        
        public static final String ERC20_SYMBOL = "ETH";
        public static final int ERC20_DECIMALS = 18;
        public static final String ERC20_NAME = "Ethereum Token";
        
        // ERC-20 함수 시그니처
        public static final String TRANSFER_METHOD = "transfer(address,uint256)";
        public static final String APPROVE_METHOD = "approve(address,uint256)";
        public static final String BALANCE_OF_METHOD = "balanceOf(address)";
        
        // 함수 선택자 (Method ID)
        public static final String TRANSFER_SELECTOR = "0xa9059cbb";
        public static final String APPROVE_SELECTOR = "0x095ea7b3";
        public static final String BALANCE_OF_SELECTOR = "0x70a08231";
    }
    
    /**
     * 가스 관련 상수 (최적화된 값)
     */
    public static final class Gas {
        private Gas() {}
        
        // 기본 가스 설정
        public static final BigInteger GAS_PRICE = BigInteger.valueOf(10_000_000_000L); // 10 Gwei
        public static final BigInteger GAS_LIMIT = BigInteger.valueOf(150_000L);
        
        // 트랜잭션별 가스 한도
        public static final BigInteger TRANSFER_DELEGATE_GAS_LIMIT = BigInteger.valueOf(300_000L);
        
        // 거버넌스 트랜잭션
        public static final BigInteger PROPOSAL_CREATION_GAS_LIMIT = BigInteger.valueOf(800_000L);
        public static final BigInteger VOTE_GAS_LIMIT = BigInteger.valueOf(180_000L);
    }
    
    /**
     * 네트워크 관련 상수
     */
    public static final class Network {
        private Network() {}
        
        public static final String NETWORK_NAME = "Sepolia";
        public static final long CHAIN_ID = 11155111L;
        
        // 블록 생성 시간 (초)
        public static final long BLOCK_TIME_SECONDS = 12L;
        
        // 확인 블록 수
        public static final int CONFIRMATION_BLOCKS = 12;
    }
    
    /**
     * 관리자 지갑 정보
     */
    public static final class Admin {
        private Admin() {}
        
        public static final String WALLET_ADDRESS = "0x55D5c49e36f8A89111687C9DC8355121068f0cD8";
    }
    
    /**
     * 수수료 설정
     */
    public static final class Fees {
        private Fees() {}
        
        public static final BigDecimal TRANSACTION_FEE = BigDecimal.valueOf(0.001); // ETH
        public static final BigDecimal EXCHANGE_FEE_PERCENTAGE = BigDecimal.valueOf(0.5); // 0.5%
        public static final BigDecimal MINIMUM_TRANSACTION_FEE = BigDecimal.valueOf(0.0001); // ETH
    }
    
    /**
     * 거버넌스 관련 상수
     */
    public static final class Governance {
        private Governance() {}
        
        // 투표 기간 (블록 단위)
        public static final BigInteger DEFAULT_VOTING_PERIOD_BLOCKS = BigInteger.valueOf(40320); // 약 1주일 (40320 블록)
        public static final BigInteger MIN_VOTING_PERIOD_BLOCKS = BigInteger.valueOf(5760); // 약 1일 (5760 블록)
        public static final BigInteger MAX_VOTING_PERIOD_BLOCKS = BigInteger.valueOf(120960); // 약 3주일 (120960 블록)
        
        // 제안 수수료
        public static final BigDecimal PROPOSAL_FEE = BigDecimal.valueOf(0.01); // ETH
        
        // 쿼럼 설정
        public static final BigDecimal DEFAULT_QUORUM_PERCENTAGE = BigDecimal.valueOf(10); // 10%
        public static final BigDecimal MIN_QUORUM_PERCENTAGE = BigDecimal.valueOf(5); // 5%
        public static final BigDecimal MAX_QUORUM_PERCENTAGE = BigDecimal.valueOf(50); // 50%
    }
    
    /**
     * 유틸리티 상수
     */
    public static final class Utils {
        private Utils() {}
        
        // Wei 변환
        public static final BigInteger WEI_PER_ETH = BigInteger.valueOf(1_000_000_000_000_000_000L); // 10^18
        public static final BigDecimal WEI_PER_ETH_DECIMAL = BigDecimal.valueOf(1_000_000_000_000_000_000L);
        
        // 주소 형식
        public static final String ADDRESS_PREFIX = "0x";
        public static final int ADDRESS_LENGTH = 42; // 0x + 40자리 16진수
        
        // 해시 형식
        public static final int HASH_LENGTH = 66; // 0x + 64자리 16진수
        
        // 최대 값들
        public static final BigInteger MAX_UINT256 = new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
        public static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(999999999.999999);
        public static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(0.000001);
    }
} 