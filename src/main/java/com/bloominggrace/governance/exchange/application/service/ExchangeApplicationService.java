package com.bloominggrace.governance.exchange.application.service;

import com.bloominggrace.governance.exchange.domain.model.ExchangeRequest;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.exchange.domain.model.ExchangeStatus;
import com.bloominggrace.governance.exchange.infrastructure.repository.ExchangeRequestRepository;
import com.bloominggrace.governance.point.application.service.PointManagementService;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.infrastructure.service.AdminWalletService;
import com.bloominggrace.governance.shared.infrastructure.service.TransactionOrchestrator;
import com.bloominggrace.governance.token.application.service.TokenAccountApplicationService;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.infrastructure.repository.TokenAccountRepository;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.model.BlockchainMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeApplicationService {
    
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final PointManagementService pointManagementService;
    private final TokenAccountApplicationService tokenAccountApplicationService;
    private final TransactionOrchestrator transactionOrchestrator;
    private final TokenAccountRepository tokenAccountRepository;
    private final AdminWalletService adminWalletService;

    // 포인트 → 토큰 교환 비율 (1 포인트 = 0.01 토큰)
    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("0.01");
    
    /**
     * 교환 요청을 조회합니다.
     */
    public ExchangeRequest getExchangeRequest(ExchangeRequestId exchangeRequestId) {
        return exchangeRequestRepository.findById(exchangeRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Exchange request not found: " + exchangeRequestId.getValue()));
    }
    
    /**
     * 교환 요청을 생성합니다.
     */
    @Transactional
    public ExchangeRequest createExchangeRequest(UserId userId, BigDecimal pointAmount, String walletAddress) {
        log.info("Creating exchange request - UserId: {}, PointAmount: {}, WalletAddress: {}", 
            userId, pointAmount, walletAddress);
        
        // 1. 포인트 잔액 확인
        var pointBalance = pointManagementService.getPointBalance(userId.getValue());
        if (pointBalance.getAvailableBalance().getAmount().compareTo(pointAmount) < 0) {
            throw new IllegalArgumentException("Insufficient point balance. Available: " + 
                pointBalance.getAvailableBalance().getAmount() + ", Required: " + pointAmount);
        }
        
        // 2. 지갑 주소 유효성 검증
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet address is required");
        }
        
        // 3. 교환 요청 생성
        ExchangeRequest exchangeRequest = new ExchangeRequest(
            userId.getValue(),
            PointAmount.of(pointAmount),
            walletAddress
        );
        
        // 4. 교환 요청 저장
        ExchangeRequest savedRequest = exchangeRequestRepository.save(exchangeRequest);
        
        log.info("Exchange request created successfully - RequestId: {}", savedRequest.getId());
        return savedRequest;
    }
    
    /**
     * 교환 요청을 처리합니다.
     */
    @Transactional
    public void processExchangeRequest(ExchangeRequestId exchangeRequestId) {
        log.info("Processing exchange request - RequestId: {}", exchangeRequestId);
        
        ExchangeRequest exchangeRequest = validateAndGetExchangeRequest(exchangeRequestId);
        
        try {
            // 1. 교환 처리 시작
            startExchangeProcessing(exchangeRequest);
            
            // 2. 사용자의 포인트 동결
            freezeUserPoints(exchangeRequest, exchangeRequestId);
            
            // 3. 토큰 전송 실행
            String transactionHash = executeTokenTransfer(exchangeRequest);
            
            // 4. 토큰 계정 생성 또는 업데이트
            createOrUpdateTokenAccount(exchangeRequest);
            
            // 5. 교환 완료 처리
            completeExchangeRequest(exchangeRequest, transactionHash);
            
            log.info("Exchange request processed successfully - RequestId: {}, TransactionHash: {}", 
                exchangeRequestId, transactionHash);
            
        } catch (Exception e) {
            log.error("Error processing exchange request - RequestId: {}", exchangeRequestId, e);
            handleExchangeFailure(exchangeRequest, exchangeRequestId, e);
        }
    }


    /**
     * 지갑 주소로부터 네트워크 타입을 결정합니다.
     */
    private NetworkType determineNetworkType(String walletAddress) {
        if (walletAddress.startsWith("0x")) {
            return NetworkType.ETHEREUM;
        } else if (walletAddress.length() == 44) {
            return NetworkType.SOLANA;
        } else {
            // 기본값으로 Ethereum 사용
            log.warn("Unable to determine network type from wallet address: {}. Using ETHEREUM as default.", walletAddress);
            return NetworkType.ETHEREUM;
        }
    }

    /**
     * 네트워크 타입에 따른 토큰 컨트랙트 주소를 반환합니다.
     */
    private String getTokenContractAddress(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                return BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS;
            case SOLANA:
                return BlockchainMetadata.Solana.SPL_TOKEN_MINT_ADDRESS;
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }

    /**
     * 교환 요청을 검증하고 조회합니다.
     */
    private ExchangeRequest validateAndGetExchangeRequest(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(exchangeRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Exchange request not found: " + exchangeRequestId));
        
        if (exchangeRequest.getStatus() != ExchangeStatus.REQUESTED) {
            throw new IllegalStateException("Exchange request is not in REQUESTED status: " + exchangeRequestId);
        }
        
        return exchangeRequest;
    }
    
    /**
     * 교환 처리 시작
     */
    private void startExchangeProcessing(ExchangeRequest exchangeRequest) {
        exchangeRequest.process();
        exchangeRequestRepository.save(exchangeRequest);
    }
    
    /**
     * 사용자 포인트 동결
     */
    private void freezeUserPoints(ExchangeRequest exchangeRequest, ExchangeRequestId exchangeRequestId) {
        pointManagementService.freezePoints(
            exchangeRequest.getUserId(),
            exchangeRequest.getPointAmount(),
            exchangeRequestId.getValue().toString()
        );
    }
    
    /**
     * 토큰 전송 실행
     */
    private String executeTokenTransfer(ExchangeRequest exchangeRequest) {
        NetworkType networkType = determineNetworkType(exchangeRequest.getWalletAddress());
        AdminWalletService.AdminWalletInfo adminWallet = adminWalletService.getAdminWallet(networkType);
        String tokenContract = getTokenContractAddress(networkType);
        BigDecimal tokenAmount = calculateTokenAmount(exchangeRequest);
        
        log.info("Creating token transfer transaction for exchange - From: {} To: {} Amount: {} Network: {} Contract: {}", 
            adminWallet.getWalletAddress(), exchangeRequest.getWalletAddress(), tokenAmount, networkType, tokenContract);
        
        String transactionHash = transactionOrchestrator.executeTransfer(
            adminWallet.getWalletAddress(),
            exchangeRequest.getWalletAddress(),
            networkType,
            tokenAmount,
            tokenContract
        ).getTransactionHash();
        
        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new RuntimeException("Token transfer failed: No transaction hash returned");
        }
        
        return transactionHash;
    }

    /**
     * 토큰 양 계산
     */
    private BigDecimal calculateTokenAmount(ExchangeRequest exchangeRequest) {
        return exchangeRequest.getPointAmount().getAmount().multiply(EXCHANGE_RATE);
    }
    
    /**
     * 토큰 계정 생성 또는 업데이트
     */
    private void createOrUpdateTokenAccount(ExchangeRequest exchangeRequest) {
        NetworkType networkType = determineNetworkType(exchangeRequest.getWalletAddress());
        String contractAddress = getTokenContractAddress(networkType);
        BigDecimal tokenAmount = calculateTokenAmount(exchangeRequest);
        
        log.info("Creating or updating token account - UserId: {}, WalletAddress: {}, Network: {}, Contract: {}, TokenAmount: {}", 
            exchangeRequest.getUserId(), exchangeRequest.getWalletAddress(), networkType, contractAddress, tokenAmount);
        
        // 1. 토큰 계정 조회 또는 생성
        TokenAccount tokenAccount = tokenAccountApplicationService.getOrCreateTokenAccount(
            new UserId(exchangeRequest.getUserId()),
            exchangeRequest.getWalletAddress(),
            networkType,
            contractAddress,
            getTokenSymbol(networkType)
        );
        
        // 2. 토큰 잔액 업데이트 (Exchange로 받은 토큰 추가)
        BigDecimal currentBalance = tokenAccount.getTotalBalance();
        BigDecimal newBalance = currentBalance.add(tokenAmount);
        
        log.info("Updating token balance - Current: {}, Added: {}, New: {}", currentBalance, tokenAmount, newBalance);
        
        // receiveTokens 메서드를 사용하여 토큰 수령 처리
        tokenAccount.receiveTokens(tokenAmount, "Exchange from points: " + exchangeRequest.getPointAmount().getAmount() + " points");
        tokenAccountRepository.save(tokenAccount);
        
        log.info("Token account updated successfully - AccountId: {}, NewBalance: {}", tokenAccount.getId(), tokenAccount.getTotalBalance());
    }
    
    /**
     * 토큰 심볼 가져오기
     */
    private String getTokenSymbol(NetworkType networkType) {
        switch (networkType) {
            case ETHEREUM:
                return BlockchainMetadata.Ethereum.ERC20_SYMBOL;
            case SOLANA:
                return BlockchainMetadata.Solana.SPL_TOKEN_SYMBOL;
            default:
                throw new IllegalArgumentException("Unsupported network type: " + networkType);
        }
    }
    
    /**
     * 교환 완료 처리
     */
    private void completeExchangeRequest(ExchangeRequest exchangeRequest, String transactionHash) {
        exchangeRequest.complete(transactionHash);
        exchangeRequestRepository.save(exchangeRequest);
    }
    
    /**
     * 교환 실패 처리
     */
    private void handleExchangeFailure(ExchangeRequest exchangeRequest, ExchangeRequestId exchangeRequestId, Exception e) {
        // 교환 실패 처리
        exchangeRequest.fail();
        exchangeRequestRepository.save(exchangeRequest);
        
        // 포인트 해제
        unfreezeUserPoints(exchangeRequest, exchangeRequestId);
        
        throw new RuntimeException("Failed to process exchange request: " + e.getMessage(), e);
    }
    
    /**
     * 사용자 포인트 해제
     */
    private void unfreezeUserPoints(ExchangeRequest exchangeRequest, ExchangeRequestId exchangeRequestId) {
        pointManagementService.unfreezePoints(
            exchangeRequest.getUserId(),
            exchangeRequest.getPointAmount(),
            exchangeRequestId.getValue().toString()
        );
    }
    
    /**
     * 취소를 위한 교환 요청을 검증하고 조회합니다.
     */
    private ExchangeRequest validateAndGetExchangeRequestForCancellation(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(exchangeRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Exchange request not found: " + exchangeRequestId));
        
        if (exchangeRequest.getStatus() == ExchangeStatus.COMPLETED) {
            throw new IllegalStateException("Exchange request cannot be cancelled: " + exchangeRequestId);
        }
        
        return exchangeRequest;
    }
    
    /**
     * 처리 중인 경우 포인트 해제
     */
    private void unfreezePointsIfProcessing(ExchangeRequest exchangeRequest, ExchangeRequestId exchangeRequestId) {
        if (exchangeRequest.getStatus() == ExchangeStatus.PROCESSING) {
            pointManagementService.unfreezePoints(
                exchangeRequest.getUserId(),
                exchangeRequest.getPointAmount(),
                exchangeRequestId.getValue().toString()
            );
        }
    }
    
    /**
     * 교환 요청 취소 처리
     */
    private void cancelExchangeRequest(ExchangeRequest exchangeRequest) {
        exchangeRequest.cancel();
        exchangeRequestRepository.save(exchangeRequest);
    }
} 