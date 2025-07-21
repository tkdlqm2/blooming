package com.bloominggrace.governance.shared.infrastructure.service;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.shared.domain.model.EthereumTransactionBody;
import com.bloominggrace.governance.shared.domain.service.TransactionBuilder;
import com.bloominggrace.governance.shared.infrastructure.service.AdminWalletService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;

/**
 * 이더리움 트랜잭션 빌더 구현체
 */
@Service
public class EthereumTransactionBuilder implements TransactionBuilder {

    // AdminWalletService는 static 메서드로 변경되어 의존성 제거
    private final BlockchainClient ethereumBlockchainClient;

    // Sepolia 네트워크 설정
    private static final BigInteger SEPOLIA_CHAIN_ID = BigInteger.valueOf(11155111L);
    private static final BigInteger MAX_FEE_PER_GAS = BigInteger.valueOf(20000000000L); // 20 gwei
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(21000L);
    private static final BigInteger GAS_LIMIT_FOR_CONTRACT = BigInteger.valueOf(100000L); // 컨트랙트 호출용

    @Autowired
    public EthereumTransactionBuilder(BlockchainClient ethereumBlockchainClient) {
        this.ethereumBlockchainClient = ethereumBlockchainClient;
    }

    @Override
    public Object buildTransactionBody(String fromAddress, String toAddress, BigDecimal value, NetworkType networkType) {
        if (networkType != NetworkType.ETHEREUM) {
            throw new IllegalArgumentException("Network type must be ETHEREUM for EthereumTransactionBuilder");
        }

        BigInteger nonce = getNonce(fromAddress);
        BigInteger valueInWei = convertEthToWei(value);

        return new EthereumTransactionBody(
                SEPOLIA_CHAIN_ID,
                nonce,
                MAX_FEE_PER_GAS,
                GAS_LIMIT,
                toAddress,
                fromAddress,
                valueInWei,
                null // 일반 ETH 전송은 data가 없음
        );
    }

    @Override
    public Object buildMintTransactionBody(String fromAddress, String toAddress, BigDecimal tokenAmount, 
                                         String tokenContractAddress, NetworkType networkType) {
        if (networkType != NetworkType.ETHEREUM) {
            throw new IllegalArgumentException("Network type must be ETHEREUM for EthereumTransactionBuilder");
        }

        BigInteger nonce = getNonce(fromAddress);
        
        // ERC20 mint 함수 호출을 위한 데이터 생성
        String mintData = generateMintData(toAddress, tokenAmount);

        return new EthereumTransactionBody(
                SEPOLIA_CHAIN_ID,
                nonce,
                MAX_FEE_PER_GAS,
                GAS_LIMIT_FOR_CONTRACT, // 컨트랙트 호출이므로 더 많은 가스 필요
                tokenContractAddress,   // 토큰 컨트랙트 주소로 전송
                fromAddress,
                BigInteger.ZERO,        // ETH 전송 없음
                mintData               // mint 함수 호출 데이터
        );
    }

    /**
     * ERC20 토큰 전송 트랜잭션 바디 생성
     */
    public TransactionBody<Object> buildErc20TransferTxBody(String fromAddress, String toAddress, BigDecimal tokenAmount, String tokenContractAddress, NetworkType networkType) {
        if (networkType != NetworkType.ETHEREUM) {
            throw new IllegalArgumentException("Network type must be ETHEREUM for EthereumTransactionBuilder");
        }

        BigInteger nonce = getNonce(fromAddress);
        // ERC20 transfer 함수 호출 데이터 생성
        String transferData = generateErc20TransferData(toAddress, tokenAmount);

        // EthereumTransactionData 생성
        com.bloominggrace.governance.shared.domain.model.EthereumTransactionData ethereumData = 
            new com.bloominggrace.governance.shared.domain.model.EthereumTransactionData(
                BigInteger.valueOf(20_000_000_000L), // 20 Gwei
                BigInteger.valueOf(100000L), // 컨트랙트 호출용 가스
                BigInteger.ZERO, // ETH 전송 없음
                toAddress, // 받는 주소
                tokenContractAddress, // 토큰 컨트랙트 주소
                transferData, // transfer 함수 호출 데이터
                nonce
            );

        return TransactionBody.builder()
            .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
            .fromAddress(fromAddress)
            .toAddress(tokenContractAddress)
            .data(transferData)
            .networkType(networkType.name())
            .networkSpecificData(ethereumData)
            .build();
    }

    /**
     * 주소에 따른 nonce를 가져옵니다.
     * Admin 주소인 경우 AdminWalletService를 사용하고, 
     * 그렇지 않은 경우 블록체인 클라이언트를 사용합니다.
     */
    private BigInteger getNonce(String address) {
        try {
            // Admin 주소인지 확인
            if (isAdminAddress(address)) {
                return AdminWalletService.getAdminWalletNonce(NetworkType.ETHEREUM);
            } else {
                // 일반 주소인 경우 블록체인에서 nonce 조회
                String nonceStr = ethereumBlockchainClient.getNonce(address);
                // hex 문자열을 BigInteger로 변환
                if (nonceStr.startsWith("0x")) {
                    nonceStr = nonceStr.substring(2);
                }
                return new BigInteger(nonceStr, 16);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get nonce for address: " + address, e);
        }
    }

    /**
     * Admin 주소인지 확인합니다.
     */
    private boolean isAdminAddress(String address) {
        try {
            return AdminWalletService.getAdminWallet(NetworkType.ETHEREUM).getWalletAddress().equalsIgnoreCase(address);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ETH를 Wei로 변환합니다.
     */
    private BigInteger convertEthToWei(BigDecimal ethAmount) {
        return ethAmount.multiply(BigDecimal.valueOf(1_000_000_000_000_000_000L)).toBigInteger();
    }

    /**
     * ERC20 mint 함수 호출을 위한 데이터를 생성합니다.
     * mint(address to, uint256 amount) 함수 시그니처: 0x40c10f19
     */
    private String generateMintData(String toAddress, BigDecimal tokenAmount) {
        // mint 함수 시그니처
        String functionSignature = "0x40c10f19";
        
        // to 주소를 32바이트로 패딩 (앞에 0x 제거 후 24자리 0으로 패딩)
        String paddedTo = "000000000000000000000000" + toAddress.substring(2);
        
        // 토큰 양을 32바이트로 패딩 (wei 단위로 변환, 18자리 소수점)
        BigInteger tokenAmountInWei = tokenAmount.multiply(BigDecimal.valueOf(1_000_000_000_000_000_000L)).toBigInteger();
        String paddedAmount = String.format("%064x", tokenAmountInWei);
        
        return functionSignature + paddedTo + paddedAmount;
    }

    /**
     * ERC20 transfer(address,uint256) 함수 호출 데이터 생성
     */
    private String generateErc20TransferData(String toAddress, BigDecimal tokenAmount) {
        // transfer 함수 시그니처: a9059cbb
        String functionSignature = "a9059cbb";
        String paddedTo = "000000000000000000000000" + toAddress.substring(2);
        BigInteger tokenAmountInWei = tokenAmount.multiply(BigDecimal.valueOf(1_000_000_000_000_000_000L)).toBigInteger();
        String paddedAmount = String.format("%064x", tokenAmountInWei);
        return "0x" + functionSignature + paddedTo + paddedAmount;
    }
} 