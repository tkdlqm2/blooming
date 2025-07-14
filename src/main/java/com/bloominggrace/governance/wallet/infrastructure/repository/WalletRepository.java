package com.bloominggrace.governance.wallet.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUserId(UUID userId);
    List<Wallet> findByUserId(UserId userId);
    List<Wallet> findByNetworkType(NetworkType networkType);
    Optional<Wallet> findByUserIdAndNetworkType(UserId userId, NetworkType networkType);
    boolean existsByWalletAddress(String walletAddress);
    Optional<Wallet> findByWalletAddress(String walletAddress);
} 