package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SolanaBlockResult {
    
    @JsonProperty("blockhash")
    private String blockhash;
    
    @JsonProperty("parentSlot")
    private Long parentSlot;
    
    @JsonProperty("transactions")
    private List<SolanaTransaction> transactions;
    
    @JsonProperty("rewards")
    private List<Object> rewards;
    
    @JsonProperty("blockTime")
    private Long blockTime;
    
    @JsonProperty("blockHeight")
    private Long blockHeight;
    
    @JsonProperty("previousBlockhash")
    private String previousBlockhash;
} 