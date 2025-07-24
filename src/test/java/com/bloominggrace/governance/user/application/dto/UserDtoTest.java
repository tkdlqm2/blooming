package com.bloominggrace.governance.user.application.dto;

import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.user.domain.model.UserRole;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserDto 테스트")
class UserDtoTest {

    private User testUser;
    private UUID testUserId;
    private String testEmail;
    private String testUsername;
    private String testPassword;
    private UserRole testRole;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "test@example.com";
        testUsername = "testuser";
        testPassword = "password123";
        testRole = UserRole.USER;

        testUser = new User(testEmail, testUsername, testPassword, testRole);
        // Reflection을 사용하여 ID 설정
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, testUserId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("User 엔티티로부터 UserDto를 생성할 수 있다")
    void createUserDtoFromUser() {
        // when
        UserDto userDto = UserDto.from(testUser);

        // then
        assertThat(userDto).isNotNull();
        assertThat(userDto.getId()).isEqualTo(testUserId);
        assertThat(userDto.getEmail()).isEqualTo(testEmail);
        assertThat(userDto.getUsername()).isEqualTo(testUsername);
        assertThat(userDto.getRole()).isEqualTo(testRole);
        assertThat(userDto.getWallets()).isNotNull();
        assertThat(userDto.getWallets()).isEmpty();
    }

    @Test
    @DisplayName("UserDto의 모든 필드가 올바르게 설정된다")
    void userDtoFieldsAreCorrectlySet() {
        // when
        UserDto userDto = UserDto.from(testUser);

        // then
        assertThat(userDto.getId()).isEqualTo(testUserId);
        assertThat(userDto.getEmail()).isEqualTo(testEmail);
        assertThat(userDto.getUsername()).isEqualTo(testUsername);
        assertThat(userDto.getRole()).isEqualTo(testRole);
    }

    @Test
    @DisplayName("다양한 UserRole로 UserDto를 생성할 수 있다")
    void createUserDtoWithDifferentRoles() {
        // given
        User adminUser = new User("admin@example.com", "admin", "password", UserRole.ADMIN);
        User moderatorUser = new User("mod@example.com", "moderator", "password", UserRole.MODERATOR);

        // when
        UserDto adminDto = UserDto.from(adminUser);
        UserDto moderatorDto = UserDto.from(moderatorUser);

        // then
        assertThat(adminDto.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(moderatorDto.getRole()).isEqualTo(UserRole.MODERATOR);
    }

    @Test
    @DisplayName("UserDto 생성자로 직접 생성할 수 있다")
    void createUserDtoWithConstructor() {
        // given
        List<WalletDto> wallets = Arrays.asList(
            WalletDto.builder()
                .walletAddress("0x123")
                .networkType(NetworkType.ETHEREUM)
                .active(true)
                .build(),
            WalletDto.builder()
                .walletAddress("0x456")
                .networkType(NetworkType.SOLANA)
                .active(true)
                .build()
        );

        // when
        UserDto userDto = new UserDto(testUserId, testEmail, testUsername, testRole, wallets);

        // then
        assertThat(userDto.getId()).isEqualTo(testUserId);
        assertThat(userDto.getEmail()).isEqualTo(testEmail);
        assertThat(userDto.getUsername()).isEqualTo(testUsername);
        assertThat(userDto.getRole()).isEqualTo(testRole);
        assertThat(userDto.getWallets()).hasSize(2);
        assertThat(userDto.getWallets()).containsAll(wallets);
    }

    @Test
    @DisplayName("User의 wallets가 null일 때 빈 리스트로 변환된다")
    void handleNullWallets() {
        // given
        User userWithNullWallets = new User(testEmail, testUsername, testPassword, testRole);
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userWithNullWallets, testUserId);
            
            var walletsField = User.class.getDeclaredField("wallets");
            walletsField.setAccessible(true);
            walletsField.set(userWithNullWallets, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // when
        UserDto userDto = UserDto.from(userWithNullWallets);

        // then
        assertThat(userDto.getWallets()).isNotNull();
        assertThat(userDto.getWallets()).isEmpty();
    }

    @Test
    @DisplayName("User의 wallets가 비어있을 때 빈 리스트로 변환된다")
    void handleEmptyWallets() {
        // given
        User userWithEmptyWallets = new User(testEmail, testUsername, testPassword, testRole);
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userWithEmptyWallets, testUserId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // when
        UserDto userDto = UserDto.from(userWithEmptyWallets);

        // then
        assertThat(userDto.getWallets()).isNotNull();
        assertThat(userDto.getWallets()).isEmpty();
    }

    @Test
    @DisplayName("UserDto의 equals와 hashCode가 올바르게 작동한다")
    void userDtoEqualsAndHashCode() {
        // given
        UserDto userDto1 = UserDto.from(testUser);
        UserDto userDto2 = UserDto.from(testUser);

        // when & then
        assertThat(userDto1).isEqualTo(userDto2);
        assertThat(userDto1.hashCode()).isEqualTo(userDto2.hashCode());
    }

    @Test
    @DisplayName("UserDto의 toString이 올바르게 작동한다")
    void userDtoToString() {
        // when
        UserDto userDto = UserDto.from(testUser);
        String toString = userDto.toString();

        // then
        assertThat(toString).isNotNull();
        assertThat(toString).contains(testEmail);
        assertThat(toString).contains(testUsername);
        assertThat(toString).contains(testRole.name());
    }
} 