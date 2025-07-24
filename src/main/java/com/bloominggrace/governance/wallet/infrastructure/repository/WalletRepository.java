package com.bloominggrace.governance.wallet.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUserId(UUID userId);
    List<Wallet> findByUserId(UserId userId);
    
    // User 관계를 통해 조회하는 메서드 추가
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.networkType = :networkType")
    Optional<Wallet> findByUser_IdAndNetworkType(@Param("userId") UUID userId, @Param("networkType") NetworkType networkType);
    Optional<Wallet> findByWalletAddress(String walletAddress);
} 