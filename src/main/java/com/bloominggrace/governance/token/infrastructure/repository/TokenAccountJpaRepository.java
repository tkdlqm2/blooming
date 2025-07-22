package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
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
public interface TokenAccountJpaRepository extends JpaRepository<TokenAccount, UUID> {
    Optional<TokenAccount> findByUserId(UserId userId);
    List<TokenAccount> findAllByUserId(UserId userId);
    List<TokenAccount> findByWallet(Wallet wallet);
    List<TokenAccount> findByWalletAndNetwork(Wallet wallet, NetworkType network);
    Optional<TokenAccount> findByWalletAndContract(Wallet wallet, String contract);
    List<TokenAccount> findByWalletAddress(String walletAddress);
    List<TokenAccount> findByWalletAddressAndNetwork(String walletAddress, NetworkType network);
    boolean existsByUserId(UserId userId);
    boolean existsByWalletAndContract(Wallet wallet, String contract);
    
    // 새로운 메서드들 추가
    @Query("SELECT ta FROM TokenAccount ta WHERE ta.userId = :userId AND ta.network = :network AND ta.contract = :contract")
    Optional<TokenAccount> findByUserIdAndNetworkAndContract(@Param("userId") UserId userId, @Param("network") NetworkType network, @Param("contract") String contract);
    
    @Query("SELECT ta FROM TokenAccount ta WHERE ta.userId = :userId AND ta.network = :network")
    List<TokenAccount> findByUserIdAndNetwork(@Param("userId") UserId userId, @Param("network") NetworkType network);
    
    @Query("SELECT COUNT(ta) > 0 FROM TokenAccount ta WHERE ta.userId = :userId AND ta.network = :network AND ta.contract = :contract")
    boolean existsByUserIdAndNetworkAndContract(@Param("userId") UserId userId, @Param("network") NetworkType network, @Param("contract") String contract);
    
    @Query("SELECT ta FROM TokenAccount ta WHERE ta.walletAddress = :walletAddress AND ta.network = :network AND ta.contract = :contract")
    Optional<TokenAccount> findByWalletAddressAndNetworkAndContract(@Param("walletAddress") String walletAddress, @Param("network") NetworkType network, @Param("contract") String contract);
} 