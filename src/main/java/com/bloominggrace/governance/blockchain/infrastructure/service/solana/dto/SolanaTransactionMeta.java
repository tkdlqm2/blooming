package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SolanaTransactionMeta {
    
    @JsonProperty("err")
    private Object err;
    
    @JsonProperty("fee")
    private Long fee;
    
    @JsonProperty("preBalances")
    private List<Long> preBalances;
    
    @JsonProperty("postBalances")
    private List<Long> postBalances;
    
    @JsonProperty("innerInstructions")
    private List<Object> innerInstructions;
    
    @JsonProperty("logMessages")
    private List<String> logMessages;
    
    @JsonProperty("preTokenBalances")
    private List<Object> preTokenBalances;
    
    @JsonProperty("postTokenBalances")
    private List<Object> postTokenBalances;
    
    @JsonProperty("rewards")
    private List<Object> rewards;
} 