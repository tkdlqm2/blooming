package com.bloominggrace.governance.shared.infrastructure.service;

import com.bloominggrace.governance.blockchain.domain.service.BlockchainClient;
import com.bloominggrace.governance.blockchain.application.service.BlockchainClientFactory;
import com.bloominggrace.governance.blockchain.infrastructure.service.ethereum.EthereumBlockchainClient;
import com.bloominggrace.governance.governance.infrastructure.service.BlockchainGovernanceServiceFactory;
import com.bloominggrace.governance.governance.infrastructure.service.ethereum.EthereumGovernanceContractService;
import com.bloominggrace.governance.shared.domain.service.TransactionBuilder;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.infrastructure.service.AdminWalletService;
import com.bloominggrace.governance.shared.infrastructure.service.EthereumTransactionBuilder;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionOrchestrator ERC20 Transfer Tests")
class TransactionOrchestratorTest {

    @Mock
    private BlockchainGovernanceServiceFactory governanceServiceFactory;

    @Mock
    private BlockchainClientFactory blockchainClientFactory;

    @Mock
    private WalletApplicationService walletApplicationService;

    @Mock
    private AdminWalletService adminWalletService;

    @Mock
    private TransactionBuilderFactory transactionBuilderFactory;

    @Mock
    private EthereumTransactionBuilder ethereumTransactionBuilder;

    @Mock
    private BlockchainClient blockchainClient;

    @Mock
    private EthereumBlockchainClient ethereumBlockchainClient;

    @Mock
    private EthereumGovernanceContractService ethereumGovernanceContractService;

    @InjectMocks
    private TransactionOrchestrator transactionOrchestrator;

    private static final String FROM_ADDRESS = "0x55D5c49e36f8A89111687C9DC8355121068f0cD8";
    private static final String TO_ADDRESS = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6";
    private static final String TOKEN_CONTRACT = "0x1234567890123456789012345678901234567890";
    private static final BigDecimal AMOUNT = new BigDecimal("100.0");
    private static final String TRANSACTION_HASH = "0x" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

    @BeforeEach
    void setUp() {
        // Mock TransactionBuilderFactory
        when(transactionBuilderFactory.getBuilder(NetworkType.ETHEREUM)).thenReturn(ethereumTransactionBuilder);
        
        // Mock BlockchainClientFactory
        when(blockchainClientFactory.getClient(NetworkType.ETHEREUM)).thenReturn(blockchainClient);
    }

    @Test
    @DisplayName("ERC20 전송 성공 테스트")
    void executeTransfer_Success() throws Exception {
        // Given
        TransactionBody<Object> mockTxBody = createMockTransactionBody();
        byte[] mockSignedTx = "mockSignedTransaction".getBytes();

        when(ethereumTransactionBuilder.buildErc20TransferTxBody(
            eq(FROM_ADDRESS), eq(TO_ADDRESS), eq(AMOUNT), eq(TOKEN_CONTRACT), eq(NetworkType.ETHEREUM)
        )).thenReturn(mockTxBody);

        when(walletApplicationService.signTransactionBody(eq(mockTxBody), eq(FROM_ADDRESS)))
            .thenReturn(mockSignedTx);

        when(blockchainClient.broadcastTransaction(anyString()))
            .thenReturn(TRANSACTION_HASH);

        // When
        TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
            FROM_ADDRESS, TO_ADDRESS, NetworkType.ETHEREUM, AMOUNT, TOKEN_CONTRACT
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(TRANSACTION_HASH, result.getTransactionHash());
        assertEquals(FROM_ADDRESS, result.getWalletAddress());
        assertEquals(NetworkType.ETHEREUM.name(), result.getNetworkType());
        assertTrue(result.getErrorMessage().contains("ERC20 transfer: 100.0"));

        // Verify interactions
        verify(ethereumTransactionBuilder).buildErc20TransferTxBody(
            FROM_ADDRESS, TO_ADDRESS, AMOUNT, TOKEN_CONTRACT, NetworkType.ETHEREUM
        );
        verify(walletApplicationService).signTransactionBody(mockTxBody, FROM_ADDRESS);
        verify(blockchainClient).broadcastTransaction(anyString());
    }

    @Test
    @DisplayName("ERC20 전송 실패 - 블록체인 브로드캐스트 실패")
    void executeTransfer_BroadcastFailure() throws Exception {
        // Given
        TransactionBody<Object> mockTxBody = createMockTransactionBody();
        byte[] mockSignedTx = "mockSignedTransaction".getBytes();

        when(ethereumTransactionBuilder.buildErc20TransferTxBody(
            eq(FROM_ADDRESS), eq(TO_ADDRESS), eq(AMOUNT), eq(TOKEN_CONTRACT), eq(NetworkType.ETHEREUM)
        )).thenReturn(mockTxBody);

        when(walletApplicationService.signTransactionBody(eq(mockTxBody), eq(FROM_ADDRESS)))
            .thenReturn(mockSignedTx);

        when(blockchainClient.broadcastTransaction(anyString()))
            .thenReturn(null); // 브로드캐스트 실패

        // When
        TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
            FROM_ADDRESS, TO_ADDRESS, NetworkType.ETHEREUM, AMOUNT, TOKEN_CONTRACT
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getTransactionHash());
        assertTrue(result.getErrorMessage().contains("No transaction hash returned from blockchain broadcast"));

        // Verify interactions
        verify(ethereumTransactionBuilder).buildErc20TransferTxBody(
            FROM_ADDRESS, TO_ADDRESS, AMOUNT, TOKEN_CONTRACT, NetworkType.ETHEREUM
        );
        verify(walletApplicationService).signTransactionBody(mockTxBody, FROM_ADDRESS);
        verify(blockchainClient).broadcastTransaction(anyString());
    }

    @Test
    @DisplayName("ERC20 전송 실패 - 빈 트랜잭션 해시")
    void executeTransfer_EmptyTransactionHash() throws Exception {
        // Given
        TransactionBody<Object> mockTxBody = createMockTransactionBody();
        byte[] mockSignedTx = "mockSignedTransaction".getBytes();

        when(ethereumTransactionBuilder.buildErc20TransferTxBody(
            eq(FROM_ADDRESS), eq(TO_ADDRESS), eq(AMOUNT), eq(TOKEN_CONTRACT), eq(NetworkType.ETHEREUM)
        )).thenReturn(mockTxBody);

        when(walletApplicationService.signTransactionBody(eq(mockTxBody), eq(FROM_ADDRESS)))
            .thenReturn(mockSignedTx);

        when(blockchainClient.broadcastTransaction(anyString()))
            .thenReturn(""); // 빈 해시

        // When
        TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
            FROM_ADDRESS, TO_ADDRESS, NetworkType.ETHEREUM, AMOUNT, TOKEN_CONTRACT
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getTransactionHash());
        assertTrue(result.getErrorMessage().contains("No transaction hash returned from blockchain broadcast"));
    }

    @Test
    @DisplayName("ERC20 전송 실패 - 트랜잭션 빌더 예외")
    void executeTransfer_TransactionBuilderException() throws Exception {
        // Given
        when(ethereumTransactionBuilder.buildErc20TransferTxBody(
            eq(FROM_ADDRESS), eq(TO_ADDRESS), eq(AMOUNT), eq(TOKEN_CONTRACT), eq(NetworkType.ETHEREUM)
        )).thenThrow(new RuntimeException("Transaction builder failed"));

        // When
        TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
            FROM_ADDRESS, TO_ADDRESS, NetworkType.ETHEREUM, AMOUNT, TOKEN_CONTRACT
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getTransactionHash());
        assertTrue(result.getErrorMessage().contains("ERC20 transfer failed: Transaction builder failed"));

        // Verify no further interactions
        verify(walletApplicationService, never()).signTransactionBody(any(), any());
        verify(blockchainClient, never()).broadcastTransaction(any());
    }

    @Test
    @DisplayName("ERC20 전송 실패 - 서명 실패")
    void executeTransfer_SigningFailure() throws Exception {
        // Given
        TransactionBody<Object> mockTxBody = createMockTransactionBody();

        when(ethereumTransactionBuilder.buildErc20TransferTxBody(
            eq(FROM_ADDRESS), eq(TO_ADDRESS), eq(AMOUNT), eq(TOKEN_CONTRACT), eq(NetworkType.ETHEREUM)
        )).thenReturn(mockTxBody);

        when(walletApplicationService.signTransactionBody(eq(mockTxBody), eq(FROM_ADDRESS)))
            .thenThrow(new RuntimeException("Signing failed"));

        // When
        TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
            FROM_ADDRESS, TO_ADDRESS, NetworkType.ETHEREUM, AMOUNT, TOKEN_CONTRACT
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getTransactionHash());
        assertTrue(result.getErrorMessage().contains("ERC20 transfer failed: Signing failed"));

        // Verify interactions
        verify(ethereumTransactionBuilder).buildErc20TransferTxBody(
            FROM_ADDRESS, TO_ADDRESS, AMOUNT, TOKEN_CONTRACT, NetworkType.ETHEREUM
        );
        verify(walletApplicationService).signTransactionBody(mockTxBody, FROM_ADDRESS);
        verify(blockchainClient, never()).broadcastTransaction(any());
    }

    @Test
    @DisplayName("ERC20 전송 실패 - 블록체인 클라이언트 예외")
    void executeTransfer_BlockchainClientException() throws Exception {
        // Given
        TransactionBody<Object> mockTxBody = createMockTransactionBody();
        byte[] mockSignedTx = "mockSignedTransaction".getBytes();

        when(ethereumTransactionBuilder.buildErc20TransferTxBody(
            eq(FROM_ADDRESS), eq(TO_ADDRESS), eq(AMOUNT), eq(TOKEN_CONTRACT), eq(NetworkType.ETHEREUM)
        )).thenReturn(mockTxBody);

        when(walletApplicationService.signTransactionBody(eq(mockTxBody), eq(FROM_ADDRESS)))
            .thenReturn(mockSignedTx);

        when(blockchainClient.broadcastTransaction(anyString()))
            .thenThrow(new RuntimeException("Blockchain client error"));

        // When
        TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
            FROM_ADDRESS, TO_ADDRESS, NetworkType.ETHEREUM, AMOUNT, TOKEN_CONTRACT
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getTransactionHash());
        assertTrue(result.getErrorMessage().contains("ERC20 transfer failed: Blockchain client error"));
    }

    @Test
    @DisplayName("ERC20 전송 실패 - 지원하지 않는 네트워크")
    void executeTransfer_UnsupportedNetwork() {
        // When
        TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
            FROM_ADDRESS, TO_ADDRESS, NetworkType.SOLANA, AMOUNT, TOKEN_CONTRACT
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getTransactionHash());
        assertTrue(result.getErrorMessage().contains("ERC20 transfer is only supported for Ethereum"));

        // Verify no interactions
        verify(ethereumTransactionBuilder, never()).buildErc20TransferTxBody(any(), any(), any(), any(), any());
        verify(walletApplicationService, never()).signTransactionBody(any(), any());
        verify(blockchainClient, never()).broadcastTransaction(any());
    }

    @Test
    @DisplayName("ERC20 전송 - 다양한 금액 테스트")
    void executeTransfer_VariousAmounts() throws Exception {
        // Given
        BigDecimal[] amounts = {
            new BigDecimal("0.000001"),
            new BigDecimal("1.0"),
            new BigDecimal("1000.0"),
            new BigDecimal("999999.999999")
        };

        TransactionBody<Object> mockTxBody = createMockTransactionBody();
        byte[] mockSignedTx = "mockSignedTransaction".getBytes();

        when(ethereumTransactionBuilder.buildErc20TransferTxBody(
            anyString(), anyString(), any(BigDecimal.class), anyString(), eq(NetworkType.ETHEREUM)
        )).thenReturn(mockTxBody);

        when(walletApplicationService.signTransactionBody(eq(mockTxBody), eq(FROM_ADDRESS)))
            .thenReturn(mockSignedTx);

        when(blockchainClient.broadcastTransaction(anyString()))
            .thenReturn(TRANSACTION_HASH);

        // When & Then
        for (BigDecimal amount : amounts) {
            TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
                FROM_ADDRESS, TO_ADDRESS, NetworkType.ETHEREUM, amount, TOKEN_CONTRACT
            );

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TRANSACTION_HASH, result.getTransactionHash());
            assertTrue(result.getErrorMessage().contains("ERC20 transfer: " + amount));
        }
    }

    @Test
    @DisplayName("ERC20 전송 - 다양한 주소 형식 테스트")
    void executeTransfer_VariousAddresses() throws Exception {
        // Given
        String[] addresses = {
            "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
            "0x1234567890123456789012345678901234567890",
            "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd"
        };

        TransactionBody<Object> mockTxBody = createMockTransactionBody();
        byte[] mockSignedTx = "mockSignedTransaction".getBytes();

        when(ethereumTransactionBuilder.buildErc20TransferTxBody(
            anyString(), anyString(), eq(AMOUNT), anyString(), eq(NetworkType.ETHEREUM)
        )).thenReturn(mockTxBody);

        when(walletApplicationService.signTransactionBody(eq(mockTxBody), anyString()))
            .thenReturn(mockSignedTx);

        when(blockchainClient.broadcastTransaction(anyString()))
            .thenReturn(TRANSACTION_HASH);

        // When & Then
        for (String address : addresses) {
            TransactionOrchestrator.TransactionResult result = transactionOrchestrator.executeTransfer(
                FROM_ADDRESS, address, NetworkType.ETHEREUM, AMOUNT, TOKEN_CONTRACT
            );

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TRANSACTION_HASH, result.getTransactionHash());
        }
    }

    private TransactionBody<Object> createMockTransactionBody() {
        return TransactionBody.builder()
            .type(TransactionBody.TransactionType.TOKEN_TRANSFER)
            .fromAddress(FROM_ADDRESS)
            .toAddress(TOKEN_CONTRACT)
            .data("0xmockData")
            .networkType(NetworkType.ETHEREUM.name())
            .build();
    }
} 