package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
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
} 