package com.bloominggrace.governance.user.application.service;

import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.user.domain.model.UserRole;
import com.bloominggrace.governance.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;
    private String testEmail;
    private String testUsername;
    private String testPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "test@example.com";
        testUsername = "testuser";
        testPassword = "password123";
        encodedPassword = "encodedPassword123";

        testUser = new User(testEmail, testUsername, encodedPassword, UserRole.USER);
        // Reflection을 사용하여 ID 설정 (실제로는 생성자에서 설정됨)
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, testUserId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("유효한 정보로 사용자를 생성할 수 있다")
    void createUserWithValidInfo() {
        // given
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        User createdUser = userService.createUser(testEmail, testUsername, testPassword, UserRole.USER);

        // then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo(testEmail);
        assertThat(createdUser.getUsername()).isEqualTo(testUsername);
        assertThat(createdUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(createdUser.getRole()).isEqualTo(UserRole.USER);

        verify(passwordEncoder).encode(testPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("ID로 사용자를 찾을 수 있다")
    void findUserById() {
        // given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // when
        Optional<User> foundUser = userService.findById(testUserId);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(testUser);
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 사용자를 찾으면 빈 Optional을 반환한다")
    void findUserByIdNotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        Optional<User> foundUser = userService.findById(nonExistentId);

        // then
        assertThat(foundUser).isEmpty();
        verify(userRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("모든 사용자를 조회할 수 있다")
    void findAllUsers() {
        // given
        User user1 = new User("user1@example.com", "user1", "password1", UserRole.USER);
        User user2 = new User("user2@example.com", "user2", "password2", UserRole.ADMIN);
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        // when
        List<User> foundUsers = userService.findAll();

        // then
        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).contains(user1, user2);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("이메일로 사용자 존재 여부를 확인할 수 있다")
    void existsByEmail() {
        // given
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // when
        boolean exists = userService.existsByEmail(testEmail);
        boolean notExists = userService.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
        verify(userRepository).existsByEmail(testEmail);
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("사용자명으로 사용자 존재 여부를 확인할 수 있다")
    void existsByUsername() {
        // given
        when(userRepository.existsByUsername(testUsername)).thenReturn(true);
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // when
        boolean exists = userService.existsByUsername(testUsername);
        boolean notExists = userService.existsByUsername("nonexistent");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
        verify(userRepository).existsByUsername(testUsername);
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 사용자 인증을 할 수 있다")
    void authenticateUserWithValidCredentials() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);

        // when
        Optional<User> authenticatedUser = userService.authenticateUser(testEmail, testPassword);

        // then
        assertThat(authenticatedUser).isPresent();
        assertThat(authenticatedUser.get()).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(testPassword, encodedPassword);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 사용자 인증을 하면 빈 Optional을 반환한다")
    void authenticateUserWithInvalidPassword() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", encodedPassword)).thenReturn(false);

        // when
        Optional<User> authenticatedUser = userService.authenticateUser(testEmail, "wrongpassword");

        // then
        assertThat(authenticatedUser).isEmpty();
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches("wrongpassword", encodedPassword);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 사용자 인증을 하면 빈 Optional을 반환한다")
    void authenticateUserWithNonExistentEmail() {
        // given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when
        Optional<User> authenticatedUser = userService.authenticateUser("nonexistent@example.com", testPassword);

        // then
        assertThat(authenticatedUser).isEmpty();
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("다양한 UserRole로 사용자를 생성할 수 있다")
    void createUserWithDifferentRoles() {
        // given
        User adminUser = new User("admin@example.com", "admin", encodedPassword, UserRole.ADMIN);
        User moderatorUser = new User("mod@example.com", "moderator", encodedPassword, UserRole.MODERATOR);
        
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser, adminUser, moderatorUser);

        // when & then
        User user = userService.createUser(testEmail, testUsername, testPassword, UserRole.USER);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);

        User admin = userService.createUser("admin@example.com", "admin", "password", UserRole.ADMIN);
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);

        User moderator = userService.createUser("mod@example.com", "moderator", "password", UserRole.MODERATOR);
        assertThat(moderator.getRole()).isEqualTo(UserRole.MODERATOR);

        verify(passwordEncoder, times(3)).encode(anyString());
        verify(userRepository, times(3)).save(any(User.class));
    }
} 