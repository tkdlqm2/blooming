package com.bloominggrace.governance.shared.blockchain.domain.constants;

public class SolanaConstants {
    
    public static class Network {
        public static final String NETWORK_NAME = "Solana Mainnet";
        public static final String CLUSTER_URL = "https://api.mainnet-beta.solana.com";
    }
    
    public static class Token {
        public static final String SPL_TOKEN_SYMBOL = "SOL";
        public static final String SPL_TOKEN_MINT_ADDRESS = "So11111111111111111111111111111111111111112";
    }
    
    public static class RpcMethods {
        public static final String GET_BALANCE = "getBalance";
        public static final String GET_BLOCK_HASH = "getLatestBlockhash";
        public static final String SEND_TRANSACTION = "sendTransaction";
        public static final String GET_TRANSACTION = "getTransaction";
        public static final String GET_ACCOUNT_INFO = "getAccountInfo";
        public static final String GET_TOKEN_ACCOUNTS_BY_OWNER = "getTokenAccountsByOwner";
    }
} 