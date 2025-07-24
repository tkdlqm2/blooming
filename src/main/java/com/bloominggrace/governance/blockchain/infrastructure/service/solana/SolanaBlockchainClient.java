package com.bloominggrace.governance.blockchain.infrastructure.service.solana;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigInteger;


@Slf4j
@Service("solanaBlockchainClient")
public class SolanaBlockchainClient implements BlockchainClient {

    @Override
    public NetworkType getNetworkType() {
        return null;
    }

    @Override
    public String getLatestBlockHash() {
        return "";
    }

    @Override
    public String getGasPrice() {
        return "";
    }

    @Override
    public String estimateGas(String fromAddress, String toAddress, String data) {
        return "";
    }

    @Override
    public String getNonce(String address) {
        return "";
    }

    @Override
    public String getBalance(String address) {
        return "";
    }

    @Override
    public String getTokenBalance(String tokenAddress, String walletAddress) {
        return "";
    }

    @Override
    public String broadcastTransaction(String signedTransaction) {
        return "";
    }

    @Override
    public String getTransactionStatus(String transactionHash) {
        return "";
    }

    @Override
    public String getTransactionReceipt(String transactionHash) {
        return "";
    }

    @Override
    public String getBlockByHash(String blockHash) {
        return "";
    }

    @Override
    public String getBlockByNumber(String blockNumber) {
        return "";
    }

    @Override
    public String getLatestBlockNumber() {
        return "";
    }

    @Override
    public BigInteger getProposalCount() {
        return null;
    }

    @Override
    public Long getBlockTimestamp(String blockNumber) {
        return 0L;
    }
}