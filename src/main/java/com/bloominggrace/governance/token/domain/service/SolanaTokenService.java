package com.bloominggrace.governance.token.domain.service;

import com.bloominggrace.governance.token.domain.model.TokenAmount;

public interface SolanaTokenService {
    String mintTokens(String walletAddress, TokenAmount amount);
    String stakeTokens(String walletAddress, TokenAmount amount);
    String unstakeTokens(String walletAddress, TokenAmount amount);
    TokenAmount getBalance(String walletAddress);
    boolean isValidWalletAddress(String walletAddress);
} 