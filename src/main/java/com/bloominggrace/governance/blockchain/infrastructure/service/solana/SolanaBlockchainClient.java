package com.bloominggrace.governance.blockchain.infrastructure.service.solana;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto.*;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Base64;

@Slf4j
@Service("solanaBlockchainClient")
public class SolanaBlockchainClient implements BlockchainClient {

    private static final String DEVNET_URL = "https://api.devnet.solana.com";
    private static final String NETWORK_ID = "devnet";
    private static final String CHAIN_ID = "103";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public SolanaBlockchainClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public NetworkType getNetworkType() {
        return NetworkType.SOLANA;
    }

    @Override
    public String getLatestBlockHash() {
        try {
            JsonRequest request = new JsonRequest("getLatestBlockhash", Arrays.asList());
            SolanaRpcResponse<SolanaBlockHashResult> response = sendRequest(request, new TypeReference<SolanaRpcResponse<SolanaBlockHashResult>>() {});
            
            if (response.getError() != null) {
                log.error("Failed to get latest blockhash: {}", response.getError().getMessage());
                return null;
            }
            
            return response.getResult().getValue().getBlockhash();
        } catch (Exception e) {
            log.error("Error getting latest blockhash", e);
            return null;
        }
    }

    @Override
    public String getGasPrice() {
        // Solana는 고정 수수료 시스템을 사용
        return "5000"; // 0.000005 SOL (5000 lamports)
    }

    @Override
    public String getNonce(String address) {
        // Solana는 nonce 대신 recent blockhash를 사용
        return getLatestBlockHash();
    }

    @Override
    public String getBalance(String address) {
        try {
            JsonRequest request = new JsonRequest("getBalance", Arrays.asList(address));
            SolanaRpcResponse<SolanaBalanceResult> response = sendRequest(request, new TypeReference<SolanaRpcResponse<SolanaBalanceResult>>() {});
            
            if (response.getError() != null) {
                log.error("Failed to get balance: {}", response.getError().getMessage());
                return "0";
            }
            
            // lamports를 SOL로 변환 (1 SOL = 1,000,000,000 lamports)
            BigDecimal balance = BigDecimal.valueOf(response.getResult().getValue()).divide(BigDecimal.valueOf(1_000_000_000));
            return balance.toString();
        } catch (Exception e) {
            log.error("Error getting balance for address: {}", address, e);
            return "0";
        }
    }

    @Override
    public String getTokenBalance(String tokenAddress, String walletAddress) {
        try {
            JsonRequest request = new JsonRequest("getTokenAccountsByOwner", 
                Arrays.asList(walletAddress, Map.of("mint", tokenAddress), Map.of("encoding", "json")));
            String response = sendRequestString(request);
            
            // 간단한 파싱 (실제로는 더 정교한 파싱 필요)
            if (response.contains("\"uiAmount\":")) {
                String[] parts = response.split("\"uiAmount\":");
                if (parts.length > 1) {
                    String amountPart = parts[1].split(",")[0];
                    return amountPart.trim();
                }
            }
            return "0";
        } catch (Exception e) {
            log.error("Error getting token balance for address: {} and token: {}", walletAddress, tokenAddress, e);
            return "0";
        }
    }

    @Override
    public String broadcastTransaction(String signedTransaction) {
        try {
            log.info("Broadcasting Solana transaction: {}", signedTransaction);
            
            JsonRequest request = new JsonRequest("sendTransaction", 
                Arrays.asList(signedTransaction, Map.of("encoding", "base64")));
            SolanaRpcResponse<SolanaSignatureResult> response = sendRequest(request, new TypeReference<SolanaRpcResponse<SolanaSignatureResult>>() {});
            
            if (response.getError() != null) {
                log.error("Failed to broadcast transaction: {}", response.getError().getMessage());
                return null;
            }
            
            String signature = response.getResult().getValue();
            log.info("Transaction broadcast successful. Signature: {}", signature);
            return signature;
        } catch (Exception e) {
            log.error("Error broadcasting transaction", e);
            return null;
        }
    }

    @Override
    public String getTransactionStatus(String transactionHash) {
        try {
            JsonRequest request = new JsonRequest("getSignatureStatuses", 
                Arrays.asList(Arrays.asList(transactionHash), Map.of("searchTransactionHistory", true)));
            String response = sendRequestString(request);
            
            // 간단한 파싱
            if (response.contains("\"err\":")) {
                return "FAILED";
            }
            if (response.contains("\"confirmed\"") || response.contains("\"finalized\"")) {
                return "CONFIRMED";
            }
            return "PENDING";
        } catch (Exception e) {
            log.error("Error getting transaction status for hash: {}", transactionHash, e);
            return "FAILED";
        }
    }

    @Override
    public String getTransactionReceipt(String transactionHash) {
        try {
            JsonRequest request = new JsonRequest("getTransaction", 
                Arrays.asList(transactionHash, Map.of("encoding", "json", "maxSupportedTransactionVersion", 0)));
            SolanaRpcResponse<SolanaTransaction> response = sendRequest(request, new TypeReference<SolanaRpcResponse<SolanaTransaction>>() {});
            
            if (response.getError() != null) {
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
            JsonRequest request = new JsonRequest("getBlock", 
                Arrays.asList(Long.parseLong(blockHash), Map.of("encoding", "json", "maxSupportedTransactionVersion", 0)));
            SolanaRpcResponse<SolanaBlockResult> response = sendRequest(request, new TypeReference<SolanaRpcResponse<SolanaBlockResult>>() {});
            
            if (response.getError() != null) {
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
            JsonRequest request = new JsonRequest("getBlock", 
                Arrays.asList(Long.parseLong(blockNumber), Map.of("encoding", "json", "maxSupportedTransactionVersion", 0)));
            String response = sendRequestString(request);
            return response;
        } catch (Exception e) {
            log.error("Error getting block by number: {}", blockNumber, e);
            return null;
        }
    }

    @Override
    public String getNetworkStatus() {
        try {
            JsonRequest request = new JsonRequest("getHealth", Arrays.asList());
            SolanaRpcResponse<String> response = sendRequest(request, new TypeReference<SolanaRpcResponse<String>>() {});
            
            if (response.getError() != null) {
                log.error("Failed to get network status: {}", response.getError().getMessage());
                return "UNHEALTHY";
            }
            
            return response.getResult();
        } catch (Exception e) {
            log.error("Error getting network status", e);
            return "UNHEALTHY";
        }
    }

    @Override
    public String getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public String getChainId() {
        return CHAIN_ID;
    }

    @Override
    public String getLatestBlockNumber() {
        try {
            JsonRequest request = new JsonRequest("getSlot", Arrays.asList());
            String response = sendRequestString(request);
            return response; // 실제 구현에서는 파싱 필요
        } catch (Exception e) {
            log.error("Error getting latest block number", e);
            return "0";
        }
    }

    @Override
    public String calculateTransactionFee(String gasPrice, String gasLimit) {
        return "5000"; // 고정 수수료
    }

    @Override
    public String estimateGas(String fromAddress, String toAddress, String data) {
        // Solana는 가스 한도가 고정되어 있음
        return "200000"; // 기본 가스 한도
    }

    @Override
    public String createTokenTransferData(String toAddress, String amount) {
        // 실제 Solana SPL 토큰 전송 명령어 데이터 생성
        // 이는 복잡한 바이너리 데이터이므로 실제 구현에서는 Solana SDK 필요
        return "transfer_instruction_data_" + toAddress + "_" + amount;
    }

    @Override
    public String createTokenApproveData(String spender, String amount) {
        return "approve_instruction_data_" + spender + "_" + amount;
    }

    @Override
    public String createTokenMintData(String toAddress, String amount) {
        return "mint_instruction_data_" + toAddress + "_" + amount;
    }

    @Override
    public String createTokenBurnData(String amount) {
        return "burn_instruction_data_" + amount;
    }

    @Override
    public String createTokenStakeData(String amount) {
        return "stake_instruction_data_" + amount;
    }

    @Override
    public String createTokenUnstakeData(String amount) {
        return "unstake_instruction_data_" + amount;
    }

    @Override
    public String createProposalData(String proposalId, String title, String description, String proposalFee) {
        return "create_proposal_instruction_data_" + proposalId + "_" + title + "_" + description + "_" + proposalFee;
    }

    @Override
    public String createVoteData(String proposalId, String voteType, String votingPower, String reason) {
        return "vote_instruction_data_" + proposalId + "_" + voteType + "_" + votingPower + "_" + reason;
    }

    private <T> T sendRequest(JsonRequest request, TypeReference<T> typeReference) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(DEVNET_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("HTTP request failed with status: " + response.statusCode());
        }
        
        return objectMapper.readValue(response.body(), typeReference);
    }

    private String sendRequestString(JsonRequest request) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(DEVNET_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("HTTP request failed with status: " + response.statusCode());
        }
        
        return response.body();
    }
} 