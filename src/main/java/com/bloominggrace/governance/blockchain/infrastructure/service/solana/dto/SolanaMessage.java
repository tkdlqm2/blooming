package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SolanaMessage {
    
    @JsonProperty("header")
    private SolanaMessageHeader header;
    
    @JsonProperty("accountKeys")
    private List<String> accountKeys;
    
    @JsonProperty("recentBlockhash")
    private String recentBlockhash;
    
    @JsonProperty("instructions")
    private List<Object> instructions;
} 