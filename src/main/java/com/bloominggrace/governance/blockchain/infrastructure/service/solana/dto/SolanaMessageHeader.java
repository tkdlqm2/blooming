package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SolanaMessageHeader {
    
    @JsonProperty("numRequiredSignatures")
    private Integer numRequiredSignatures;
    
    @JsonProperty("numReadonlySignedAccounts")
    private Integer numReadonlySignedAccounts;
    
    @JsonProperty("numReadonlyUnsignedAccounts")
    private Integer numReadonlyUnsignedAccounts;
} 