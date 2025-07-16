package com.bloominggrace.governance.exchange.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequest;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.exchange.domain.model.ExchangeStatus;
import com.bloominggrace.governance.exchange.infrastructure.repository.ExchangeRequestRepository;
import com.bloominggrace.governance.point.application.service.PointManagementService;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.domain.model.TokenAmount;
import com.bloominggrace.governance.token.infrastructure.repository.TokenAccountRepository;
import com.bloominggrace.governance.shared.domain.model.BlockchainTransactionType;
import com.bloominggrace.governance.shared.domain.model.Transaction;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.TransactionRequest;
import com.bloominggrace.governance.shared.infrastructure.repository.TransactionRepository;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ExchangeApplicationService {
    
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final PointManagementService pointManagementService;
    private final TokenAccountRepository tokenAccountRepository;
    private final TransactionRepository transactionRepository;
    private final WalletApplicationService walletApplicationService;
    
    // 포인트와 토큰의 교환 비율 (1 포인트 = 0.01 토큰)
    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("0.01");
    
    /**
     * 포인트를 토큰으로 교환 요청
     */
    public ExchangeRequestId requestExchange(UUID userId, PointAmount pointAmount, String walletAddress) {
        // 포인트 잔액 확인 (PointManagementService에는 hasSufficientPoints 메서드가 없으므로 직접 확인)
        PointManagementService.PointBalance balance = pointManagementService.getPointBalance(userId);
        if (balance.getAvailableBalance().getAmount().compareTo(pointAmount.getAmount()) < 0) {
            throw new IllegalStateException("포인트가 부족합니다");
        }
        
        // 교환 요청 생성
        ExchangeRequest exchangeRequest = new ExchangeRequest(userId, pointAmount, walletAddress);
        exchangeRequest = exchangeRequestRepository.save(exchangeRequest);
        
        return exchangeRequest.getId();
    }
    
    /**
     * 교환 처리 (포인트 차감 및 토큰 발행)
     */
    public void processExchange(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(exchangeRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Exchange request not found"));
        
        if (exchangeRequest.getStatus() != ExchangeStatus.REQUESTED) {
            throw new IllegalStateException("Exchange request already processed");
        }
        
        // 포인트 차감
        pointManagementService.freezePoints(exchangeRequest.getUserId(), exchangeRequest.getPointAmount(), exchangeRequest.getId().toString());
        
        // 교환 요청 상태 업데이트
        exchangeRequest.process();
        exchangeRequestRepository.save(exchangeRequest);
    }
    
    /**
     * 교환 완료 (토큰 발행)
     */
    public void completeExchange(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(exchangeRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Exchange request not found"));
        
        if (exchangeRequest.getStatus() != ExchangeStatus.PROCESSING) {
            throw new IllegalStateException("Exchange request not processed yet");
        }
        
        if (exchangeRequest.getStatus() == ExchangeStatus.COMPLETED) {
            throw new IllegalStateException("Exchange request already completed");
        }
        
        // 블록체인 네트워크에 토큰 발행 트랜잭션 생성 및 전송
        String blockchainTransactionSignature = mintTokensOnBlockchain(exchangeRequest);
        
        // 토큰 발행 (로컬 DB)
        mintTokensForExchange(exchangeRequest);
        
        // 교환 요청 완료 처리 (블록체인 트랜잭션 서명 포함)
        exchangeRequest.complete(blockchainTransactionSignature);
        exchangeRequestRepository.save(exchangeRequest);
    }
    
    /**
     * 교환 취소
     */
    public void cancelExchange(ExchangeRequestId exchangeRequestId) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(exchangeRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Exchange request not found"));
        
        if (exchangeRequest.getStatus() == ExchangeStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed exchange");
        }
        
        // 포인트 해제 (차감된 포인트 복원)
        if (exchangeRequest.getStatus() == ExchangeStatus.PROCESSING) {
            pointManagementService.unfreezePoints(exchangeRequest.getUserId(), exchangeRequest.getPointAmount(), exchangeRequest.getId().toString());
        }
        
        // 교환 요청 취소
        exchangeRequest.cancel();
        exchangeRequestRepository.save(exchangeRequest);
    }
    
    /**
     * 교환을 위한 토큰 발행
     */
    private void mintTokensForExchange(ExchangeRequest exchangeRequest) {
        // 포인트를 토큰으로 변환 (1 포인트 = 0.01 토큰)
        BigDecimal tokenAmount = exchangeRequest.getPointAmount().getAmount()
            .multiply(EXCHANGE_RATE);
        
        // 토큰 계정 생성 또는 조회
        TokenAccount tokenAccount = getOrCreateTokenAccount(new UserId(exchangeRequest.getUserId()), exchangeRequest.getWalletAddress());
        
        // 토큰 발행
        tokenAccount.mintTokens(tokenAmount, "포인트 교환: " + exchangeRequest.getPointAmount().getAmount() + " 포인트");
        tokenAccountRepository.save(tokenAccount);
        
        // 블록체인 트랜잭션 기록
        Transaction transaction = new Transaction(
            new UserId(exchangeRequest.getUserId()),
            BlockchainTransactionType.TOKEN_MINT,
            determineNetworkType(exchangeRequest.getWalletAddress()),
            tokenAmount,
            exchangeRequest.getWalletAddress(),
            null, // MINT는 toAddress 없음
            "포인트 교환: " + exchangeRequest.getPointAmount().getAmount() + " 포인트"
        );
        transactionRepository.save(transaction);
    }
    
    /**
     * 블록체인 네트워크에 토큰 발행 트랜잭션 생성 및 전송
     */
    private String mintTokensOnBlockchain(ExchangeRequest exchangeRequest) {
        // 포인트를 토큰으로 변환 (1 포인트 = 0.01 토큰)
        BigDecimal tokenAmount = exchangeRequest.getPointAmount().getAmount()
            .multiply(EXCHANGE_RATE);
        
        // 네트워크 타입 결정
        NetworkType networkType = determineNetworkType(exchangeRequest.getWalletAddress());
        
        // 새로운 통합 메서드를 사용하여 토큰 민팅 트랜잭션 생성
        TransactionRequest.TokenMintData mintData = new TransactionRequest.TokenMintData(
            "포인트 교환: " + exchangeRequest.getPointAmount().getAmount() + " 포인트"
        );
        TransactionBody transactionBody = walletApplicationService.createTransactionBody(
            exchangeRequest.getWalletAddress(), null, tokenAmount, null, mintData, 
            TransactionRequest.TransactionType.TOKEN_MINT, networkType
        );
        
        // 트랜잭션 서명 및 브로드캐스트
        byte[] signedTransaction = walletApplicationService.signTransactionBody(transactionBody, exchangeRequest.getWalletAddress());
        return walletApplicationService.broadcastSignedTransaction(signedTransaction, networkType.name());
    }
    
    /**
     * 토큰 계정 생성 또는 조회
     */
    private TokenAccount getOrCreateTokenAccount(UserId userId, String walletAddress) {
        return tokenAccountRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Wallet 객체를 찾아야 함
                Wallet wallet = walletApplicationService.getWalletByAddress(walletAddress)
                    .orElseThrow(() -> new IllegalStateException("Wallet not found: " + walletAddress));
                
                NetworkType networkType = determineNetworkType(walletAddress);
                TokenAccount newAccount = new TokenAccount(
                    wallet, 
                    userId, 
                    networkType,
                    "default-contract", // 기본값, 필요시 파라미터로 받아야 함
                    "TOKEN" // 기본값, 필요시 파라미터로 받아야 함
                );
                return tokenAccountRepository.save(newAccount);
            });
    }
    
    /**
     * 지갑 주소로 네트워크 타입 결정
     */
    private NetworkType determineNetworkType(String walletAddress) {
        return com.bloominggrace.governance.shared.util.AddressUtils.guessNetworkType(walletAddress);
    }
    
    // ===== 조회 메서드들 =====
    
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getExchangeRequests(UUID userId) {
        return exchangeRequestRepository.findByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public Optional<ExchangeRequest> getExchangeRequest(ExchangeRequestId exchangeRequestId) {
        return exchangeRequestRepository.findById(exchangeRequestId);
    }
} 