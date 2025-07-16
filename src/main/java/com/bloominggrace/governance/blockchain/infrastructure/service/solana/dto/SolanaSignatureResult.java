package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SolanaSignatureResult {
    
    @JsonProperty("context")
    private SolanaContext context;
    
    @JsonProperty("value")
    private String value;
} 