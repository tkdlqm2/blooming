package com.bloominggrace.governance.wallet.infrastructure.controller;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.infrastructure.service.JwtService;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.application.dto.CreateWalletRequest;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;

import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.SignedTransaction;
import com.bloominggrace.governance.shared.domain.model.TransactionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletApplicationService walletApplicationService;
    private final JwtService jwtService;

    public WalletController(WalletApplicationService walletApplicationService, JwtService jwtService) {
        this.walletApplicationService = walletApplicationService;
        this.jwtService = jwtService;
    }

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
     * 테스트용 간단한 지갑 생성 엔드포인트
     */
    @PostMapping("/test")
    public ResponseEntity<String> testWalletCreation(@RequestBody CreateWalletRequest request) {
        try {
            System.out.println("Test wallet creation for user: " + request.getUserId() + ", network: " + request.getNetworkType());
            return ResponseEntity.ok("Test endpoint reached successfully");
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
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
    // ===== 단계별 트랜잭션 처리 엔드포인트 =====

    /**
     * 1단계: 네이티브 토큰 전송용 TransactionBody 생성
     */
    @PostMapping("/transaction-body/native-transfer")
    public ResponseEntity<TransactionBodyResponse> createNativeTransferTransactionBody(
            @RequestParam String fromAddress,
            @RequestParam String toAddress,
            @RequestParam BigDecimal amount,
            @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            
            // 새로운 통합 메서드를 사용하여 트랜잭션 생성
            TransactionRequest.TokenTransferData transferData = TransactionRequest.TokenTransferData.builder().build();
            TransactionBody transactionBody = walletApplicationService.createTransactionBody(
                fromAddress, toAddress, amount, null, transferData, 
                TransactionRequest.TransactionType.TOKEN_TRANSFER, type
            );
            
            return ResponseEntity.ok(TransactionBodyResponse.from(transactionBody));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 1단계: 토큰 전송용 TransactionBody 생성
     */
    @PostMapping("/transaction-body/token-transfer")
    public ResponseEntity<TransactionBodyResponse> createTokenTransferTransactionBody(
            @RequestParam String fromAddress,
            @RequestParam String toAddress,
            @RequestParam String tokenAddress,
            @RequestParam BigDecimal amount,
            @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            
            // 새로운 통합 메서드를 사용하여 트랜잭션 생성
            TransactionRequest.TokenTransferData transferData = TransactionRequest.TokenTransferData.builder().build();
            TransactionBody transactionBody = walletApplicationService.createTransactionBody(
                fromAddress, toAddress, amount, tokenAddress, transferData, 
                TransactionRequest.TransactionType.TOKEN_TRANSFER, type
            );
            
            return ResponseEntity.ok(TransactionBodyResponse.from(transactionBody));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 1단계: 토큰 민팅용 TransactionBody 생성
     */
    @PostMapping("/transaction-body/token-mint")
    public ResponseEntity<TransactionBodyResponse> createTokenMintTransactionBody(
            @RequestParam String walletAddress,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            
            // 새로운 통합 메서드를 사용하여 트랜잭션 생성
            TransactionRequest.TokenMintData mintData = TransactionRequest.TokenMintData.builder()
                .description(description)
                .build();
            TransactionBody transactionBody = walletApplicationService.createTransactionBody(
                walletAddress, null, amount, null, mintData, 
                TransactionRequest.TransactionType.TOKEN_MINT, type
            );
            
            return ResponseEntity.ok(TransactionBodyResponse.from(transactionBody));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 1단계: 프로포잘 생성용 TransactionBody 생성
     */
    @PostMapping("/transaction-body/proposal")
    public ResponseEntity<TransactionBodyResponse> createProposalTransactionBody(
            @RequestParam String walletAddress,
            @RequestParam String proposalId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam BigDecimal proposalFee,
            @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            
            // 새로운 통합 메서드를 사용하여 트랜잭션 생성
            TransactionRequest.ProposalData proposalData = TransactionRequest.ProposalData.builder()
                .proposalId(proposalId)
                .title(title)
                .description(description)
                .proposalFee(proposalFee)
                .build();
            TransactionBody transactionBody = walletApplicationService.createTransactionBody(
                walletAddress, null, null, null, proposalData, 
                TransactionRequest.TransactionType.PROPOSAL_CREATE, type
            );
            
            return ResponseEntity.ok(TransactionBodyResponse.from(transactionBody));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 1단계: 투표용 TransactionBody 생성
     */
    @PostMapping("/transaction-body/vote")
    public ResponseEntity<TransactionBodyResponse> createVoteTransactionBody(
            @RequestParam String walletAddress,
            @RequestParam String proposalId,
            @RequestParam String voteType,
            @RequestParam BigDecimal votingPower,
            @RequestParam(required = false) String reason,
            @RequestParam String networkType) {
        try {
            NetworkType type = NetworkType.valueOf(networkType.toUpperCase());
            
            // 새로운 통합 메서드를 사용하여 트랜잭션 생성
            TransactionRequest.VoteData voteData = TransactionRequest.VoteData.builder()
                .proposalId(proposalId)
                .voteType(voteType)
                .votingPower(votingPower)
                .reason(reason)
                .build();
            TransactionBody transactionBody = walletApplicationService.createTransactionBody(
                walletAddress, null, null, null, voteData, 
                TransactionRequest.TransactionType.PROPOSAL_VOTE, type
            );
            
            return ResponseEntity.ok(TransactionBodyResponse.from(transactionBody));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 2단계: TransactionBody 서명하여 signedRawTransaction 반환
     */
    @PostMapping("/sign-transaction")
    public ResponseEntity<SignedRawTransactionResponse> signTransaction(
            @RequestBody TransactionBodyRequest request) {
        try {
            TransactionBody<Object> transactionBody = request.toTransactionBody();
            byte[] signedRawTransaction = walletApplicationService.signTransactionBody(
                transactionBody, request.getWalletAddress());
            
            return ResponseEntity.ok(SignedRawTransactionResponse.builder()
                .signedRawTransaction(java.util.Base64.getEncoder().encodeToString(signedRawTransaction))
                .networkType(request.getNetworkType())
                .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(SignedRawTransactionResponse.builder()
                .signedRawTransaction(null)
                .networkType(null)
                .build());
        }
    }

    /**
     * 3단계: signedRawTransaction 브로드캐스트
     */
    @PostMapping("/broadcast-transaction")
    public ResponseEntity<String> broadcastTransaction(
            @RequestBody SignedRawTransactionRequest request) {
        try {
            byte[] signedRawTransaction = java.util.Base64.getDecoder().decode(request.getSignedRawTransaction());
            String transactionHash = walletApplicationService.broadcastSignedTransaction(
                signedRawTransaction, request.getNetworkType());
            
            return ResponseEntity.ok(transactionHash);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("브로드캐스트 실패: " + e.getMessage());
        }
    }

    // ===== DTO 클래스들 =====

    public static class TransactionBodyResponse {
        private final UUID transactionId;
        private final String type;
        private final String fromAddress;
        private final String toAddress;
        private final String data;
        private final String networkType;
        private final long nonce;

        public TransactionBodyResponse(UUID transactionId, String type, String fromAddress, 
                                     String toAddress, String data, String networkType, long nonce) {
            this.transactionId = transactionId;
            this.type = type;
            this.fromAddress = fromAddress;
            this.toAddress = toAddress;
            this.data = data;
            this.networkType = networkType;
            this.nonce = nonce;
        }

        public static <T> TransactionBodyResponse from(TransactionBody<T> transactionBody) {
            return new TransactionBodyResponse(
                transactionBody.getTransactionId(),
                transactionBody.getType().name(),
                transactionBody.getFromAddress(),
                transactionBody.getToAddress(),
                transactionBody.getData(),
                transactionBody.getNetworkType(),
                transactionBody.getNonce()
            );
        }

        // Getters
        public UUID getTransactionId() { return transactionId; }
        public String getType() { return type; }
        public String getFromAddress() { return fromAddress; }
        public String getToAddress() { return toAddress; }
        public String getData() { return data; }
        public String getNetworkType() { return networkType; }
        public long getNonce() { return nonce; }
    }

    public static class TransactionBodyRequest {
        private String type;
        private String fromAddress;
        private String toAddress;
        private String data;
        private String networkType;
        private String walletAddress;

        public TransactionBody<Object> toTransactionBody() {
            // 네트워크별 특정 데이터 생성 (간단한 구현)
            Object networkSpecificData = createNetworkSpecificData(networkType, toAddress);
            
            return TransactionBody.builder()
                .type(TransactionBody.TransactionType.valueOf(type))
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .data(data)
                .networkType(networkType)
                .networkSpecificData(networkSpecificData)
                .build();
        }
        
        private Object createNetworkSpecificData(String networkType, String toAddress) {
            switch (networkType.toUpperCase()) {
                case "ETHEREUM":
                    return com.bloominggrace.governance.shared.domain.model.EthereumTransactionData.builder()
                        .gasPrice(java.math.BigInteger.valueOf(20000000000L))
                        .gasLimit(java.math.BigInteger.valueOf(21000L))
                        .value(java.math.BigInteger.ZERO)
                        .contractAddress(toAddress)
                        .build();
                case "SOLANA":
                    return com.bloominggrace.governance.shared.domain.model.SolanaTransactionData.builder()
                        .recentBlockhash("11111111111111111111111111111111")
                        .fee(5000L)
                        .programId(null)
                        .build();
                default:
                    throw new IllegalArgumentException("Unsupported network type: " + networkType);
            }
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getFromAddress() { return fromAddress; }
        public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
        public String getToAddress() { return toAddress; }
        public void setToAddress(String toAddress) { this.toAddress = toAddress; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public String getNetworkType() { return networkType; }
        public void setNetworkType(String networkType) { this.networkType = networkType; }
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
    }

    public static class SignedTransactionResponse {
        private final UUID signedTransactionId;
        private final TransactionBodyResponse transactionBody;
        private final String signature; // Base64 encoded
        private final String signerAddress;
        private final String status;
        private final String transactionHash;

        public SignedTransactionResponse(UUID signedTransactionId, TransactionBodyResponse transactionBody, 
                                       String signature, String signerAddress, String status, String transactionHash) {
            this.signedTransactionId = signedTransactionId;
            this.transactionBody = transactionBody;
            this.signature = signature;
            this.signerAddress = signerAddress;
            this.status = status;
            this.transactionHash = transactionHash;
        }

        public static <T> SignedTransactionResponse from(SignedTransaction<T> signedTransaction) {
            return new SignedTransactionResponse(
                signedTransaction.getSignedTransactionId(),
                TransactionBodyResponse.from(signedTransaction.getTransactionBody()),
                com.bloominggrace.governance.shared.util.SignatureUtils.encodeBase64(signedTransaction.getSignature()),
                signedTransaction.getSignerAddress(),
                signedTransaction.getStatus().name(),
                signedTransaction.getTransactionHash()
            );
        }

        // Getters
        public UUID getSignedTransactionId() { return signedTransactionId; }
        public TransactionBodyResponse getTransactionBody() { return transactionBody; }
        public String getSignature() { return signature; }
        public String getSignerAddress() { return signerAddress; }
        public String getStatus() { return status; }
        public String getTransactionHash() { return transactionHash; }
    }

    public static class SignedRawTransactionResponse {
        private final String signedRawTransaction; // Base64 encoded
        private final String networkType;

        public SignedRawTransactionResponse(String signedRawTransaction, String networkType) {
            this.signedRawTransaction = signedRawTransaction;
            this.networkType = networkType;
        }
        
        public static SignedRawTransactionResponseBuilder builder() {
            return new SignedRawTransactionResponseBuilder();
        }
        
        public static class SignedRawTransactionResponseBuilder {
            private String signedRawTransaction;
            private String networkType;
            
            public SignedRawTransactionResponseBuilder signedRawTransaction(String signedRawTransaction) {
                this.signedRawTransaction = signedRawTransaction;
                return this;
            }
            
            public SignedRawTransactionResponseBuilder networkType(String networkType) {
                this.networkType = networkType;
                return this;
            }
            
            public SignedRawTransactionResponse build() {
                return new SignedRawTransactionResponse(signedRawTransaction, networkType);
            }
        }

        // Getters
        public String getSignedRawTransaction() { return signedRawTransaction; }
        public String getNetworkType() { return networkType; }
    }

    public static class SignedRawTransactionRequest {
        private String signedRawTransaction; // Base64 encoded
        private String networkType;

        // Getters and Setters
        public String getSignedRawTransaction() { return signedRawTransaction; }
        public void setSignedRawTransaction(String signedRawTransaction) { this.signedRawTransaction = signedRawTransaction; }
        public String getNetworkType() { return networkType; }
        public void setNetworkType(String networkType) { this.networkType = networkType; }
    }
} 