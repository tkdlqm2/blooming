package com.bloominggrace.governance.blockchain.infrastructure.service.solana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonRequest {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    @JsonProperty("id")
    private int id = 1;
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("params")
    private List<Object> params;
    
    public JsonRequest(String method, List<Object> params) {
        this.method = method;
        this.params = params;
    }
} 