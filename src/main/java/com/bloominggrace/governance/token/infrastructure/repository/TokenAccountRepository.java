package com.bloominggrace.governance.token.infrastructure.repository;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenAccountRepository {
    TokenAccount save(TokenAccount tokenAccount);
    Optional<TokenAccount> findById(UUID id);
    Optional<TokenAccount> findByUserId(UserId userId);
    List<TokenAccount> findAllByUserId(UserId userId);
    List<TokenAccount> findByWallet(Wallet wallet);
    List<TokenAccount> findByWalletAndNetwork(Wallet wallet, NetworkType network);
    Optional<TokenAccount> findByWalletAndContract(Wallet wallet, String contract);
    List<TokenAccount> findByWalletAddress(String walletAddress);
    List<TokenAccount> findByWalletAddressAndNetwork(String walletAddress, NetworkType network);
    List<TokenAccount> findAll();
    void delete(UUID id);
    boolean existsByUserId(UserId userId);
    boolean existsByWalletAndContract(Wallet wallet, String contract);
    
    // 새로운 메서드들 추가
    Optional<TokenAccount> findByUserIdAndNetworkAndContract(UserId userId, NetworkType network, String contract);
    List<TokenAccount> findByUserIdAndNetwork(UserId userId, NetworkType network);
    boolean existsByUserIdAndNetworkAndContract(UserId userId, NetworkType network, String contract);
    Optional<TokenAccount> findByWalletAddressAndNetworkAndContract(String walletAddress, NetworkType network, String contract);
} 