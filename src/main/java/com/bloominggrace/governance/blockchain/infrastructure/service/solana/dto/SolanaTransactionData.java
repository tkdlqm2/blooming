package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SolanaTransactionData {
    
    @JsonProperty("signatures")
    private List<String> signatures;
    
    @JsonProperty("message")
    private SolanaMessage message;
} 