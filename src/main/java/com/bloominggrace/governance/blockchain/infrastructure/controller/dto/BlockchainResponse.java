package com.bloominggrace.governance.blockchain.infrastructure.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlockchainResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("error")
    private String error;
    
    public static <T> BlockchainResponse<T> success(T data) {
        return BlockchainResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    public static <T> BlockchainResponse<T> error(String error) {
        return BlockchainResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }
} 