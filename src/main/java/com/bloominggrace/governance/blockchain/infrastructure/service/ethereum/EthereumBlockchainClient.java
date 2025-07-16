package com.bloominggrace.governance.blockchain.infrastructure.service.ethereum;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcRequest;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcResponse;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto.EthereumRpcError;
import com.bloominggrace.governance.shared.util.HexUtils;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Ethereum 블록체인 클라이언트 구현체
 * HTTP JSON-RPC를 사용하여 Ethereum RPC와 통신
 */
@Slf4j
@Service("ethereumBlockchainClient")
public class EthereumBlockchainClient implements BlockchainClient {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String rpcUrl;
    
    @Value("${blockchain.ethereum.network-id:11155111}")
    private String networkId;
    
    @Value("${blockchain.ethereum.chain-id:11155111}")
    private String chainId;
    
    public EthereumBlockchainClient(@Value("${blockchain.ethereum.rpc-url}") String rpcUrl) {
        this.rpcUrl = rpcUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
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
            EthereumRpcResponse<Map<String, Object>> response = sendRequest(request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
            if (response.hasError()) {
                log.error("Failed to get latest block hash: {}", response.getError().getMessage());
                return null;
            }
            
            return (String) response.getResult().get("hash");
        } catch (Exception e) {
            log.error("Error getting latest block hash", e);
            return null;
        }
    }
    
    @Override
    public String getGasPrice() {
        try {
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_gasPrice", Arrays.asList());
            EthereumRpcResponse<String> response = sendRequest(request, new TypeReference<EthereumRpcResponse<String>>() {});
            
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
            EthereumRpcResponse<String> response = sendRequest(request, new TypeReference<EthereumRpcResponse<String>>() {});
            
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
            EthereumRpcResponse<String> response = sendRequest(request, new TypeReference<EthereumRpcResponse<String>>() {});
            
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
            EthereumRpcResponse<String> response = sendRequest(request, new TypeReference<EthereumRpcResponse<String>>() {});
            
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
            // ERC20 balanceOf 함수 호출
            String balanceOfData = "0x70a08231" + padLeft(walletAddress.substring(2), 64);
            
            Map<String, String> transaction = Map.of(
                "to", tokenAddress,
                "data", balanceOfData
            );
            
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_call", Arrays.asList(transaction, "latest"));
            EthereumRpcResponse<String> response = sendRequest(request, new TypeReference<EthereumRpcResponse<String>>() {});
            
            if (response.hasError()) {
                log.error("Token balance query error: {}", response.getError().getMessage());
                return "0";
            }
            
            String result = response.getResult();
            if (result.equals("0x")) {
                return "0";
            }
            
            return new BigInteger(result.substring(2), 16).toString();
        } catch (Exception e) {
            log.error("Error getting token balance for token: {} wallet: {}", tokenAddress, walletAddress, e);
            return "0";
        }
    }
    
    @Override
    public String broadcastTransaction(String signedTransaction) {
        try {
            log.info("Broadcasting Ethereum raw transaction: {}", signedTransaction);
            
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_sendRawTransaction", Arrays.asList(signedTransaction));
            EthereumRpcResponse<String> response = sendRequest(request, new TypeReference<EthereumRpcResponse<String>>() {});
            
            if (response.hasError()) {
                log.error("Transaction broadcast error: {}", response.getError().getMessage());
                return null;
            }
            
            String transactionHash = response.getResult();
            log.info("Transaction broadcast successful. Hash: {}", transactionHash);
            
            return transactionHash;
        } catch (Exception e) {
            log.error("Error broadcasting transaction", e);
            return null;
        }
    }
    
    @Override
    public String getTransactionStatus(String transactionHash) {
        try {
            EthereumRpcRequest request = EthereumRpcRequest.of("eth_getTransactionReceipt", Arrays.asList(transactionHash));
            EthereumRpcResponse<Map<String, Object>> response = sendRequest(request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
            EthereumRpcResponse<Map<String, Object>> response = sendRequest(request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
            EthereumRpcResponse<Map<String, Object>> response = sendRequest(request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
            EthereumRpcResponse<Map<String, Object>> response = sendRequest(request, new TypeReference<EthereumRpcResponse<Map<String, Object>>>() {});
            
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
            EthereumRpcResponse<Boolean> response = sendRequest(request, new TypeReference<EthereumRpcResponse<Boolean>>() {});
            
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
            
            EthereumRpcResponse<String> response = sendRequest(request, new TypeReference<EthereumRpcResponse<String>>() {});
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
            
            // Wei를 ETH로 변환 (1 ETH = 10^18 Wei)
            BigDecimal feeInEth = new BigDecimal(fee).divide(BigDecimal.valueOf(10).pow(18));
            return feeInEth.toString();
        } catch (Exception e) {
            log.error("Error calculating transaction fee", e);
            return "0";
        }
    }
    
    @Override
    public String createTokenTransferData(String toAddress, String amount) {
        // ERC20 transfer 함수 호출 데이터 생성
        String methodId = "0xa9059cbb"; // transfer(address,uint256)
        String paddedTo = padLeft(toAddress.substring(2), 64);
        String paddedAmount = padLeft(new BigInteger(amount).toString(16), 64);
        return methodId + paddedTo + paddedAmount;
    }
    
    @Override
    public String createTokenApproveData(String spender, String amount) {
        // ERC20 approve 함수 호출 데이터 생성
        String methodId = "0x095ea7b3"; // approve(address,uint256)
        String paddedSpender = padLeft(spender.substring(2), 64);
        String paddedAmount = padLeft(new BigInteger(amount).toString(16), 64);
        return methodId + paddedSpender + paddedAmount;
    }
    
    @Override
    public String createTokenMintData(String toAddress, String amount) {
        // ERC20 mint 함수 호출 데이터 생성 (표준 ERC20에는 없지만 커스텀 토큰에서 사용)
        String methodId = "0x40c10f19"; // mint(address,uint256)
        String paddedTo = padLeft(toAddress.substring(2), 64);
        String paddedAmount = padLeft(new BigInteger(amount).toString(16), 64);
        return methodId + paddedTo + paddedAmount;
    }
    
    @Override
    public String createTokenBurnData(String amount) {
        // ERC20 burn 함수 호출 데이터 생성
        String methodId = "0x42966c68"; // burn(uint256)
        String paddedAmount = padLeft(new BigInteger(amount).toString(16), 64);
        return methodId + paddedAmount;
    }
    
    @Override
    public String createTokenStakeData(String amount) {
        // 스테이킹 함수 호출 데이터 생성 (커스텀 토큰에서 사용)
        String methodId = "0x2e17de78"; // stake(uint256)
        String paddedAmount = padLeft(new BigInteger(amount).toString(16), 64);
        return methodId + paddedAmount;
    }
    
    @Override
    public String createTokenUnstakeData(String amount) {
        // 언스테이킹 함수 호출 데이터 생성 (커스텀 토큰에서 사용)
        String methodId = "0x9f678cca"; // unstake(uint256)
        String paddedAmount = padLeft(new BigInteger(amount).toString(16), 64);
        return methodId + paddedAmount;
    }
    
    @Override
    public String createProposalData(String proposalId, String title, String description, String proposalFee) {
        // 제안 생성 함수 호출 데이터 생성 (커스텀 컨트랙트에서 사용)
        String methodId = "0x01234567"; // createProposal(string,string,uint256)
        String encodedTitle = encodeString(title);
        String encodedDescription = encodeString(description);
        String paddedFee = padLeft(new BigInteger(proposalFee).toString(16), 64);
        return methodId + encodedTitle + encodedDescription + paddedFee;
    }
    
    @Override
    public String createVoteData(String proposalId, String voteType, String votingPower, String reason) {
        // 투표 함수 호출 데이터 생성 (커스텀 컨트랙트에서 사용)
        String methodId = "0x89abcdef"; // vote(uint256,uint8,uint256,string)
        String paddedProposalId = padLeft(new BigInteger(proposalId).toString(16), 64);
        String paddedVoteType = padLeft(new BigInteger(voteType).toString(16), 64);
        String paddedVotingPower = padLeft(new BigInteger(votingPower).toString(16), 64);
        String encodedReason = encodeString(reason);
        return methodId + paddedProposalId + paddedVoteType + paddedVotingPower + encodedReason;
    }
    
    private <T> T sendRequest(EthereumRpcRequest request, TypeReference<T> typeReference) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(request);
        log.info("Sending HTTP request to {}: {}", rpcUrl, requestBody);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(rpcUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.info("Received HTTP response status: {}, body: {}", response.statusCode(), response.body());
        
        if (response.statusCode() != 200) {
            log.error("HTTP request failed with status: {} and body: {}", response.statusCode(), response.body());
            throw new IOException("HTTP request failed with status: " + response.statusCode() + ", body: " + response.body());
        }
        
        try {
            return objectMapper.readValue(response.body(), typeReference);
        } catch (Exception e) {
            log.error("Failed to parse response: {}", response.body(), e);
            throw e;
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