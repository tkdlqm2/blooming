package com.bloominggrace.governance.blockchain.infrastructure.controller;

import com.bloominggrace.governance.blockchain.application.service.BlockchainApplicationService;
import com.bloominggrace.governance.blockchain.infrastructure.controller.dto.BlockchainResponse;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/blockchain/{networkType}")
@RequiredArgsConstructor
@Slf4j
public class BlockchainController {

    private final BlockchainApplicationService blockchainApplicationService;
    private final ObjectMapper objectMapper;

    /**
     * Solana JSON-RPC 응답을 파싱하여 실제 결과값만 추출합니다.
     *
     * @param jsonResponse JSON-RPC 응답 문자열
     * @return 파싱된 결과값
     */
    private String parseSolanaResponse(String jsonResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            if (jsonNode.has("result")) {
                return jsonNode.get("result").asText();
            }
            return jsonResponse;
        } catch (Exception e) {
            return jsonResponse;
        }
    }

    /**
     * 네트워크 타입에 따라 응답을 적절히 파싱합니다.
     * 
     * @param response 원본 응답
     * @param networkType 네트워크 타입
     * @return 파싱된 응답
     */
    private String parseResponseByNetwork(String response, NetworkType networkType) {
        if (networkType == NetworkType.SOLANA) {
            return parseSolanaResponse(response);
        }
        return response;
    }

    /**
     * 공통 응답 처리 메서드
     */
    private ResponseEntity<BlockchainResponse<String>> handleResponse(String result, NetworkType networkType) {
        String parsedResponse = parseResponseByNetwork(result, networkType);
        return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
    }

    /**
     * 공통 에러 처리 메서드
     */
    private ResponseEntity<BlockchainResponse<String>> handleError(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        }
        return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
    }

    /**
     * 네트워크 타입 변환 및 공통 처리
     */
    private ResponseEntity<BlockchainResponse<String>> executeWithNetworkType(String networkType, 
                                                                             java.util.function.Function<NetworkType, String> operation) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String result = operation.apply(type);
            return handleResponse(result, type);
        } catch (Exception e) {
            return handleError(e);
        }
    }

    /**
     * 잔액을 조회합니다.
     * 
     * @param address 지갑 주소
     * @param networkType 네트워크 타입
     * @return 잔액
     */
    @GetMapping("/balance")
    public ResponseEntity<BlockchainResponse<String>> getBalance(@RequestParam String address,
                                                                @PathVariable String networkType) {
        return executeWithNetworkType(networkType, 
            type -> blockchainApplicationService.getBalance(address, type).toString());
    }

    /**
     * 토큰 잔액을 조회합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param tokenAddress 토큰 컨트랙트 주소
     * @param networkType 네트워크 타입
     * @return 토큰 잔액
     */
    @GetMapping("/token-balance")
    public ResponseEntity<BlockchainResponse<String>> getTokenBalance(@RequestParam String walletAddress,
                                                                     @RequestParam String tokenAddress,
                                                                     @PathVariable String networkType) {
        return executeWithNetworkType(networkType, 
            type -> blockchainApplicationService.getTokenBalance(walletAddress, tokenAddress, type).toString());
    }

    /**
     * 트랜잭션을 조회합니다.
     * 
     * @param txHash 트랜잭션 해시
     * @param networkType 네트워크 타입
     * @return 트랜잭션 정보
     */
    @GetMapping("/transaction")
    public ResponseEntity<BlockchainResponse<String>> getTransaction(@RequestParam String txHash,
                                                                    @PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            Optional<String> transactionStatus = blockchainApplicationService.getTransaction(txHash, type);
            return transactionStatus.map(status -> {
                String parsedResponse = parseResponseByNetwork(status, type);
                return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
            }).orElse(ResponseEntity.ok(BlockchainResponse.error("Transaction not found")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 트랜잭션 영수증을 조회합니다.
     * 
     * @param txHash 트랜잭션 해시
     * @param networkType 네트워크 타입
     * @return 트랜잭션 영수증
     */
    @GetMapping("/transaction-receipt")
    public ResponseEntity<BlockchainResponse<String>> getTransactionReceipt(@RequestParam String txHash,
                                                                           @PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            Optional<String> receipt = blockchainApplicationService.getTransactionReceipt(txHash, type);
            return receipt.map(r -> {
                String parsedResponse = parseResponseByNetwork(r, type);
                return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
            }).orElse(ResponseEntity.ok(BlockchainResponse.error("Transaction receipt not found")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 가스 가격을 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 가스 가격
     */
    @GetMapping("/gas-price")
    public ResponseEntity<BlockchainResponse<String>> getGasPrice(@PathVariable String networkType) {
        return executeWithNetworkType(networkType, 
            type -> blockchainApplicationService.getGasPrice(type).toString());
    }

    /**
     * 계정의 nonce를 조회합니다.
     * 
     * @param address 계정 주소
     * @param networkType 네트워크 타입
     * @return nonce 값
     */
    @GetMapping("/nonce")
    public ResponseEntity<BlockchainResponse<String>> getNonce(@RequestParam String address,
                                                              @PathVariable String networkType) {
        return executeWithNetworkType(networkType, 
            type -> blockchainApplicationService.getNonce(address, type));
    }

    /**
     * 최신 블록 번호를 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 최신 블록 번호
     */
    @GetMapping("/latest-block-number")
    public ResponseEntity<BlockchainResponse<String>> getLatestBlockNumber(@PathVariable String networkType) {
        return executeWithNetworkType(networkType, 
            type -> blockchainApplicationService.getLatestBlockNumber(type));
    }

    /**
     * 최신 블록 해시를 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 최신 블록 해시
     */
    @GetMapping("/latest-block-hash")
    public ResponseEntity<BlockchainResponse<String>> getLatestBlockHash(@PathVariable String networkType) {
        return executeWithNetworkType(networkType, 
            type -> blockchainApplicationService.getLatestBlockHash(type));
    }

    /**
     * 블록 정보를 조회합니다.
     * 
     * @param blockHash 블록 해시
     * @param networkType 네트워크 타입
     * @return 블록 정보
     */
    @GetMapping("/block")
    public ResponseEntity<BlockchainResponse<String>> getBlockByHash(@RequestParam String blockHash,
                                                                    @PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String block = blockchainApplicationService.getBlockByHash(blockHash, type);
            String parsedResponse = parseResponseByNetwork(block, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 블록 번호로 블록 정보를 조회합니다.
     * 
     * @param blockNumber 블록 번호
     * @param networkType 네트워크 타입
     * @return 블록 정보
     */
    @GetMapping("/block-by-number")
    public ResponseEntity<BlockchainResponse<String>> getBlockByNumber(@RequestParam String blockNumber,
                                                                      @PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String block = blockchainApplicationService.getBlockByNumber(blockNumber, type);
            String parsedResponse = parseResponseByNetwork(block, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }
} 