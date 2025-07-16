package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SolanaRpcError {
    
    @JsonProperty("code")
    private int code;
    
    @JsonProperty("message")
    private String message;
} 