package com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EthereumRpcResponse<T> {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc;
    
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("result")
    private T result;
    
    @JsonProperty("error")
    private EthereumRpcError error;
    
    public boolean hasError() {
        return error != null;
    }
} 