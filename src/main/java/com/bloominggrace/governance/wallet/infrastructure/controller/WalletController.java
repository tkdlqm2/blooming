package com.bloominggrace.governance.wallet.infrastructure.controller;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.application.dto.CreateWalletRequest;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.BlockchainClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletApplicationService walletApplicationService;

    public WalletController(WalletApplicationService walletApplicationService) {
        this.walletApplicationService = walletApplicationService;
    }

    /**
     * 새로운 지갑을 생성합니다.
     * 
     * @param request 지갑 생성 요청
     * @return 생성된 지갑 정보
     */
    @PostMapping
    public ResponseEntity<WalletDto> createWallet(@RequestBody CreateWalletRequest request) {
        try {
            WalletDto wallet = walletApplicationService.createWallet(request);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 네이티브 토큰을 전송합니다.
     * 
     * @param fromAddress 송금 지갑 주소
     * @param toAddress 수신 지갑 주소
     * @param amount 전송 금액
     * @param networkType 네트워크 타입
     * @return 트랜잭션 해시
     */
    @PostMapping("/send-transaction")
    public ResponseEntity<String> sendTransaction(@RequestParam String fromAddress,
                                                 @RequestParam String toAddress,
                                                 @RequestParam BigDecimal amount,
                                                 @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String transactionHash = walletApplicationService.sendTransaction(fromAddress, toAddress, amount, type);
            return ResponseEntity.ok(transactionHash);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("트랜잭션 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 ID와 네트워크를 기반으로 네이티브 토큰을 전송합니다.
     * 
     * @param userId 사용자 ID
     * @param toAddress 수신 지갑 주소
     * @param amount 전송 금액
     * @param networkType 네트워크 타입
     * @return 트랜잭션 해시
     */
    @PostMapping("/send-transaction-by-user")
    public ResponseEntity<String> sendTransactionByUser(@RequestParam String userId,
                                                       @RequestParam String toAddress,
                                                       @RequestParam BigDecimal amount,
                                                       @RequestParam String networkType) {
        try {
            UserId user = new UserId(UUID.fromString(userId));
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            
            // 사용자의 지갑 주소를 가져옵니다
            List<Wallet> userWallets = walletApplicationService.findByUserId(user);
            Wallet userWallet = userWallets.stream()
                    .filter(w -> w.getNetworkType() == type)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("사용자의 해당 네트워크 지갑을 찾을 수 없습니다"));
            
            String transactionHash = walletApplicationService.sendTransaction(
                userWallet.getWalletAddress(), toAddress, amount, type);
            return ResponseEntity.ok(transactionHash);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("트랜잭션 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 토큰을 전송합니다.
     * 
     * @param fromAddress 송금 지갑 주소
     * @param toAddress 수신 지갑 주소
     * @param tokenAddress 토큰 컨트랙트 주소
     * @param amount 전송 금액
     * @param networkType 네트워크 타입
     * @return 트랜잭션 해시
     */
    @PostMapping("/send-token")
    public ResponseEntity<String> sendToken(@RequestParam String fromAddress,
                                           @RequestParam String toAddress,
                                           @RequestParam String tokenAddress,
                                           @RequestParam BigDecimal amount,
                                           @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            String transactionHash = walletApplicationService.sendToken(fromAddress, toAddress, tokenAddress, amount, type);
            return ResponseEntity.ok(transactionHash);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("토큰 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 ID와 네트워크를 기반으로 토큰을 전송합니다.
     * 
     * @param userId 사용자 ID
     * @param toAddress 수신 지갑 주소
     * @param tokenAddress 토큰 컨트랙트 주소
     * @param amount 전송 금액
     * @param networkType 네트워크 타입
     * @return 트랜잭션 해시
     */
    @PostMapping("/send-token-by-user")
    public ResponseEntity<String> sendTokenByUser(@RequestParam String userId,
                                                 @RequestParam String toAddress,
                                                 @RequestParam String tokenAddress,
                                                 @RequestParam BigDecimal amount,
                                                 @RequestParam String networkType) {
        try {
            UserId user = new UserId(UUID.fromString(userId));
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            
            // 사용자의 지갑 주소를 가져옵니다
            List<Wallet> userWallets = walletApplicationService.findByUserId(user);
            Wallet userWallet = userWallets.stream()
                    .filter(w -> w.getNetworkType() == type)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("사용자의 해당 네트워크 지갑을 찾을 수 없습니다"));
            
            String transactionHash = walletApplicationService.sendToken(
                userWallet.getWalletAddress(), toAddress, tokenAddress, amount, type);
            return ResponseEntity.ok(transactionHash);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("토큰 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 메시지를 서명합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param message 서명할 메시지 (Base64 인코딩)
     * @param networkType 네트워크 타입
     * @return 서명 결과 (Base64 인코딩)
     */
    @PostMapping("/sign-message")
    public ResponseEntity<String> signMessage(@RequestParam String walletAddress,
                                             @RequestParam String message,
                                             @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            byte[] messageBytes = java.util.Base64.getDecoder().decode(message);
            byte[] signature = walletApplicationService.signMessage(walletAddress, messageBytes, type);
            String signatureBase64 = java.util.Base64.getEncoder().encodeToString(signature);
            return ResponseEntity.ok(signatureBase64);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("메시지 서명 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 ID와 네트워크를 기반으로 메시지를 서명합니다.
     * 
     * @param userId 사용자 ID
     * @param message 서명할 메시지 (Base64 인코딩)
     * @param networkType 네트워크 타입
     * @return 서명 결과 (Base64 인코딩)
     */
    @PostMapping("/sign-message-by-user")
    public ResponseEntity<String> signMessageByUser(@RequestParam String userId,
                                                   @RequestParam String message,
                                                   @RequestParam String networkType) {
        try {
            UserId user = new UserId(UUID.fromString(userId));
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            byte[] messageBytes = java.util.Base64.getDecoder().decode(message);
            byte[] signature = walletApplicationService.signMessageByUser(user, messageBytes, type);
            String signatureBase64 = java.util.Base64.getEncoder().encodeToString(signature);
            return ResponseEntity.ok(signatureBase64);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("메시지 서명 실패: " + e.getMessage());
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
    public ResponseEntity<BigDecimal> getBalance(@RequestParam String address,
                                                @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            BigDecimal balance = walletApplicationService.getBalance(address, type);
            return ResponseEntity.ok(balance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<BigDecimal> getTokenBalance(@RequestParam String walletAddress,
                                                     @RequestParam String tokenAddress,
                                                     @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            BigDecimal balance = walletApplicationService.getTokenBalance(walletAddress, tokenAddress, type);
            return ResponseEntity.ok(balance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<BlockchainClient.TransactionInfo> getTransaction(@RequestParam String txHash,
                                                @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            var transaction = walletApplicationService.getTransaction(txHash, type);
            return transaction.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 가스 가격을 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 가스 가격
     */
    @GetMapping("/gas-price")
    public ResponseEntity<BigDecimal> getGasPrice(@RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            BigDecimal gasPrice = walletApplicationService.getGasPrice(type);
            return ResponseEntity.ok(gasPrice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 모든 지갑을 조회합니다.
     * 
     * @return 지갑 목록
     */
    @GetMapping
    public ResponseEntity<List<WalletDto>> getAllWallets() {
        try {
            List<Wallet> wallets = walletApplicationService.findAll();
            List<WalletDto> walletDtos = wallets.stream()
                    .map(WalletDto::from)
                    .toList();
            return ResponseEntity.ok(walletDtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 지갑을 조회합니다.
     * 
     * @param id 지갑 ID
     * @return 지갑 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<WalletDto> getWalletById(@PathVariable UUID id) {
        try {
            var wallet = walletApplicationService.findById(id);
            return wallet.map(w -> ResponseEntity.ok(WalletDto.from(w)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 ID로 지갑 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 지갑 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletDto>> getWalletsByUserId(@PathVariable String userId) {
        try {
            UserId user = new UserId(UUID.fromString(userId));
            List<Wallet> wallets = walletApplicationService.findByUserId(user);
            List<WalletDto> walletDtos = wallets.stream()
                    .map(WalletDto::from)
                    .toList();
            return ResponseEntity.ok(walletDtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 네트워크 타입으로 지갑 목록을 조회합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 지갑 목록
     */
    @GetMapping("/network/{networkType}")
    public ResponseEntity<List<WalletDto>> getWalletsByNetworkType(@PathVariable String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            List<Wallet> wallets = walletApplicationService.findByNetworkType(type);
            List<WalletDto> walletDtos = wallets.stream()
                    .map(WalletDto::from)
                    .toList();
            return ResponseEntity.ok(walletDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 지갑을 삭제합니다.
     * 
     * @param id 지갑 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable UUID id) {
        try {
            walletApplicationService.deleteWallet(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 지갑 활성화 상태를 변경합니다.
     * 
     * @param walletAddress 지갑 주소
     * @param active 활성화 여부
     * @param networkType 네트워크 타입
     * @return 업데이트된 지갑 정보
     */
    @PutMapping("/{walletAddress}/active")
    public ResponseEntity<WalletDto> updateWalletActiveStatus(@PathVariable String walletAddress,
                                                             @RequestParam boolean active,
                                                             @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            Wallet wallet = walletApplicationService.updateWalletActiveStatus(walletAddress, active, type);
            return ResponseEntity.ok(WalletDto.from(wallet));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 주소 유효성을 검증합니다.
     * 
     * @param address 검증할 주소
     * @param networkType 네트워크 타입
     * @return 유효성 여부
     */
    @GetMapping("/validate-address")
    public ResponseEntity<Boolean> validateAddress(@RequestParam String address,
                                                  @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            boolean isValid = walletApplicationService.validateAddress(address, type);
            return ResponseEntity.ok(isValid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 네트워크 연결 상태를 확인합니다.
     * 
     * @param networkType 네트워크 타입
     * @return 연결 상태
     */
    @GetMapping("/network-status")
    public ResponseEntity<Boolean> getNetworkStatus(@RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            boolean isConnected = walletApplicationService.isNetworkConnected(type);
            return ResponseEntity.ok(isConnected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 