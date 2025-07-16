package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SolanaTransaction {
    
    @JsonProperty("slot")
    private Long slot;
    
    @JsonProperty("transaction")
    private SolanaTransactionData transaction;
    
    @JsonProperty("meta")
    private SolanaTransactionMeta meta;
    
    @JsonProperty("version")
    private String version;
} 