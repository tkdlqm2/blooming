package com.bloominggrace.governance.wallet.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.security.domain.service.EncryptionService;
import com.bloominggrace.governance.user.application.service.UserService;
import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.user.domain.model.UserRole;
import com.bloominggrace.governance.wallet.application.dto.CreateWalletRequest;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletApplicationService 테스트")
class WalletApplicationServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletServiceFactory walletServiceFactory;

    @Mock
    private UserService userService;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletApplicationService walletApplicationService;

    private User testUser;
    private Wallet testWallet;
    private UUID testUserId;
    private UserId userId;
    private String testWalletAddress;
    private String testEncryptedPrivateKey;
    private String testDecryptedPrivateKey;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        userId = new UserId(testUserId);
        testUser = new User("test@example.com", "testuser", "password123", UserRole.USER);
        testWalletAddress = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6";
        testEncryptedPrivateKey = "encrypted_private_key_123";
        testDecryptedPrivateKey = "decrypted_private_key_123";

        testWallet = new Wallet(testUser, testWalletAddress, NetworkType.ETHEREUM, testEncryptedPrivateKey);
    }

    @Test
    @DisplayName("유효한 정보로 지갑을 생성할 수 있다")
    void createWalletWithValidInfo() {
        // given
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(testUserId.toString());
        request.setNetworkType("ETHEREUM");

        when(userService.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(walletServiceFactory.getWalletService(NetworkType.ETHEREUM)).thenReturn(walletService);
        when(walletService.createWallet(userId, NetworkType.ETHEREUM)).thenReturn(testWallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // when
        WalletDto result = walletApplicationService.createWallet(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWalletAddress()).isEqualTo(testWalletAddress);
        assertThat(result.getNetworkType()).isEqualTo(NetworkType.ETHEREUM);

        verify(userService).findById(testUserId);
        verify(walletServiceFactory).getWalletService(NetworkType.ETHEREUM);
        verify(walletService).createWallet(userId, NetworkType.ETHEREUM);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 지갑 생성 시 예외가 발생한다")
    void createWalletWithNonExistentUser() {
        // given
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(testUserId.toString());
        request.setNetworkType("ETHEREUM");

        when(userService.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletApplicationService.createWallet(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(userService).findById(testUserId);
        verify(walletServiceFactory, never()).getWalletService(any(NetworkType.class));
    }

    @Test
    @DisplayName("복호화된 개인키를 가져올 수 있다")
    void getDecryptedPrivateKey() {
        // given
        when(walletRepository.findByUser_IdAndNetworkType(testUserId, NetworkType.ETHEREUM))
            .thenReturn(Optional.of(testWallet));
        when(encryptionService.decrypt(testEncryptedPrivateKey)).thenReturn(testDecryptedPrivateKey);

        // when
        String result = walletApplicationService.getDecryptedPrivateKey(userId, NetworkType.ETHEREUM);

        // then
        assertThat(result).isEqualTo(testDecryptedPrivateKey);
        verify(walletRepository).findByUser_IdAndNetworkType(testUserId, NetworkType.ETHEREUM);
        verify(encryptionService).decrypt(testEncryptedPrivateKey);
    }

    @Test
    @DisplayName("존재하지 않는 지갑으로 개인키 조회 시 예외가 발생한다")
    void getDecryptedPrivateKeyWithNonExistentWallet() {
        // given
        when(walletRepository.findByUser_IdAndNetworkType(testUserId, NetworkType.ETHEREUM))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletApplicationService.getDecryptedPrivateKey(userId, NetworkType.ETHEREUM))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("지갑을 찾을 수 없습니다");

        verify(walletRepository).findByUser_IdAndNetworkType(testUserId, NetworkType.ETHEREUM);
        verify(encryptionService, never()).decrypt(anyString());
    }

    @Test
    @DisplayName("복호화 실패 시 예외가 발생한다")
    void getDecryptedPrivateKeyWithDecryptionFailure() {
        // given
        when(walletRepository.findByUser_IdAndNetworkType(testUserId, NetworkType.ETHEREUM))
            .thenReturn(Optional.of(testWallet));
        when(encryptionService.decrypt(testEncryptedPrivateKey))
            .thenThrow(new RuntimeException("Decryption failed"));

        // when & then
        assertThatThrownBy(() -> walletApplicationService.getDecryptedPrivateKey(userId, NetworkType.ETHEREUM))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("프라이빗 키 복호화에 실패했습니다");

        verify(walletRepository).findByUser_IdAndNetworkType(testUserId, NetworkType.ETHEREUM);
        verify(encryptionService).decrypt(testEncryptedPrivateKey);
    }

    @Test
    @DisplayName("ID로 지갑을 찾을 수 있다")
    void findById() {
        // given
        when(walletRepository.findById(testUserId)).thenReturn(Optional.of(testWallet));

        // when
        Optional<Wallet> result = walletApplicationService.findById(testUserId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testWallet);
        verify(walletRepository).findById(testUserId);
    }

    @Test
    @DisplayName("모든 지갑을 조회할 수 있다")
    void findAll() {
        // given
        Wallet wallet1 = new Wallet(testUser, "0x123", NetworkType.ETHEREUM, "key1");
        Wallet wallet2 = new Wallet(testUser, "0x456", NetworkType.SOLANA, "key2");
        List<Wallet> wallets = Arrays.asList(wallet1, wallet2);

        when(walletRepository.findAll()).thenReturn(wallets);

        // when
        List<Wallet> result = walletApplicationService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(wallet1, wallet2);
        verify(walletRepository).findAll();
    }

    @Test
    @DisplayName("지갑 활성화 상태를 변경할 수 있다")
    void updateWalletActiveStatus() {
        // given
        when(walletServiceFactory.getWalletService(NetworkType.ETHEREUM)).thenReturn(walletService);
        when(walletService.updateActiveStatus(testWalletAddress, false)).thenReturn(testWallet);

        // when
        Wallet result = walletApplicationService.updateWalletActiveStatus(testWalletAddress, false, NetworkType.ETHEREUM);

        // then
        assertThat(result).isEqualTo(testWallet);
        verify(walletServiceFactory).getWalletService(NetworkType.ETHEREUM);
        verify(walletService).updateActiveStatus(testWalletAddress, false);
    }

    @Test
    @DisplayName("사용자 ID로 지갑 목록을 조회할 수 있다")
    void findByUserId() {
        // given
        Wallet wallet1 = new Wallet(testUser, "0x123", NetworkType.ETHEREUM, "key1");
        Wallet wallet2 = new Wallet(testUser, "0x456", NetworkType.SOLANA, "key2");
        List<Wallet> wallets = Arrays.asList(wallet1, wallet2);

        when(walletRepository.findByUserId(userId)).thenReturn(wallets);

        // when
        List<Wallet> result = walletApplicationService.findByUserId(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(wallet1, wallet2);
        verify(walletRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("지갑 주소로 지갑을 찾을 수 있다")
    void getWalletByAddress() {
        // given
        when(walletRepository.findByWalletAddress(testWalletAddress)).thenReturn(Optional.of(testWallet));

        // when
        Optional<Wallet> result = walletApplicationService.getWalletByAddress(testWalletAddress);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testWallet);
        verify(walletRepository).findByWalletAddress(testWalletAddress);
    }

    @Test
    @DisplayName("지갑을 잠금 해제할 수 있다")
    void unlockWallet() {
        // given
        WalletService.UnlockResult unlockResult = WalletService.UnlockResult.success(testWalletAddress, testWalletAddress, true);
        
        when(walletServiceFactory.getWalletService(NetworkType.ETHEREUM)).thenReturn(walletService);
        when(walletService.unlockWallet(testWalletAddress)).thenReturn(unlockResult);

        // when
        WalletService.UnlockResult result = walletApplicationService.unlockWallet(testWalletAddress, NetworkType.ETHEREUM);

        // then
        assertThat(result).isEqualTo(unlockResult);
        assertThat(result.isSuccess()).isTrue();
        verify(walletServiceFactory).getWalletService(NetworkType.ETHEREUM);
        verify(walletService).unlockWallet(testWalletAddress);
    }

    @Test
    @DisplayName("다양한 NetworkType으로 지갑을 생성할 수 있다")
    void createWalletWithDifferentNetworkTypes() {
        // given
        CreateWalletRequest ethereumRequest = new CreateWalletRequest();
        ethereumRequest.setUserId(testUserId.toString());
        ethereumRequest.setNetworkType("ETHEREUM");

        CreateWalletRequest solanaRequest = new CreateWalletRequest();
        solanaRequest.setUserId(testUserId.toString());
        solanaRequest.setNetworkType("SOLANA");

        when(userService.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(walletServiceFactory.getWalletService(NetworkType.ETHEREUM)).thenReturn(walletService);
        when(walletServiceFactory.getWalletService(NetworkType.SOLANA)).thenReturn(walletService);
        when(walletService.createWallet(any(UserId.class), any(NetworkType.class))).thenReturn(testWallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // when & then
        WalletDto ethereumResult = walletApplicationService.createWallet(ethereumRequest);
        assertThat(ethereumResult).isNotNull();

        WalletDto solanaResult = walletApplicationService.createWallet(solanaRequest);
        assertThat(solanaResult).isNotNull();

        verify(walletServiceFactory).getWalletService(NetworkType.ETHEREUM);
        verify(walletServiceFactory).getWalletService(NetworkType.SOLANA);
    }
} 