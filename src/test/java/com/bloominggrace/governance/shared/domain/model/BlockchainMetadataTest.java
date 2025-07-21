package com.bloominggrace.governance.shared.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * BlockchainMetadata 클래스 사용법 테스트
 */
@DisplayName("BlockchainMetadata 테스트")
class BlockchainMetadataTest {

    @Test
    @DisplayName("이더리움 메타데이터 접근 테스트")
    void testEthereumMetadata() {
        // ERC20 토큰 정보
        assertEquals("0x1234567890123456789012345678901234567890", BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS);
        assertEquals("ETH", BlockchainMetadata.Ethereum.ERC20_SYMBOL);
        assertEquals(18, BlockchainMetadata.Ethereum.ERC20_DECIMALS);
        
        // 관리자 지갑 정보
        assertEquals("0xabcdef1234567890abcdef1234567890abcdef12", BlockchainMetadata.Ethereum.ADMIN_WALLET_ADDRESS);
        
        // 가스 설정
        assertEquals(BigInteger.valueOf(21000L), BlockchainMetadata.Ethereum.GAS_LIMIT);
        assertEquals(BigInteger.valueOf(20000000000L), BlockchainMetadata.Ethereum.GAS_PRICE); // 20 Gwei
        
        // 수수료 설정
        assertEquals(BigDecimal.valueOf(0.001), BlockchainMetadata.Ethereum.TRANSACTION_FEE);
        assertEquals(BigDecimal.valueOf(0.5), BlockchainMetadata.Ethereum.EXCHANGE_FEE_PERCENTAGE);
        
        // 네트워크 설정
        assertEquals("Sepolia", BlockchainMetadata.Ethereum.NETWORK_NAME);
        assertEquals(11155111L, BlockchainMetadata.Ethereum.CHAIN_ID);
        
        // 컨트랙트 설정
        assertEquals("0x9876543210987654321098765432109876543210", BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS);
        assertEquals("0xfedcba0987654321098765432109876543210fed", BlockchainMetadata.Ethereum.STAKING_CONTRACT_ADDRESS);
    }

    @Test
    @DisplayName("솔라나 메타데이터 접근 테스트")
    void testSolanaMetadata() {
        // SPL 토큰 정보
        assertEquals("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", BlockchainMetadata.Solana.SPL_TOKEN_MINT_ADDRESS);
        assertEquals("SOL", BlockchainMetadata.Solana.SPL_TOKEN_SYMBOL);
        assertEquals(9, BlockchainMetadata.Solana.SPL_TOKEN_DECIMALS);
        
        // 관리자 지갑 정보
        assertEquals("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM", BlockchainMetadata.Solana.ADMIN_WALLET_ADDRESS);
        
        // 수수료 설정
        assertEquals(BigDecimal.valueOf(0.000005), BlockchainMetadata.Solana.TRANSACTION_FEE);
        assertEquals(BigDecimal.valueOf(0.3), BlockchainMetadata.Solana.EXCHANGE_FEE_PERCENTAGE);
        
        // 네트워크 설정
        assertEquals("Devnet", BlockchainMetadata.Solana.NETWORK_NAME);
        assertEquals("https://api.devnet.solana.com", BlockchainMetadata.Solana.RPC_ENDPOINT);
        
        // 프로그램 ID
        assertEquals("GovER5Lthms3bLBqWub97yVrMmEogzX7xNjdXpPPCVZw", BlockchainMetadata.Solana.GOVERNANCE_PROGRAM_ID);
        assertEquals("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA", BlockchainMetadata.Solana.TOKEN_PROGRAM_ID);
    }

    @Test
    @DisplayName("공통 설정 접근 테스트")
    void testCommonMetadata() {
        // 타임아웃 설정
        assertEquals(30, BlockchainMetadata.Common.REQUEST_TIMEOUT_SECONDS);
        assertEquals(10, BlockchainMetadata.Common.CONNECTION_TIMEOUT_SECONDS);
        
        // 재시도 설정
        assertEquals(3, BlockchainMetadata.Common.MAX_RETRY_ATTEMPTS);
        assertEquals(1000L, BlockchainMetadata.Common.RETRY_DELAY_MS);
        
        // 로깅 설정
        assertTrue(BlockchainMetadata.Common.ENABLE_DETAILED_LOGGING);
        assertTrue(BlockchainMetadata.Common.ENABLE_TRANSACTION_LOGGING);
        
        // 보안 설정
        assertEquals(8, BlockchainMetadata.Common.MIN_PASSWORD_LENGTH);
        assertTrue(BlockchainMetadata.Common.REQUIRE_SPECIAL_CHARACTERS);
        assertTrue(BlockchainMetadata.Common.REQUIRE_NUMBERS);
        assertTrue(BlockchainMetadata.Common.REQUIRE_UPPERCASE);
    }

    @Test
    @DisplayName("환경 설정 테스트")
    void testEnvironmentMetadata() {
        // 환경 상수
        assertEquals("dev", BlockchainMetadata.Environment.DEVELOPMENT);
        assertEquals("staging", BlockchainMetadata.Environment.STAGING);
        assertEquals("prod", BlockchainMetadata.Environment.PRODUCTION);
        
        // 기본값은 개발 환경
        assertEquals("dev", BlockchainMetadata.Environment.CURRENT_ENV);
        
        // 환경 확인 메서드
        assertTrue(BlockchainMetadata.Environment.isDevelopment());
        assertFalse(BlockchainMetadata.Environment.isStaging());
        assertFalse(BlockchainMetadata.Environment.isProduction());
    }

    @Test
    @DisplayName("유틸리티 메서드 테스트")
    void testUtilsMethods() {
        // 이더리움 가스 비용 계산
        BigInteger gasUsed = BigInteger.valueOf(21000L);
        BigDecimal gasCost = BlockchainMetadata.Utils.calculateEthereumGasCost(gasUsed);
        assertNotNull(gasCost);
        assertTrue(gasCost.compareTo(BigDecimal.ZERO) > 0);
        
        // 솔라나 수수료 계산
        BigDecimal solanaFee = BlockchainMetadata.Utils.calculateSolanaFee();
        assertEquals(BlockchainMetadata.Solana.TRANSACTION_FEE, solanaFee);
        
        // 교환 수수료 계산
        BigDecimal amount = BigDecimal.valueOf(100.0);
        BigDecimal ethereumFee = BlockchainMetadata.Utils.calculateExchangeFee(amount, "ETHEREUM");
        assertEquals(amount.multiply(BlockchainMetadata.Ethereum.EXCHANGE_FEE_PERCENTAGE)
            .divide(BigDecimal.valueOf(100)), ethereumFee);
        
        BigDecimal solanaExchangeFee = BlockchainMetadata.Utils.calculateExchangeFee(amount, "SOLANA");
        assertEquals(amount.multiply(BlockchainMetadata.Solana.EXCHANGE_FEE_PERCENTAGE)
            .divide(BigDecimal.valueOf(100)), solanaExchangeFee);
        
        // 거래 금액 유효성 검사
        assertTrue(BlockchainMetadata.Utils.isValidTransactionAmount(BigDecimal.valueOf(1.0), "ETHEREUM"));
        assertTrue(BlockchainMetadata.Utils.isValidTransactionAmount(BigDecimal.valueOf(1.0), "SOLANA"));
        assertFalse(BlockchainMetadata.Utils.isValidTransactionAmount(BigDecimal.valueOf(0.00001), "ETHEREUM")); // 최소 금액보다 작음
        assertFalse(BlockchainMetadata.Utils.isValidTransactionAmount(BigDecimal.valueOf(2000.0), "ETHEREUM")); // 최대 금액보다 큼
    }

    @Test
    @DisplayName("실제 사용 시나리오 테스트")
    void testRealWorldUsage() {
        // 시나리오: 이더리움 ERC20 토큰 전송 준비
        String contractAddress = BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS;
        String symbol = BlockchainMetadata.Ethereum.ERC20_SYMBOL;
        BigInteger gasPrice = BlockchainMetadata.Ethereum.GAS_PRICE;
        BigInteger gasLimit = BlockchainMetadata.Ethereum.GAS_LIMIT;
        
        assertNotNull(contractAddress);
        assertNotNull(symbol);
        assertNotNull(gasPrice);
        assertNotNull(gasLimit);
        
        // 시나리오: 솔라나 SPL 토큰 전송 준비
        String solanaTokenMint = BlockchainMetadata.Solana.SPL_TOKEN_MINT_ADDRESS;
        String solanaSymbol = BlockchainMetadata.Solana.SPL_TOKEN_SYMBOL;
        BigDecimal solanaFee = BlockchainMetadata.Solana.TRANSACTION_FEE;
        
        assertNotNull(solanaTokenMint);
        assertNotNull(solanaSymbol);
        assertNotNull(solanaFee);
        
        // 시나리오: 교환 수수료 계산
        BigDecimal exchangeAmount = BigDecimal.valueOf(1000.0);
        BigDecimal ethereumExchangeFee = BlockchainMetadata.Utils.calculateExchangeFee(exchangeAmount, "ETHEREUM");
        BigDecimal solanaExchangeFee = BlockchainMetadata.Utils.calculateExchangeFee(exchangeAmount, "SOLANA");
        
        // 이더리움 수수료가 솔라나보다 높음 (0.5% vs 0.3%)
        assertTrue(ethereumExchangeFee.compareTo(solanaExchangeFee) > 0);
        
        // 시나리오: 거래 금액 유효성 검사
        BigDecimal validAmount = BigDecimal.valueOf(100.0);
        assertTrue(BlockchainMetadata.Utils.isValidTransactionAmount(validAmount, "ETHEREUM"));
        assertTrue(BlockchainMetadata.Utils.isValidTransactionAmount(validAmount, "SOLANA"));
    }
} 