package com.bloominggrace.governance.shared.blockchain.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * JSON-RPC 통신을 위한 재사용 가능한 클라이언트
 */
@Slf4j
@Component
public class JsonRpcClient {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public JsonRpcClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    /**
     * JSON-RPC 요청을 전송하고 응답을 받습니다.
     * 
     * @param rpcUrl JSON-RPC 서버 URL
     * @param request JSON-RPC 요청 객체
     * @param typeReference 응답 타입 참조
     * @param <T> 응답 타입
     * @return JSON-RPC 응답
     * @throws IOException HTTP 통신 오류
     * @throws InterruptedException 인터럽트 오류
     */
    public <T> T sendRequest(String rpcUrl, Object request, TypeReference<T> typeReference) 
            throws IOException, InterruptedException {
        
        String requestBody = objectMapper.writeValueAsString(request);
        log.debug("Sending JSON-RPC request to {}: {}", rpcUrl, requestBody);
        
        // HTTP 요청 생성
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(rpcUrl))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .timeout(Duration.ofSeconds(30))
            .build();
        
        // 요청 전송
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.debug("Received HTTP response status: {}, body: {}", response.statusCode(), response.body());
        
        if (response.statusCode() != 200) {
            log.error("HTTP request failed with status: {} and body: {}", response.statusCode(), response.body());
            throw new IOException("HTTP request failed with status: " + response.statusCode() + ", body: " + response.body());
        }
        
        try {
            return objectMapper.readValue(response.body(), typeReference);
        } catch (Exception e) {
            log.error("Failed to parse JSON-RPC response: {}", response.body(), e);
            throw e;
        }
    }
    
    /**
     * 간단한 JSON-RPC 요청을 전송합니다.
     * 
     * @param rpcUrl JSON-RPC 서버 URL
     * @param method JSON-RPC 메서드
     * @param params JSON-RPC 파라미터
     * @param typeReference 응답 타입 참조
     * @param <T> 응답 타입
     * @return JSON-RPC 응답
     * @throws IOException HTTP 통신 오류
     * @throws InterruptedException 인터럽트 오류
     */
    public <T> T sendSimpleRequest(String rpcUrl, String method, Object[] params, TypeReference<T> typeReference) 
            throws IOException, InterruptedException {
        
        Map<String, Object> request = Map.of(
            "jsonrpc", "2.0",
            "method", method,
            "params", params,
            "id", 1
        );
        
        return sendRequest(rpcUrl, request, typeReference);
    }
    
    /**
     * JSON-RPC 응답에서 에러를 확인합니다.
     * 
     * @param responseMap JSON-RPC 응답 맵
     * @throws IOException JSON-RPC 에러가 있는 경우
     */
    @SuppressWarnings("unchecked")
    public static void checkForError(Map<String, Object> responseMap) throws IOException {
        if (responseMap.containsKey("error") && responseMap.get("error") != null) {
            Map<String, Object> error = (Map<String, Object>) responseMap.get("error");
            String errorMessage = (String) error.get("message");
            Integer errorCode = (Integer) error.get("code");
            log.error("JSON-RPC error: code={}, message={}", errorCode, errorMessage);
            throw new IOException("JSON-RPC error: " + errorMessage);
        }
    }
    
    /**
     * JSON-RPC 응답에서 결과를 추출합니다.
     * 
     * @param responseMap JSON-RPC 응답 맵
     * @return 결과 객체
     * @throws IOException 결과가 없는 경우
     */
    public static Object extractResult(Map<String, Object> responseMap) throws IOException {
        Object result = responseMap.get("result");
        if (result == null) {
            log.error("No result in JSON-RPC response: {}", responseMap);
            throw new IOException("No result in JSON-RPC response");
        }
        return result;
    }
    
    /**
     * Hex 문자열을 BigInteger로 변환합니다.
     * 
     * @param hexString hex 문자열 (0x 접두사 포함 가능)
     * @return BigInteger 값
     */
    public static java.math.BigInteger hexToBigInteger(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return java.math.BigInteger.ZERO;
        }
        
        if (hexString.startsWith("0x")) {
            hexString = hexString.substring(2);
        }
        
        if (hexString.isEmpty()) {
            return java.math.BigInteger.ZERO;
        }
        
        return new java.math.BigInteger(hexString, 16);
    }
    
    /**
     * BigInteger를 hex 문자열로 변환합니다.
     * 
     * @param value BigInteger 값
     * @return hex 문자열 (0x 접두사 포함)
     */
    public static String bigIntegerToHex(java.math.BigInteger value) {
        if (value == null) {
            return "0x0";
        }
        return "0x" + value.toString(16);
    }
} 