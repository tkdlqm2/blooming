package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SolanaBalanceResult {
    
    @JsonProperty("context")
    private SolanaContext context;
    
    @JsonProperty("value")
    private Long value;
} 