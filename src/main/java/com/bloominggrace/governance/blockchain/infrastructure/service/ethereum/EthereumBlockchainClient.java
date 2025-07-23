package com.bloominggrace.governance.blockchain.infrastructure.service.ethereum;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcError;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcRequest;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcResponse;
import com.bloominggrace.governance.shared.domain.model.BlockchainMetadata;
import com.bloominggrace.governance.shared.util.HexUtils;
import com.bloominggrace.governance.shared.util.JsonRpcClient;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
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
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getTransactionCount", Arrays.asList(address, "pending"));
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

    public BigInteger getProposalCount() {
        try {
            log.info("Calling proposalCount() function on governance contract: {}", BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS);

            // 1. proposalCount() 함수 정의
            Function function = new Function(
                "proposalCount",                              // 함수명
                Collections.emptyList(),                      // 입력 파라미터 없음
                Arrays.asList(new org.web3j.abi.TypeReference<Uint256>() {})
            );

            // 2. 함수 호출 데이터 인코딩
            String encodedFunction = FunctionEncoder.encode(function);
            log.debug("Encoded function data: {}", encodedFunction);

            // 3. eth_call RPC 요청 생성
            EthereumRpcRequest request = EthereumRpcRequest.of(
                "eth_call",
                Arrays.asList(
                    Map.of(
                        "to", BlockchainMetadata.Ethereum.GOVERNANCE_CONTRACT_ADDRESS,
                        "data", encodedFunction
                    ),
                    "latest"
                )
            );

            // 4. RPC 요청 전송
            EthereumRpcResponse<String> response = jsonRpcClient.sendRequest(
                rpcUrl,
                request,
                new TypeReference<EthereumRpcResponse<String>>() {}
            );

            // 5. 응답 확인
            if (response.hasError()) {
                log.error("Error calling proposalCount(): {}", response.getError().getMessage());
                return BigInteger.ZERO;
            }

            // 6. 결과 확인
            String result = response.getResult();
            if (result == null || result.equals("0x")) {
                log.warn("Empty result from proposalCount()");
                return BigInteger.ZERO;
            }

            // 7. hex 결과를 BigInteger로 변환
            BigInteger proposalCount = new BigInteger(result.substring(2), 16);
            log.info("Successfully retrieved proposal count: {}", proposalCount);

            return proposalCount;

        } catch (Exception e) {
            log.error("Error calling proposalCount() function: {}", e.getMessage(), e);
            return BigInteger.ZERO;
        }
    }

    @Override
    public Long getBlockTimestamp(String blockNumber) {
        try {
            // blockNumber가 null이면 "latest" 사용
            String targetBlock = (blockNumber != null) ? blockNumber : "latest";
            log.info("Getting block timestamp for block: {}", targetBlock);

            // eth_getBlockByNumber RPC 요청 생성
            EthereumRpcRequest request = EthereumRpcRequest.of(
                "eth_getBlockByNumber",
                Arrays.asList(targetBlock, false) // false: 전체 블록 정보가 아닌 기본 정보만
            );

            // RPC 요청 전송
            EthereumRpcResponse<Map<String, Object>> response = jsonRpcClient.sendRequest(
                rpcUrl,
                request,
                new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {}
            );

            // 응답 확인
            if (response.hasError()) {
                log.error("Error getting block timestamp: {}", response.getError().getMessage());
                return null;
            }

            Map<String, Object> blockInfo = response.getResult();
            if (blockInfo == null) {
                log.error("Received null block info from Ethereum RPC");
                return null;
            }

            // timestamp 필드 추출 (hex 문자열)
            String timestampHex = (String) blockInfo.get("timestamp");
            if (timestampHex == null) {
                log.error("Block info does not contain timestamp field");
                return null;
            }

            // hex를 Long으로 변환
            Long timestamp = Long.parseLong(timestampHex.substring(2), 16);
            log.info("Successfully retrieved block timestamp: {} for block: {}", timestamp, targetBlock);

            return timestamp;

        } catch (Exception e) {
            log.error("Error getting block timestamp for block {}: {}", blockNumber, e.getMessage(), e);
            return null;
        }
    }

    private String padLeft(String value, int length) {
        return String.format("%" + length + "s", value).replace(' ', '0');
    }
} 