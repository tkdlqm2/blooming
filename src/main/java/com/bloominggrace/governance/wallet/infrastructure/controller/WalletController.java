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

//    /**
//     * 3단계: signedRawTransaction 브로드캐스트
//     */
//    @PostMapping("/broadcast-transaction")
//    public ResponseEntity<String> broadcastTransaction(
//            @RequestBody SignedRawTransactionRequest request) {
//        try {
//            // Base64 디코딩을 더 안전하게 처리
//            String signedRawTransactionBase64 = request.getSignedRawTransaction();
//            if (signedRawTransactionBase64 == null || signedRawTransactionBase64.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body("브로드캐스트 실패: signedRawTransaction이 비어있습니다.");
//            }
//
//            // Base64 패딩 추가 (필요한 경우)
//            while (signedRawTransactionBase64.length() % 4 != 0) {
//                signedRawTransactionBase64 += "=";
//            }
//
//            byte[] signedRawTransaction;
//            try {
//                signedRawTransaction = java.util.Base64.getDecoder().decode(signedRawTransactionBase64);
//            } catch (IllegalArgumentException e) {
//                return ResponseEntity.badRequest().body("브로드캐스트 실패: 잘못된 Base64 형식입니다. " + e.getMessage());
//            }
//
//            String transactionHash = walletApplicationService.broadcastSignedTransaction(
//                signedRawTransaction, request.getNetworkType());
//
//            return ResponseEntity.ok(transactionHash);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("브로드캐스트 실패: " + e.getMessage());
//        }
//    }

    /**
     * 바이트 배열을 hex 문자열로 변환하는 헬퍼 메서드
     */
//    private String bytesToHex(byte[] bytes) {
//        StringBuilder result = new StringBuilder();
//        for (byte b : bytes) {
//            result.append(String.format("%02x", b));
//        }
//        return result.toString();
//    }
//
//    // ===== DTO 클래스들 =====
//
//    public static class TransactionBodyResponse {
//        private final UUID transactionId;
//        private final String type;
//        private final String fromAddress;
//        private final String toAddress;
//        private final String data;
//        private final String networkType;
//        private final long nonce;
//
//        public TransactionBodyResponse(UUID transactionId, String type, String fromAddress,
//                                     String toAddress, String data, String networkType, long nonce) {
//            this.transactionId = transactionId;
//            this.type = type;
//            this.fromAddress = fromAddress;
//            this.toAddress = toAddress;
//            this.data = data;
//            this.networkType = networkType;
//            this.nonce = nonce;
//        }
//
//        public static <T> TransactionBodyResponse from(TransactionBody<T> transactionBody) {
//            return new TransactionBodyResponse(
//                transactionBody.getTransactionId(),
//                transactionBody.getType().name(),
//                transactionBody.getFromAddress(),
//                transactionBody.getToAddress(),
//                transactionBody.getData(),
//                transactionBody.getNetworkType(),
//                transactionBody.getNonce()
//            );
//        }
//
//        // Getters
//        public UUID getTransactionId() { return transactionId; }
//        public String getType() { return type; }
//        public String getFromAddress() { return fromAddress; }
//        public String getToAddress() { return toAddress; }
//        public String getData() { return data; }
//        public String getNetworkType() { return networkType; }
//        public long getNonce() { return nonce; }
//    }
//
//    public static class TransactionBodyRequest {
//        private String type;
//        private String fromAddress;
//        private String toAddress;
//        private String data;
//        private String networkType;
//        private String walletAddress;
//
//        public TransactionBody<Object> toTransactionBody() {
//            // 네트워크별 특정 데이터 생성 (간단한 구현)
//            Object networkSpecificData = createNetworkSpecificData(networkType, toAddress);
//
//            return TransactionBody.builder()
//                .type(TransactionBody.TransactionType.valueOf(type))
//                .fromAddress(fromAddress)
//                .toAddress(toAddress)
//                .data(data)
//                .networkType(networkType)
//                .networkSpecificData(networkSpecificData)
//                .build();
//        }
//
//        private Object createNetworkSpecificData(String networkType, String toAddress) {
//            switch (networkType.toUpperCase()) {
//                case "ETHEREUM":
//                    return new com.bloominggrace.governance.shared.domain.model.EthereumTransactionData(
//                        java.math.BigInteger.valueOf(20000000000L),
//                        java.math.BigInteger.valueOf(21000L),
//                        java.math.BigInteger.ZERO,
//                        toAddress,
//                        toAddress,
//                        null,
//                        java.math.BigInteger.ZERO
//                    );
//                case "SOLANA":
//                    return com.bloominggrace.governance.shared.domain.model.SolanaTransactionData.builder()
//                        .recentBlockhash("11111111111111111111111111111111")
//                        .fee(5000L)
//                        .programId(null)
//                        .build();
//                default:
//                    throw new IllegalArgumentException("Unsupported network type: " + networkType);
//            }
//        }
//
//        // Getters and Setters
//        public String getType() { return type; }
//        public void setType(String type) { this.type = type; }
//        public String getFromAddress() { return fromAddress; }
//        public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
//        public String getToAddress() { return toAddress; }
//        public void setToAddress(String toAddress) { this.toAddress = toAddress; }
//        public String getData() { return data; }
//        public void setData(String data) { this.data = data; }
//        public String getNetworkType() { return networkType; }
//        public void setNetworkType(String networkType) { this.networkType = networkType; }
//        public String getWalletAddress() { return walletAddress; }
//        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
//    }
//
//    public static class SignedTransactionResponse {
//        private final UUID signedTransactionId;
//        private final TransactionBodyResponse transactionBody;
//        private final String signature; // Base64 encoded
//        private final String signerAddress;
//        private final String status;
//        private final String transactionHash;
//
//        public SignedTransactionResponse(UUID signedTransactionId, TransactionBodyResponse transactionBody,
//                                       String signature, String signerAddress, String status, String transactionHash) {
//            this.signedTransactionId = signedTransactionId;
//            this.transactionBody = transactionBody;
//            this.signature = signature;
//            this.signerAddress = signerAddress;
//            this.status = status;
//            this.transactionHash = transactionHash;
//        }
//
//        public static <T> SignedTransactionResponse from(SignedTransaction<T> signedTransaction) {
//            return new SignedTransactionResponse(
//                signedTransaction.getSignedTransactionId(),
//                TransactionBodyResponse.from(signedTransaction.getTransactionBody()),
//                com.bloominggrace.governance.shared.util.SignatureUtils.encodeBase64(signedTransaction.getSignature()),
//                signedTransaction.getSignerAddress(),
//                signedTransaction.getStatus().name(),
//                signedTransaction.getTransactionHash()
//            );
//        }
//
//        // Getters
//        public UUID getSignedTransactionId() { return signedTransactionId; }
//        public TransactionBodyResponse getTransactionBody() { return transactionBody; }
//        public String getSignature() { return signature; }
//        public String getSignerAddress() { return signerAddress; }
//        public String getStatus() { return status; }
//        public String getTransactionHash() { return transactionHash; }
//    }
//
//    public static class SignedRawTransactionResponse {
//        private final String signedRawTransaction; // Base64 encoded
//        private final String networkType;
//
//        public SignedRawTransactionResponse(String signedRawTransaction, String networkType) {
//            this.signedRawTransaction = signedRawTransaction;
//            this.networkType = networkType;
//        }
//
//        public static SignedRawTransactionResponseBuilder builder() {
//            return new SignedRawTransactionResponseBuilder();
//        }
//
//        public static class SignedRawTransactionResponseBuilder {
//            private String signedRawTransaction;
//            private String networkType;
//
//            public SignedRawTransactionResponseBuilder signedRawTransaction(String signedRawTransaction) {
//                this.signedRawTransaction = signedRawTransaction;
//                return this;
//            }
//
//            public SignedRawTransactionResponseBuilder networkType(String networkType) {
//                this.networkType = networkType;
//                return this;
//            }
//
//            public SignedRawTransactionResponse build() {
//                return new SignedRawTransactionResponse(signedRawTransaction, networkType);
//            }
//        }
//
//        // Getters
//        public String getSignedRawTransaction() { return signedRawTransaction; }
//        public String getNetworkType() { return networkType; }
//    }
//
//    public static class SignedRawTransactionRequest {
//        private String signedRawTransaction; // Base64 encoded
//        private String networkType;
//
//        // Getters and Setters
//        public String getSignedRawTransaction() { return signedRawTransaction; }
//        public void setSignedRawTransaction(String signedRawTransaction) { this.signedRawTransaction = signedRawTransaction; }
//        public String getNetworkType() { return networkType; }
//        public void setNetworkType(String networkType) { this.networkType = networkType; }
//    }
}