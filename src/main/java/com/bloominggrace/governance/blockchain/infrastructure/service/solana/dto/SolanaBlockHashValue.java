package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SolanaBlockHashValue {
    
    @JsonProperty("blockhash")
    private String blockhash;
    
    @JsonProperty("lastValidBlockHeight")
    private Long lastValidBlockHeight;
} 