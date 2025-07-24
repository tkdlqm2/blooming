package com.bloominggrace.governance.blockchain.infrastructure.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainRpcRequest {
    
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("params")
    private List<Object> params;
    
    @JsonProperty("id")
    @Builder.Default
    private int id = 1;
    
    public static BlockchainRpcRequest of(String method, List<Object> params) {
        return BlockchainRpcRequest.builder()
                .jsonrpc("2.0")
                .method(method)
                .params(params)
                .id(1)
                .build();
    }
} 