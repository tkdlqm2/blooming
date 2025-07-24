package com.bloominggrace.governance.shared.blockchain.domain.constants;

import java.math.BigInteger;

public class EthereumConstants {
    
    public static class Network {
        public static final String NETWORK_NAME = "Ethereum Sepolia";
        public static final long CHAIN_ID = 11155111L;
    }
    
    public static class Gas {
        public static final BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L); // 20 Gwei
        public static final BigInteger GAS_LIMIT = BigInteger.valueOf(21000L);
        public static final BigInteger PROPOSAL_CREATION_GAS_LIMIT = BigInteger.valueOf(500000L);
        public static final BigInteger VOTE_GAS_LIMIT = BigInteger.valueOf(150000L);
        public static final BigInteger TRANSFER_DELEGATE_GAS_LIMIT = BigInteger.valueOf(550000L);
    }
    
    public static class Contracts {
        public static final String GOVERNANCE_CONTRACT_ADDRESS = "0x4E5EE91796498E843a7Ae952BC86B1a1547C60bB";
        public static final String ERC20_CONTRACT_ADDRESS = "0xd2Dfe16C1F31493530D297D58E32c337fd27615D";
    }
    
    public static class Token {
        public static final String ERC20_SYMBOL = "ETH";
        public static final String BALANCE_OF_SELECTOR = "0x70a08231";
    }
    
    public static class RpcMethods {
        public static final String GET_BLOCK_BY_NUMBER = "eth_getBlockByNumber";
        public static final String GET_GAS_PRICE = "eth_gasPrice";
        public static final String ESTIMATE_GAS = "eth_estimateGas";
        public static final String GET_TRANSACTION_COUNT = "eth_getTransactionCount";
        public static final String GET_BALANCE = "eth_getBalance";
        public static final String CALL = "eth_call";
        public static final String SEND_RAW_TRANSACTION = "eth_sendRawTransaction";
        public static final String GET_TRANSACTION_RECEIPT = "eth_getTransactionReceipt";
        public static final String GET_BLOCK_BY_HASH = "eth_getBlockByHash";
        public static final String GET_BLOCK_NUMBER = "eth_blockNumber";
    }
    
    public static class RpcParams {
        public static final String LATEST = "latest";
        public static final String PENDING = "pending";
        // EXCLUDE_TRANSACTIONS는 boolean 값으로 직접 사용해야 함 (문자열 "false"는 RPC 에러 발생)
        // public static final String EXCLUDE_TRANSACTIONS = "false";
    }
} 