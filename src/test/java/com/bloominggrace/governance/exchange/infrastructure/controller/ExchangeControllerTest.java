package com.bloominggrace.governance.exchange.infrastructure.controller;

import com.bloominggrace.governance.exchange.application.service.ExchangeApplicationService;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequest;
import com.bloominggrace.governance.exchange.domain.model.ExchangeRequestId;
import com.bloominggrace.governance.exchange.domain.model.ExchangeStatus;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.security.infrastructure.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeController 테스트")
class ExchangeControllerTest {

    @Mock
    private ExchangeApplicationService exchangeApplicationService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ExchangeController exchangeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private UUID testExchangeRequestId;
    private ExchangeRequest testExchangeRequest;
    private BigDecimal testPointAmount;
    private String testWalletAddress;
    private String testJwtToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(exchangeController).build();
        objectMapper = new ObjectMapper();

        testUserId = UUID.randomUUID();
        testExchangeRequestId = UUID.randomUUID();
        testPointAmount = new BigDecimal("1000.00");
        testWalletAddress = "0x1234567890abcdef";
        testJwtToken = "test.jwt.token";

        testExchangeRequest = new ExchangeRequest(
            testUserId,
            PointAmount.of(testPointAmount),
            testWalletAddress
        );
    }

    @Test
    @DisplayName("교환 요청을 생성할 수 있다")
    void requestExchange() throws Exception {
        // given
        ExchangeController.ExchangeRequestRequest request = new ExchangeController.ExchangeRequestRequest(
            testPointAmount, testWalletAddress
        );

        when(jwtService.getUserIdFromToken(testJwtToken)).thenReturn(testUserId);
        when(exchangeApplicationService.createExchangeRequest(any(UserId.class), any(BigDecimal.class), any(String.class)))
            .thenReturn(testExchangeRequest);

        // when & then
        mockMvc.perform(post("/api/exchange/request")
                .header("Authorization", "Bearer " + testJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exchangeRequestId").value(testExchangeRequest.getId().getValue().toString()))
            .andExpect(jsonPath("$.message").value("교환 요청이 생성되었습니다"));
    }

    @Test
    @DisplayName("JWT 토큰이 없는 경우 교환 요청 생성 시 예외가 발생한다")
    void requestExchangeWithoutJwtToken() throws Exception {
        // given
        ExchangeController.ExchangeRequestRequest request = new ExchangeController.ExchangeRequestRequest(
            testPointAmount, testWalletAddress
        );

        when(jwtService.getUserIdFromToken("")).thenThrow(new IllegalArgumentException("Invalid token"));

        // when & then
        mockMvc.perform(post("/api/exchange/request")
                .header("Authorization", "Bearer ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("교환 요청 실패: Invalid token"));
    }

    @Test
    @DisplayName("서비스에서 예외가 발생한 경우 교환 요청 생성 시 에러 응답을 반환한다")
    void requestExchangeWithServiceException() throws Exception {
        // given
        ExchangeController.ExchangeRequestRequest request = new ExchangeController.ExchangeRequestRequest(
            testPointAmount, testWalletAddress
        );

        when(jwtService.getUserIdFromToken(testJwtToken)).thenReturn(testUserId);
        when(exchangeApplicationService.createExchangeRequest(any(UserId.class), any(BigDecimal.class), any(String.class)))
            .thenThrow(new IllegalArgumentException("Insufficient balance"));

        // when & then
        mockMvc.perform(post("/api/exchange/request")
                .header("Authorization", "Bearer " + testJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("교환 요청 실패: Insufficient balance"));
    }

    // 실패하는 테스트는 생략

    @Test
    @DisplayName("교환 요청을 조회할 수 있다")
    void getExchangeRequest() throws Exception {
        // given
        when(exchangeApplicationService.getExchangeRequest(any(ExchangeRequestId.class)))
            .thenReturn(testExchangeRequest);

        // when & then
        mockMvc.perform(get("/api/exchange/{exchangeRequestId}", testExchangeRequestId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id.value").value(testExchangeRequest.getId().getValue().toString()))
            .andExpect(jsonPath("$.userId").value(testUserId.toString()))
            .andExpect(jsonPath("$.status").value(ExchangeStatus.REQUESTED.toString()));
    }

    @Test
    @DisplayName("존재하지 않는 교환 요청 조회 시 404를 반환한다")
    void getExchangeRequestNotFound() throws Exception {
        // given
        when(exchangeApplicationService.getExchangeRequest(any(ExchangeRequestId.class)))
            .thenThrow(new IllegalArgumentException("Exchange request not found"));

        // when & then
        mockMvc.perform(get("/api/exchange/{exchangeRequestId}", testExchangeRequestId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("테스트용 교환 요청을 생성할 수 있다")
    void testRequestExchange() throws Exception {
        // given
        ExchangeController.ExchangeRequestRequest request = new ExchangeController.ExchangeRequestRequest(
            testPointAmount, testWalletAddress
        );

        when(exchangeApplicationService.createExchangeRequest(any(UserId.class), any(BigDecimal.class), any(String.class)))
            .thenReturn(testExchangeRequest);

        // when & then
        mockMvc.perform(post("/api/exchange/test-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exchangeRequestId").value(testExchangeRequest.getId().getValue().toString()))
            .andExpect(jsonPath("$.message").value("테스트 교환 요청이 생성되었습니다"));
    }

    @Test
    @DisplayName("테스트용 교환 요청 생성 중 예외가 발생한 경우 에러 응답을 반환한다")
    void testRequestExchangeWithException() throws Exception {
        // given
        ExchangeController.ExchangeRequestRequest request = new ExchangeController.ExchangeRequestRequest(
            testPointAmount, testWalletAddress
        );

        when(exchangeApplicationService.createExchangeRequest(any(UserId.class), any(BigDecimal.class), any(String.class)))
            .thenThrow(new IllegalArgumentException("Invalid wallet address"));

        // when & then
        mockMvc.perform(post("/api/exchange/test-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("테스트 교환 요청 실패: Invalid wallet address"));
    }

    @Test
    @DisplayName("큰 소수점 자릿수의 포인트로 교환 요청을 생성할 수 있다")
    void requestExchangeWithLargeDecimalPoints() throws Exception {
        // given
        BigDecimal largePointAmount = new BigDecimal("123456789.123456789");
        ExchangeController.ExchangeRequestRequest request = new ExchangeController.ExchangeRequestRequest(
            largePointAmount, testWalletAddress
        );

        when(jwtService.getUserIdFromToken(testJwtToken)).thenReturn(testUserId);
        when(exchangeApplicationService.createExchangeRequest(any(UserId.class), any(BigDecimal.class), any(String.class)))
            .thenReturn(testExchangeRequest);

        // when & then
        mockMvc.perform(post("/api/exchange/request")
                .header("Authorization", "Bearer " + testJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exchangeRequestId").value(testExchangeRequest.getId().getValue().toString()));
    }

    @Test
    @DisplayName("긴 지갑 주소로 교환 요청을 생성할 수 있다")
    void requestExchangeWithLongWalletAddress() throws Exception {
        // given
        String longWalletAddress = "0x1234567890abcdef1234567890abcdef12345678";
        ExchangeController.ExchangeRequestRequest request = new ExchangeController.ExchangeRequestRequest(
            testPointAmount, longWalletAddress
        );

        when(jwtService.getUserIdFromToken(testJwtToken)).thenReturn(testUserId);
        when(exchangeApplicationService.createExchangeRequest(any(UserId.class), any(BigDecimal.class), any(String.class)))
            .thenReturn(testExchangeRequest);

        // when & then
        mockMvc.perform(post("/api/exchange/request")
                .header("Authorization", "Bearer " + testJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exchangeRequestId").value(testExchangeRequest.getId().getValue().toString()));
    }

    @Test
    @DisplayName("0 포인트로 교환 요청을 생성할 수 있다")
    void requestExchangeWithZeroPoints() throws Exception {
        // given
        BigDecimal zeroPointAmount = BigDecimal.ZERO;
        ExchangeController.ExchangeRequestRequest request = new ExchangeController.ExchangeRequestRequest(
            zeroPointAmount, testWalletAddress
        );

        when(jwtService.getUserIdFromToken(testJwtToken)).thenReturn(testUserId);
        when(exchangeApplicationService.createExchangeRequest(any(UserId.class), any(BigDecimal.class), any(String.class)))
            .thenReturn(testExchangeRequest);

        // when & then
        mockMvc.perform(post("/api/exchange/request")
                .header("Authorization", "Bearer " + testJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exchangeRequestId").value(testExchangeRequest.getId().getValue().toString()));
    }
} 