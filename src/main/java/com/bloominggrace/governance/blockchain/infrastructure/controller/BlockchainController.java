package com.bloominggrace.governance.blockchain.infrastructure.controller;

import com.bloominggrace.governance.blockchain.application.service.BlockchainApplicationService;
import com.bloominggrace.governance.blockchain.infrastructure.controller.dto.BlockchainResponse;
import com.bloominggrace.governance.shared.infrastructure.service.JwtService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator;

@RestController
@RequestMapping("/api/blockchain/{networkType}")
@RequiredArgsConstructor
@Slf4j
public class BlockchainController {

    private final BlockchainApplicationService blockchainApplicationService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final TransactionOrchestrator transactionOrchestrator;

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
     * 잔액을 조회합니다.
     * 
     * @param address 지갑 주소
     * @param networkType 네트워크 타입
     * @return 잔액
     */
    @GetMapping("/balance")
    public ResponseEntity<BlockchainResponse<String>> getBalance(@RequestParam String address,
                                                                @PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String balance = blockchainApplicationService.getBalance(address, type).toString();
            String parsedResponse = parseResponseByNetwork(balance, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
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
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String balance = blockchainApplicationService.getTokenBalance(walletAddress, tokenAddress, type).toString();
            String parsedResponse = parseResponseByNetwork(balance, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
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
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String gasPrice = blockchainApplicationService.getGasPrice(type).toString();
            String parsedResponse = parseResponseByNetwork(gasPrice, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 가스 한도를 추정합니다.
     * 
     * @param fromAddress 발신자 주소
     * @param toAddress 수신자 주소
     * @param data 트랜잭션 데이터
     * @param networkType 네트워크 타입
     * @return 가스 한도
     */
    @GetMapping("/estimate-gas")
    public ResponseEntity<BlockchainResponse<String>> estimateGas(@RequestParam String fromAddress,
                                                                 @RequestParam String toAddress,
                                                                 @RequestParam(required = false) String data,
                                                                 @PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String gasLimit = blockchainApplicationService.estimateGas(fromAddress, toAddress, data, type);
            String parsedResponse = parseResponseByNetwork(gasLimit, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
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
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String nonce = blockchainApplicationService.getNonce(address, type);
            String parsedResponse = parseResponseByNetwork(nonce, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 네트워크 상태를 확인합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 네트워크 상태
     */
    @GetMapping("/network-status")
    public ResponseEntity<BlockchainResponse<String>> getNetworkStatus(@PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String status = blockchainApplicationService.getNetworkStatus(type);
            return ResponseEntity.ok(BlockchainResponse.success(status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 최신 블록 번호를 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 최신 블록 번호
     */
    @GetMapping("/latest-block-number")
    public ResponseEntity<BlockchainResponse<String>> getLatestBlockNumber(@PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String blockNumber = blockchainApplicationService.getLatestBlockNumber(type);
            String parsedResponse = parseResponseByNetwork(blockNumber, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 최신 블록 해시를 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 최신 블록 해시
     */
    @GetMapping("/latest-block-hash")
    public ResponseEntity<BlockchainResponse<String>> getLatestBlockHash(@PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String blockHash = blockchainApplicationService.getLatestBlockHash(type);
            String parsedResponse = parseResponseByNetwork(blockHash, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
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

    /**
     * 네트워크 ID를 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 네트워크 ID
     */
    @GetMapping("/network-id")
    public ResponseEntity<BlockchainResponse<String>> getNetworkId(@PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String networkId = blockchainApplicationService.getNetworkId(type);
            String parsedResponse = parseResponseByNetwork(networkId, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 체인 ID를 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 체인 ID
     */
    @GetMapping("/chain-id")
    public ResponseEntity<BlockchainResponse<String>> getChainId(@PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String chainId = blockchainApplicationService.getChainId(type);
            String parsedResponse = parseResponseByNetwork(chainId, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * 트랜잭션 수수료를 계산합니다.
     * 
     * @param gasPrice 가스 가격
     * @param gasLimit 가스 한도
     * @param networkType 네트워크 타입
     * @return 트랜잭션 수수료
     */
    @GetMapping("/transaction-fee")
    public ResponseEntity<BlockchainResponse<String>> calculateTransactionFee(@RequestParam String gasPrice,
                                                                             @RequestParam String gasLimit,
                                                                             @PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String fee = blockchainApplicationService.calculateTransactionFee(gasPrice, gasLimit, type);
            String parsedResponse = parseResponseByNetwork(fee, type);
            return ResponseEntity.ok(BlockchainResponse.success(parsedResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }

    /**
     * ERC20 토큰 전송을 수행합니다.
     * 
     * @param fromAddress 보내는 지갑 주소
     * @param toAddress 받는 지갑 주소
     * @param amount 전송할 토큰 양
     * @param tokenAddress 토큰 컨트랙트 주소
     * @param authorization JWT 토큰
     * @return 전송 결과
     */
    @PostMapping("/ethereum/token-transfer")
    public ResponseEntity<BlockchainResponse<String>> transferERC20Token(
            @RequestParam String fromAddress,
            @RequestParam String toAddress,
            @RequestParam String amount,
            @RequestParam String tokenAddress,
            @RequestHeader("Authorization") String authorization) {
        
        try {
            // JWT 토큰에서 Bearer 제거
            String token = authorization.replace("Bearer ", "");
            
            // JWT 토큰에서 사용자 정보 추출
            String userEmail = jwtService.getEmailFromToken(token);
            String userRole = jwtService.getRoleFromToken(token);
            
            log.info("ERC20 토큰 전송 요청 - From: {}, To: {}, Amount: {}, Token: {}, User: {}", 
                fromAddress, toAddress, amount, tokenAddress, userEmail);
            
            // 토큰 전송 수행 (시뮬레이션)
            String transactionHash = "0x" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 64);
            
            return ResponseEntity.ok(BlockchainResponse.success(transactionHash));
            
        } catch (Exception e) {
            // log.error("ERC20 토큰 전송 실패", e); // Original code had this line commented out
            return ResponseEntity.badRequest().body(BlockchainResponse.error("토큰 전송 실패: " + e.getMessage()));
        }
    }
} 