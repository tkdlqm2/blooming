package com.bloominggrace.governance.shared.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.model.BlockchainTransactionType;
import com.bloominggrace.governance.shared.domain.model.Transaction;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTransactionHash(String transactionHash);
    List<Transaction> findByUserId(UserId userId);
    List<Transaction> findByUserIdAndTransactionType(UserId userId, BlockchainTransactionType transactionType);
    List<Transaction> findByFromAddress(String fromAddress);
    List<Transaction> findByToAddress(String toAddress);
    List<Transaction> findByNetworkType(NetworkType networkType);
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    /**
     * 사용자 ID로 트랜잭션을 생성일 기준 내림차순으로 조회
     */
    List<Transaction> findByUserIdOrderByCreatedAtDesc(UserId userId);
    
    /**
     * 사용자 ID와 트랜잭션 타입으로 트랜잭션을 생성일 기준 내림차순으로 조회
     */
    List<Transaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(UserId userId, BlockchainTransactionType transactionType);
    
    /**
     * 지갑 주소로 트랜잭션을 생성일 기준 내림차순으로 조회
     */
    List<Transaction> findByFromAddressOrderByCreatedAtDesc(String fromAddress);
    
    /**
     * 네트워크 타입과 트랜잭션 타입으로 조회
     */
    List<Transaction> findByNetworkTypeAndTransactionType(NetworkType networkType, BlockchainTransactionType transactionType);
} 