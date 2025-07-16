package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SolanaRpcResponse<T> {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc;
    
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("result")
    private T result;
    
    @JsonProperty("error")
    private SolanaRpcError error;
} 