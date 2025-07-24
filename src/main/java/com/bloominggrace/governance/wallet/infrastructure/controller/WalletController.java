package com.bloominggrace.governance.wallet.infrastructure.controller;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.infrastructure.service.JwtService;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.application.dto.CreateWalletRequest;
import com.bloominggrace.governance.wallet.application.dto.UnlockWalletRequest;
import com.bloominggrace.governance.wallet.application.dto.UnlockWalletResponse;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.domain.model.Wallet;

import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.SignedTransaction;
import com.bloominggrace.governance.shared.domain.model.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletApplicationService walletApplicationService;
    private final JwtService jwtService;
    /**
     * 새로운 지갑을 생성합니다.
     *
     * @param request 지갑 생성 요청
     * @return 생성된 지갑 정보
     */
    @PostMapping
    public ResponseEntity<?> createWallet(@RequestBody CreateWalletRequest request) {
        try {
            System.out.println("Creating wallet for user: " + request.getUserId() + ", network: " + request.getNetworkType());
            WalletDto wallet = walletApplicationService.createWallet(request);
            System.out.println("Wallet created successfully: " + wallet.getWalletAddress());
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            System.err.println("Error creating wallet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
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
            Optional<Wallet> wallet = walletApplicationService.findById(id);
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
    public ResponseEntity<List<WalletDto>> getWalletsByUserId(@PathVariable String userId,
                                                             @RequestHeader("Authorization") String token) {
        try {
            // "Bearer " 접두사 제거
            String actualToken = token.replace("Bearer ", "");
            UUID jwtUserId = jwtService.getUserIdFromToken(actualToken);

            // JWT의 userId와 요청의 userId가 일치하는지 확인
            if (!jwtUserId.toString().equals(userId)) {
                return ResponseEntity.status(403).build();
            }

            UserId user = new UserId(UUID.fromString(userId));
            List<Wallet> wallets = walletApplicationService.findByUserId(user);
            List<WalletDto> walletDtos = wallets.stream()
                    .map(WalletDto::from)
                    .toList();
            return ResponseEntity.ok(walletDtos);
        } catch (Exception e) {
            System.err.println("Error getting wallets by user ID: " + e.getMessage());
            e.printStackTrace();
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
     * 지갑을 잠금 해제하고 프라이빗 키의 유효성을 검증합니다.
     * 프라이빗 키를 복호화하여 다시 주소를 생성하고, 요청된 주소와 일치하는지 확인합니다.
     *
     * @param request 지갑 잠금 해제 요청
     * @return 잠금 해제 결과
     */
    @PostMapping("/unlock")
    public ResponseEntity<UnlockWalletResponse> unlockWallet(@RequestBody UnlockWalletRequest request) {
        try {
            System.out.println("Unlocking wallet: " + request.getWalletAddress() + " on network: " + request.getNetworkType());

            NetworkType networkType = NetworkType.valueOf(request.getNetworkType().toUpperCase());
            WalletService.UnlockResult result = walletApplicationService.unlockWallet(request.getWalletAddress(), networkType);

            UnlockWalletResponse response = UnlockWalletResponse.from(result, request.getNetworkType());

            if (result.isSuccess()) {
                System.out.println("✅ Wallet unlock successful: " + request.getWalletAddress());
                return ResponseEntity.ok(response);
            } else {
                System.out.println("❌ Wallet unlock failed: " + request.getWalletAddress() + " - " + result.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid network type: " + request.getNetworkType());
            return ResponseEntity.badRequest().body(UnlockWalletResponse.from(
                WalletService.UnlockResult.failure(request.getWalletAddress(), "Invalid network type: " + request.getNetworkType()),
                request.getNetworkType()
            ));
        } catch (Exception e) {
            System.err.println("Error unlocking wallet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(UnlockWalletResponse.from(
                WalletService.UnlockResult.failure(request.getWalletAddress(), "Internal server error: " + e.getMessage()),
                request.getNetworkType()
            ));
        }
    }
}