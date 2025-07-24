package com.bloominggrace.governance.user.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User 도메인 모델 테스트")
class UserTest {

    private String validEmail;
    private String validUsername;
    private String validPassword;
    private UserRole validRole;

    @BeforeEach
    void setUp() {
        validEmail = "test@example.com";
        validUsername = "testuser";
        validPassword = "password123";
        validRole = UserRole.USER;
    }

    @Test
    @DisplayName("유효한 정보로 User를 생성할 수 있다")
    void createUserWithValidInfo() {
        // when
        User user = new User(validEmail, validUsername, validPassword, validRole);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo(validEmail);
        assertThat(user.getUsername()).isEqualTo(validUsername);
        assertThat(user.getPassword()).isEqualTo(validPassword);
        assertThat(user.getRole()).isEqualTo(validRole);
        assertThat(user.getWallets()).isNotNull();
        assertThat(user.getWallets()).isEmpty();
    }

    @Test
    @DisplayName("User 생성 시 고유한 ID가 생성된다")
    void createUserWithUniqueId() {
        // when
        User user1 = new User("user1@example.com", "user1", "password1", UserRole.USER);
        User user2 = new User("user2@example.com", "user2", "password2", UserRole.ADMIN);

        // then
        assertThat(user1.getId()).isNotNull();
        assertThat(user2.getId()).isNotNull();
        assertThat(user1.getId()).isNotEqualTo(user2.getId());
    }

    @Test
    @DisplayName("다양한 UserRole로 User를 생성할 수 있다")
    void createUserWithDifferentRoles() {
        // when & then
        User user = new User(validEmail, validUsername, validPassword, UserRole.USER);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);

        User admin = new User("admin@example.com", "admin", "password", UserRole.ADMIN);
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);

        User moderator = new User("mod@example.com", "moderator", "password", UserRole.MODERATOR);
        assertThat(moderator.getRole()).isEqualTo(UserRole.MODERATOR);
    }

    @Test
    @DisplayName("User 생성 시 wallets 리스트가 초기화된다")
    void createUserWithInitializedWallets() {
        // when
        User user = new User(validEmail, validUsername, validPassword, validRole);

        // then
        assertThat(user.getWallets()).isNotNull();
        assertThat(user.getWallets()).isEmpty();
    }

    @Test
    @DisplayName("null 값으로 User를 생성할 수 있다")
    void createUserWithNullValues() {
        // when
        User user = new User(null, null, null, null);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRole()).isNull();
    }

    @Test
    @DisplayName("User의 기본 생성자가 작동한다")
    void createUserWithDefaultConstructor() {
        // when
        User user = new User();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(user.getWallets()).isNotNull();
    }
} 