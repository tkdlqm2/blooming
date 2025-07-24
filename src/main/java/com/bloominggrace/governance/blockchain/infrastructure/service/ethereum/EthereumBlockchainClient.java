package com.bloominggrace.governance.blockchain.infrastructure.service.ethereum;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.blockchain.infrastructure.service.dto.BlockchainRpcRequest;
import com.bloominggrace.governance.blockchain.infrastructure.service.dto.BlockchainRpcResponse;
import com.bloominggrace.governance.shared.blockchain.domain.constants.EthereumConstants;
import com.bloominggrace.governance.shared.blockchain.util.JsonRpcClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

/**
 * Ethereum 블록체인 클라이언트 구현체
 * HTTP JSON-RPC를 사용하여 Ethereum RPC와 통신
 */
@Slf4j
@Service("ethereumBlockchainClient")
public class EthereumBlockchainClient implements BlockchainClient {
    
    private final JsonRpcClient jsonRpcClient;
    private final ObjectMapper objectMapper;
    private final String rpcUrl;

    public EthereumBlockchainClient(@Value("${blockchain.ethereum.rpc-url}") String rpcUrl,
                                   JsonRpcClient jsonRpcClient,
                                   ObjectMapper objectMapper) {
        this.rpcUrl = rpcUrl;
        this.jsonRpcClient = jsonRpcClient;
        this.objectMapper = objectMapper;
        log.info("EthereumBlockchainClient initialized with RPC URL: {}", rpcUrl);
        log.info("Configuration check - rpcUrl value: '{}'", rpcUrl);
    }
    
    @Override
    public NetworkType getNetworkType() {
        return NetworkType.ETHEREUM;
    }
    
    @Override
    public String getLatestBlockHash() {
        try {
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_BLOCK_BY_NUMBER, Arrays.asList(EthereumConstants.RpcParams.LATEST, false));
            BlockchainRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<Map<String, Object>>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get latest block hash: {}", response.getError().getMessage());
                return null;
            }
            
            if (response.getResult() == null) {
                log.error("Received null result from Ethereum RPC");
                return null;
            }
            
            String hash = (String) response.getResult().get("hash");
            log.info("Successfully got latest block hash: {}", hash);
            return hash;
        } catch (Exception e) {
            log.error("Error getting latest block hash: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public String getGasPrice() {
        try {
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_GAS_PRICE, Arrays.asList());
            BlockchainRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<String>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get gas price: {}", response.getError().getMessage());
                return "0";
            }
            
            // Hex를 decimal로 변환
            return new BigInteger(response.getResult().substring(2), 16).toString();
        } catch (Exception e) {
            log.error("Error getting gas price", e);
            return "0";
        }
    }
    
    @Override
    public String estimateGas(String fromAddress, String toAddress, String data) {
        try {
            Map<String, String> transaction = Map.of(
                "from", fromAddress,
                "to", toAddress,
                "data", data
            );
            
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.ESTIMATE_GAS, Arrays.asList(transaction));
            BlockchainRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<String>>() {});
            
            if (response.hasError()) {
                log.error("Gas estimation error: {}", response.getError().getMessage());
                return "0";
            }
            
            // Hex를 decimal로 변환
            return new BigInteger(response.getResult().substring(2), 16).toString();
        } catch (Exception e) {
            log.error("Error estimating gas", e);
            return "0";
        }
    }
    
    @Override
    public String getNonce(String address) {
        try {
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_TRANSACTION_COUNT, Arrays.asList(address, EthereumConstants.RpcParams.PENDING));
            BlockchainRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<String>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get nonce: {}", response.getError().getMessage());
                return "0";
            }
            
            return new BigInteger(response.getResult().substring(2), 16).toString();
        } catch (Exception e) {
            log.error("Error getting nonce for address: {}", address, e);
            return "0";
        }
    }
    
    @Override
    public String getBalance(String address) {
        try {
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_BALANCE, Arrays.asList(address, EthereumConstants.RpcParams.LATEST));
            BlockchainRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<String>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get balance: {}", response.getError().getMessage());
                return "0";
            }
            
            // Hex를 decimal로 변환 (Wei 단위)
            return new BigInteger(response.getResult().substring(2), 16).toString();
        } catch (Exception e) {
            log.error("Error getting balance for address: {}", address, e);
            return "0";
        }
    }
    
    @Override
    public String getTokenBalance(String tokenAddress, String walletAddress) {
        try {
            log.info("=== EthereumBlockchainClient.getTokenBalance Debug ===");
            log.info("Token Address: {}", tokenAddress);
            log.info("Wallet Address: {}", walletAddress);
            log.info("RPC URL: {}", rpcUrl);
            
            // ERC20 balanceOf 함수 호출
            String balanceOfData = EthereumConstants.Token.BALANCE_OF_SELECTOR + padLeft(walletAddress.substring(2), 64);
            log.info("BalanceOf Data: {}", balanceOfData);
            
            Map<String, String> transaction = Map.of(
                "to", tokenAddress,
                "data", balanceOfData
            );
            log.info("RPC Transaction: {}", transaction);
            
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.CALL, Arrays.asList(transaction, EthereumConstants.RpcParams.LATEST));
            log.info("RPC Request: {}", request);
            
            // 직접 curl 명령어 출력 (디버깅용)
            String curlCommand = String.format(
                "curl -X POST %s -H \"Content-Type: application/json\" -d '{\"jsonrpc\":\"2.0\",\"method\":\"eth_call\",\"params\":[{\"to\":\"%s\",\"data\":\"%s\"},\"latest\"],\"id\":1}'",
                rpcUrl, tokenAddress, balanceOfData
            );
            log.info("Equivalent curl command: {}", curlCommand);
            
            BlockchainRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<String>>() {});
            log.info("RPC Response: {}", response);
            
            if (response.hasError()) {
                log.error("Token balance query error: {}", response.getError().getMessage());
                return "0";
            }
            
            String result = response.getResult();
            log.info("Raw RPC Result: {}", result);
            
            if (result == null) {
                log.error("RPC result is null");
                return "0";
            }
            
            if (result.equals("0x")) {
                log.info("Empty result, returning 0");
                return "0";
            }
            
            String balance = new BigInteger(result.substring(2), 16).toString();
            log.info("Parsed Balance: {}", balance);
            log.info("=== End Debug ===");
            
            return balance;
        } catch (Exception e) {
            log.error("Error getting token balance for token: {} wallet: {}", tokenAddress, walletAddress, e);
            return "0";
        }
    }
    
    @Override
    public String broadcastTransaction(String signedTransaction) {
        try {
            log.info("=== Ethereum Transaction Broadcast Debug ===");
            log.info("Broadcasting Ethereum raw transaction: {}", signedTransaction);
            log.info("RPC URL: {}", rpcUrl);
            log.info("Network ID: {}", EthereumConstants.Network.NETWORK_NAME);
            log.info("Chain ID: {}", EthereumConstants.Network.CHAIN_ID);

            // 서명된 트랜잭션 형식 검증
            if (signedTransaction == null || signedTransaction.trim().isEmpty()) {
                log.error("Signed transaction is null or empty");
                return null;
            }
            
            if (!signedTransaction.startsWith("0x")) {
                log.error("Signed transaction must start with 0x: {}", signedTransaction);
                return null;
            }
            
            log.info("Signed transaction format validation passed");

            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.SEND_RAW_TRANSACTION, Arrays.asList(signedTransaction));
            log.info("RPC Request: {}", request);
            
            log.info("Sending request to RPC endpoint...");
            BlockchainRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<String>>() {});
            log.info("RPC Response: {}", response);
            
            if (response.hasError()) {
                log.error("Transaction broadcast error: {}", response.getError().getMessage());
                log.error("Error details: {}", response.getError());
                log.error("Error code: {}", response.getError().getCode());
                return null;
            }
            
            String transactionHash = response.getResult();
            log.info("Transaction broadcast successful. Hash: {}", transactionHash);
            log.info("=== End Debug ===");
            
            return transactionHash;

        } catch (Exception e) {
            log.error("=== Ethereum Transaction Broadcast Exception ===");
            log.error("Error broadcasting transaction", e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception message: {}", e.getMessage());
            log.error("=== End Exception Debug ===");
            return null;
        }
    }
    
    @Override
    public String getTransactionStatus(String transactionHash) {
        try {
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_TRANSACTION_RECEIPT, Arrays.asList(transactionHash));
            BlockchainRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<Map<String, Object>>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get transaction status: {}", response.getError().getMessage());
                return "PENDING";
            }
            
            if (response.getResult() == null) {
                return "PENDING";
            }
            
            String status = (String) response.getResult().get("status");
            if ("0x1".equals(status)) {
                return "CONFIRMED";
            } else {
                return "FAILED";
            }
        } catch (Exception e) {
            log.error("Error getting transaction status for hash: {}", transactionHash, e);
            return "FAILED";
        }
    }
    
    @Override
    public String getTransactionReceipt(String transactionHash) {
        try {
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_TRANSACTION_RECEIPT, Arrays.asList(transactionHash));
            BlockchainRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<Map<String, Object>>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get transaction receipt: {}", response.getError().getMessage());
                return null;
            }
            
            return objectMapper.writeValueAsString(response.getResult());
        } catch (Exception e) {
            log.error("Error getting transaction receipt for hash: {}", transactionHash, e);
            return null;
        }
    }
    
    @Override
    public String getBlockByHash(String blockHash) {
        try {
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_BLOCK_BY_HASH, Arrays.asList(blockHash, false));
            BlockchainRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<Map<String, Object>>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get block by hash: {}", response.getError().getMessage());
                return null;
            }
            
            return objectMapper.writeValueAsString(response.getResult());
        } catch (Exception e) {
            log.error("Error getting block by hash: {}", blockHash, e);
            return null;
        }
    }
    
    @Override
    public String getBlockByNumber(String blockNumber) {
        try {
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_BLOCK_BY_NUMBER, Arrays.asList(blockNumber, false));
            BlockchainRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<Map<String, Object>>>() {
            });

            if (response.hasError()) {
                log.error("Failed to get block by number: {}", response.getError().getMessage());
                return null;
            }

            return objectMapper.writeValueAsString(response.getResult());
        } catch (Exception e) {
            log.error("Error getting block by number: {}", blockNumber, e);
            return null;
        }
    }
    
    @Override
    public String getLatestBlockNumber() {
        try {
            log.info("Requesting latest block number from: {}", rpcUrl);
            
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_BLOCK_NUMBER, Arrays.asList());
            log.debug("Sending request: {}", request);
            
            BlockchainRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<String>>() {});
            log.debug("Received response: {}", response);
            
            if (response.hasError()) {
                log.error("Failed to get latest block number: {}", response.getError().getMessage());
                return "0";
            }
            
            if (response.getResult() == null) {
                log.error("Received null result from Ethereum RPC");
                return "0";
            }
            
            // Hex를 decimal로 변환
            String result = new BigInteger(response.getResult().substring(2), 16).toString();
            log.info("Successfully got latest block number: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error getting latest block number: {}", e.getMessage(), e);
            return "0";
        }
    }

    @Override
    public Long getBlockTimestamp(String blockNumber) {
        try {
            log.info("Getting block timestamp for block: {}", blockNumber);
            
            // blockNumber가 null이면 최신 블록 사용
            String targetBlock = (blockNumber == null || blockNumber.isEmpty()) ? 
                EthereumConstants.RpcParams.LATEST : blockNumber;
            
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.GET_BLOCK_BY_NUMBER,
                Arrays.asList(targetBlock, false));
            BlockchainRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request,
                new TypeReference<BlockchainRpcResponse<Map<String, Object>>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get block timestamp: {}", response.getError().getMessage());
                return null;
            }
            
            Map<String, Object> block = response.getResult();
            if (block == null) {
                log.error("Received null block from Ethereum RPC");
                return null;
            }
            
            String timestampHex = (String) block.get("timestamp");
            if (timestampHex == null) {
                log.error("Block timestamp is null");
                return null;
            }
            
            // Hex를 decimal로 변환하여 Unix timestamp 반환
            Long timestamp = new BigInteger(timestampHex.substring(2), 16).longValue();
            log.info("Block timestamp: {} (block: {})", timestamp, targetBlock);
            return timestamp;
            
        } catch (Exception e) {
            log.error("Error getting block timestamp: {}", e.getMessage(), e);
            return null;
        }
    }



    private String padLeft(String value, int length) {
        return String.format("%" + length + "s", value).replace(' ', '0');
    }

    @Override
    public BigInteger getProposalCount() {
        try {
            log.info("Calling proposalCount() function on governance contract: {}", EthereumConstants.Contracts.GOVERNANCE_CONTRACT_ADDRESS);
            
            // 1. proposalCount() 함수 정의
            String functionData = "0x" + "da35c664"; // proposalCount() 함수의 Method ID
            
            // 2. eth_call RPC 요청 생성
            Map<String, String> transaction = Map.of(
                "to", EthereumConstants.Contracts.GOVERNANCE_CONTRACT_ADDRESS,
                "data", functionData
            );
            
            BlockchainRpcRequest request = BlockchainRpcRequest.of(EthereumConstants.RpcMethods.CALL, Arrays.asList(transaction, EthereumConstants.RpcParams.LATEST));
            BlockchainRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<BlockchainRpcResponse<String>>() {});
            
            if (response.hasError()) {
                log.error("Error calling proposalCount(): {}", response.getError().getMessage());
                return BigInteger.ZERO;
            }
            
            String result = response.getResult();
            if (result == null || result.equals("0x")) {
                log.warn("Empty result from proposalCount()");
                return BigInteger.ZERO;
            }
            
            BigInteger proposalCount = new BigInteger(result.substring(2), 16);
            log.info("Successfully retrieved proposal count: {}", proposalCount);
            
            return proposalCount;
            
        } catch (Exception e) {
            log.error("Error calling proposalCount() function: {}", e.getMessage(), e);
            return BigInteger.ZERO;
        }
    }
} 