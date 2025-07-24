package com.bloominggrace.governance.shared.blockchain.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.blockchain.domain.model.BlockchainTransactionType;
import com.bloominggrace.governance.shared.blockchain.domain.model.Transaction;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository transactionJpaRepository;

    public TransactionRepositoryAdapter(TransactionJpaRepository transactionJpaRepository) {
        this.transactionJpaRepository = transactionJpaRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transactionJpaRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionJpaRepository.findById(id);
    }

    @Override
    public Optional<Transaction> findByTransactionHash(String transactionHash) {
        return transactionJpaRepository.findByTransactionHash(transactionHash);
    }

    @Override
    public List<Transaction> findByUserId(UserId userId) {
        return transactionJpaRepository.findByUserId(userId);
    }

    @Override
    public List<Transaction> findByUserIdAndTransactionType(UserId userId, BlockchainTransactionType transactionType) {
        return transactionJpaRepository.findByUserIdAndTransactionType(userId, transactionType);
    }

    @Override
    public List<Transaction> findByFromAddress(String fromAddress) {
        return transactionJpaRepository.findByFromAddress(fromAddress);
    }

    @Override
    public List<Transaction> findByToAddress(String toAddress) {
        return transactionJpaRepository.findByToAddress(toAddress);
    }

    @Override
    public List<Transaction> findByNetworkType(NetworkType networkType) {
        return transactionJpaRepository.findByNetworkType(networkType);
    }

    @Override
    public List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionJpaRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return transactionJpaRepository.findByStatus(status);
    }

    @Override
    public List<Transaction> findAll() {
        return transactionJpaRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        transactionJpaRepository.deleteById(id);
    }

    @Override
    public List<Transaction> findByUserIdOrderByCreatedAtDesc(UserId userId) {
        return transactionJpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Transaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(UserId userId, BlockchainTransactionType transactionType) {
        return transactionJpaRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(userId, transactionType);
    }

    @Override
    public List<Transaction> findByFromAddressOrderByCreatedAtDesc(String fromAddress) {
        return transactionJpaRepository.findByFromAddressOrderByCreatedAtDesc(fromAddress);
    }

    @Override
    public List<Transaction> findByNetworkTypeAndTransactionType(NetworkType networkType, BlockchainTransactionType transactionType) {
        return transactionJpaRepository.findByNetworkTypeAndTransactionType(networkType, transactionType);
    }
} 