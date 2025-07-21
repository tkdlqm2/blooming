package com.bloominggrace.governance.blockchain.infrastructure.service.ethereum;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcError;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcRequest;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcResponse;
import com.bloominggrace.governance.shared.util.HexUtils;
import com.bloominggrace.governance.shared.util.JsonRpcClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
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
    
    @Value("${blockchain.ethereum.network-id:11155111}")
    private String networkId;
    
    @Value("${blockchain.ethereum.chain-id:11155111}")
    private String chainId;
    
    @Value("${blockchain.ethereum.admin-private-key}")
    private String adminPrivateKey;

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
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getBlockByNumber", Arrays.asList("latest", false));
            EthereumRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_gasPrice", Arrays.asList());
            EthereumRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<String>>() {});
            
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
            
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_estimateGas", Arrays.asList(transaction));
            EthereumRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<String>>() {});
            
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
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getTransactionCount", Arrays.asList(address, "latest"));
            EthereumRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<String>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get nonce: {}", response.getError().getMessage());
                return "0";
            }
            
            // Hex를 decimal로 변환
            return new BigInteger(response.getResult().substring(2), 16).toString();
        } catch (Exception e) {
            log.error("Error getting nonce for address: {}", address, e);
            return "0";
        }
    }
    
    @Override
    public String getBalance(String address) {
        try {
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getBalance", Arrays.asList(address, "latest"));
            EthereumRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<String>>() {});
            
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
            String balanceOfData = "0x70a08231" + padLeft(walletAddress.substring(2), 64);
            log.info("BalanceOf Data: {}", balanceOfData);
            
            Map<String, String> transaction = Map.of(
                "to", tokenAddress,
                "data", balanceOfData
            );
            log.info("RPC Transaction: {}", transaction);
            
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_call", Arrays.asList(transaction, "latest"));
            log.info("RPC Request: {}", request);
            
            // 직접 curl 명령어 출력 (디버깅용)
            String curlCommand = String.format(
                "curl -X POST %s -H \"Content-Type: application/json\" -d '{\"jsonrpc\":\"2.0\",\"method\":\"eth_call\",\"params\":[{\"to\":\"%s\",\"data\":\"%s\"},\"latest\"],\"id\":1}'",
                rpcUrl, tokenAddress, balanceOfData
            );
            log.info("Equivalent curl command: {}", curlCommand);
            
            EthereumRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<String>>() {});
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
            log.info("Network ID: {}", networkId);
            log.info("Chain ID: {}", chainId);
            
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

            // 실제 RPC 호출 코드 (운영 환경에서 사용)
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_sendRawTransaction", Arrays.asList(signedTransaction));
            log.info("RPC Request: {}", request);
            
            log.info("Sending request to RPC endpoint...");
            EthereumRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<String>>() {});
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
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getTransactionReceipt", Arrays.asList(transactionHash));
            EthereumRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getTransactionReceipt", Arrays.asList(transactionHash));
            EthereumRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getBlockByHash", Arrays.asList(blockHash, false));
            EthereumRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getBlockByNumber", Arrays.asList(blockNumber, false));
            EthereumRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
    public String getNetworkStatus() {
        try {
            EthereumRpcRequest request = EthereumRpcRequest.of("net_listening", Arrays.asList());
            EthereumRpcResponse<Boolean> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<Boolean>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get network status: {}", response.getError().getMessage());
                return "UNHEALTHY";
            }
            
            return response.getResult() ? "HEALTHY" : "UNHEALTHY";
        } catch (Exception e) {
            log.error("Error getting network status", e);
            return "UNHEALTHY";
        }
    }
    
    @Override
    public String getNetworkId() {
        return networkId;
    }
    
    @Override
    public String getChainId() {
        return chainId;
    }
    
    @Override
    public String getLatestBlockNumber() {
        try {
            log.info("Requesting latest block number from: {}", rpcUrl);
            
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_blockNumber", Arrays.asList());
            log.debug("Sending request: {}", request);
            
            EthereumRpcResponse<String> response = jsonRpcClient.sendRequest(rpcUrl, request, new TypeReference<EthereumRpcResponse<String>>() {});
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
    public String calculateTransactionFee(String gasPrice, String gasLimit) {
        try {
            BigInteger price = new BigInteger(gasPrice);
            BigInteger limit = new BigInteger(gasLimit);
            BigInteger fee = price.multiply(limit);
            return fee.toString();
        } catch (Exception e) {
            log.error("Error calculating transaction fee", e);
            return "0";
        }
    }

    /**
     * ETH 전송을 수행합니다.
     */
    public String transferETH(String fromAddress, String toAddress, BigDecimal amount, BigInteger nonce) {
        try {
            log.info("Starting ETH transfer - From: {}, To: {}, Amount: {} ETH, Nonce: {}", 
                fromAddress, toAddress, amount, nonce);
            
            // Admin 지갑 Credentials 생성
            Credentials credentials = Credentials.create(adminPrivateKey);
            
            // Gas price 조회
            BigInteger gasPrice = new BigInteger(getGasPrice());
            BigInteger gasLimit = BigInteger.valueOf(21000); // ETH 전송 기본 가스 한도
            
            // 전송 금액을 Wei로 변환
            BigInteger amountInWei = amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            
            // RawTransaction 생성
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                gasPrice,
                gasLimit,
                toAddress,
                amountInWei
            );
            
            // 트랜잭션 서명
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            
            // 트랜잭션 브로드캐스트
            String transactionHash = broadcastTransaction(hexValue);
            
            log.info("ETH transfer completed - Hash: {}", transactionHash);
            return transactionHash;
            
        } catch (Exception e) {
            log.error("ETH transfer failed", e);
            throw new RuntimeException("ETH transfer failed: " + e.getMessage(), e);
        }
    }

    /**
     * ERC20 토큰 전송을 수행합니다.
     */
    public String transferERC20Token(String fromAddress, String toAddress, String tokenAddress, BigDecimal amount, BigInteger nonce) {
        try {
            log.info("Starting ERC20 transfer - From: {}, To: {}, Token: {}, Amount: {}, Nonce: {}", 
                fromAddress, toAddress, tokenAddress, amount, nonce);
            
            // Admin 지갑 Credentials 생성
            Credentials credentials = Credentials.create(adminPrivateKey);
            
            // Gas price 조회
            BigInteger gasPrice = new BigInteger(getGasPrice());
            BigInteger gasLimit = BigInteger.valueOf(100_000L); // ERC20 전송 가스 한도
            
            // ERC-20 transfer 함수 데이터 생성
            BigInteger amountWei = amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            String transferData = createERC20TransferData(toAddress, amountWei);
            
            // RawTransaction 생성
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                tokenAddress, // 토큰 컨트랙트 주소
                transferData
            );
            
            // 트랜잭션 서명
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            
            // 트랜잭션 브로드캐스트
            String transactionHash = broadcastTransaction(hexValue);
            
            log.info("ERC20 transfer completed - Hash: {}", transactionHash);
            return transactionHash;
            
        } catch (Exception e) {
            log.error("ERC20 transfer failed", e);
            throw new RuntimeException("ERC20 transfer failed: " + e.getMessage(), e);
        }
    }

    /**
     * ERC-20 transfer 함수 데이터 생성
     */
    private String createERC20TransferData(String toAddress, BigInteger amount) {
        try {
            // ERC-20 transfer 함수 selector: transfer(address,uint256)
            String functionSelector = "0xa9059cbb";
            
            // toAddress (20 bytes, padded to 32 bytes)
            String paddedToAddress = "000000000000000000000000" + toAddress.substring(2);
            
            // amount (32 bytes, padded)
            String paddedAmount = String.format("%064x", amount);
            
            String data = functionSelector + paddedToAddress + paddedAmount;
            log.info("Created ERC-20 transfer data: {}", data);
            
            return data;
        } catch (Exception e) {
            log.error("Error creating ERC-20 transfer data", e);
            throw new RuntimeException("Failed to create ERC-20 transfer data", e);
        }
    }

    private String padLeft(String value, int length) {
        return String.format("%" + length + "s", value).replace(' ', '0');
    }
    
    private String encodeString(String value) {
        // 문자열을 ABI 인코딩 (간단한 구현)
        String hexValue = HexUtils.bytesToHex(value.getBytes());
        String length = padLeft(Integer.toHexString(value.length()), 64);
        return length + padRight(hexValue, 64);
    }
    
    private String padRight(String value, int length) {
        return String.format("%-" + length + "s", value).replace(' ', '0');
    }
} 