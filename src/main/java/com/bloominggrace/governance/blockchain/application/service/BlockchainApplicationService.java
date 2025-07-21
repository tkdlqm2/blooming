package com.bloominggrace.governance.blockchain.application.service;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.wallet.application.service.WalletServiceFactory;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.infrastructure.service.AdminWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class BlockchainApplicationService {

    private final BlockchainClientFactory blockchainClientFactory;
    private final WalletServiceFactory walletServiceFactory;
    private final AdminWalletService adminWalletService;

    /**
     * 잔액을 조회합니다.
     */
    public BigDecimal getBalance(String address, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return new BigDecimal(blockchainClient.getBalance(address));
    }

    /**
     * 토큰 잔액을 조회합니다.
     */
    public BigDecimal getTokenBalance(String walletAddress, String tokenAddress, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return new BigDecimal(blockchainClient.getTokenBalance(tokenAddress, walletAddress));
    }

    /**
     * 트랜잭션을 조회합니다.
     */
    public Optional<String> getTransaction(String txHash, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return Optional.of(blockchainClient.getTransactionStatus(txHash));
    }

    /**
     * 트랜잭션 영수증을 조회합니다.
     */
    public Optional<String> getTransactionReceipt(String txHash, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return Optional.of(blockchainClient.getTransactionReceipt(txHash));
    }

    /**
     * 가스 가격을 조회합니다.
     */
    public BigDecimal getGasPrice(NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return new BigDecimal(blockchainClient.getGasPrice());
    }

    /**
     * 가스 한도를 추정합니다.
     */
    public String estimateGas(String fromAddress, String toAddress, String data, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.estimateGas(fromAddress, toAddress, data);
    }

    /**
     * 계정의 nonce를 조회합니다.
     */
    public String getNonce(String address, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.getNonce(address);
    }

    /**
     * 네트워크 연결 상태를 확인합니다.
     */
    public boolean isNetworkConnected(NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return "SYNCED".equals(blockchainClient.getNetworkStatus());
    }

    /**
     * 네트워크 상태를 조회합니다.
     */
    public String getNetworkStatus(NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.getNetworkStatus();
    }

    /**
     * 최신 블록 번호를 조회합니다.
     */
    public String getLatestBlockNumber(NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.getLatestBlockNumber();
    }

    /**
     * 최신 블록 해시를 조회합니다.
     */
    public String getLatestBlockHash(NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.getLatestBlockHash();
    }

    /**
     * 블록 정보를 조회합니다.
     */
    public String getBlockByHash(String blockHash, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.getBlockByHash(blockHash);
    }

    /**
     * 블록 번호로 블록 정보를 조회합니다.
     */
    public String getBlockByNumber(String blockNumber, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.getBlockByNumber(blockNumber);
    }

    /**
     * 네트워크 ID를 조회합니다.
     */
    public String getNetworkId(NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.getNetworkId();
    }

    /**
     * 체인 ID를 조회합니다.
     */
    public String getChainId(NetworkType networkType) {
        BlockchainClient client = blockchainClientFactory.getClient(networkType);
        return client.getChainId();
    }

    /**
     * Admin 지갑의 nonce 캐시를 초기화합니다.
     */
    public void resetAdminNonce(NetworkType networkType) {
        adminWalletService.clearNonceCache(networkType);
    }

    /**
     * Admin 지갑의 nonce를 강제로 블록체인에서 다시 로딩합니다.
     */
    public String reloadAdminNonce(NetworkType networkType) {
        BigInteger nonce = adminWalletService.forceReloadAdminWalletNonce(networkType);
        return nonce.toString();
    }

    /**
     * Admin 지갑의 nonce를 0으로 강제 설정합니다 (테스트용).
     */
    public String setAdminNonceToZero(NetworkType networkType) {
        BigInteger nonce = adminWalletService.forceSetAdminWalletNonceToZero(networkType);
        return nonce.toString();
    }

    /**
     * Admin 지갑의 nonce를 자동으로 증가시킵니다.
     */
    public String incrementAdminNonce(NetworkType networkType) {
        BigInteger nonce = adminWalletService.incrementAdminNonce(networkType);
        return nonce.toString();
    }

    /**
     * Admin 지갑의 현재 nonce를 가져옵니다.
     */
    public String getCurrentAdminNonce(NetworkType networkType) {
        BigInteger nonce = adminWalletService.getCurrentAdminNonce(networkType);
        return nonce.toString();
    }

    /**
     * 트랜잭션 수수료를 계산합니다.
     */
    public String calculateTransactionFee(String gasPrice, String gasLimit, NetworkType networkType) {
        BlockchainClient blockchainClient = blockchainClientFactory.getClient(networkType);
        return blockchainClient.calculateTransactionFee(gasPrice, gasLimit);
    }

    /**
     * 주소 유효성을 검증합니다.
     */
    public boolean validateAddress(String address, NetworkType networkType) {
        return walletServiceFactory.getWalletService(networkType).isValidAddress(address);
    }
} 