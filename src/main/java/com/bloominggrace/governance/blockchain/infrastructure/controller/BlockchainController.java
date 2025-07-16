package com.bloominggrace.governance.blockchain.infrastructure.controller;

import com.bloominggrace.governance.blockchain.application.service.BlockchainApplicationService;
import com.bloominggrace.governance.blockchain.infrastructure.controller.dto.BlockchainResponse;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    private final BlockchainApplicationService blockchainApplicationService;
    private final ObjectMapper objectMapper;

    public BlockchainController(BlockchainApplicationService blockchainApplicationService) {
        this.blockchainApplicationService = blockchainApplicationService;
        this.objectMapper = new ObjectMapper();
    }

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
                                                                    @RequestParam String networkType) {
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
                                                                         @RequestParam String networkType) {
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
                                                                    @RequestParam String networkType) {
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
                                                                           @RequestParam String networkType) {
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
    public ResponseEntity<BlockchainResponse<String>> getGasPrice(@RequestParam String networkType) {
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
                                                                 @RequestParam String networkType) {
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
                                                              @RequestParam String networkType) {
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
    public ResponseEntity<BlockchainResponse<String>> getNetworkStatus(@RequestParam String networkType) {
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
    public ResponseEntity<BlockchainResponse<String>> getLatestBlockNumber(@RequestParam String networkType) {
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
    public ResponseEntity<BlockchainResponse<String>> getLatestBlockHash(@RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String blockHash = blockchainApplicationService.getLatestBlockHash(type);
            return ResponseEntity.ok(BlockchainResponse.success(blockHash));
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
                                                                    @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String block = blockchainApplicationService.getBlockByHash(blockHash, type);
            return ResponseEntity.ok(BlockchainResponse.success(block));
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
                                                                      @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String block = blockchainApplicationService.getBlockByNumber(blockNumber, type);
            return ResponseEntity.ok(BlockchainResponse.success(block));
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
    public ResponseEntity<BlockchainResponse<String>> getNetworkId(@RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String networkId = blockchainApplicationService.getNetworkId(type);
            return ResponseEntity.ok(BlockchainResponse.success(networkId));
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
    public ResponseEntity<BlockchainResponse<String>> getChainId(@RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String chainId = blockchainApplicationService.getChainId(type);
            return ResponseEntity.ok(BlockchainResponse.success(chainId));
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
                                                                             @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String fee = blockchainApplicationService.calculateTransactionFee(gasPrice, gasLimit, type);
            return ResponseEntity.ok(BlockchainResponse.success(fee));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error("Invalid network type"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BlockchainResponse.error(e.getMessage()));
        }
    }
} 