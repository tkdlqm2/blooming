package com.bloominggrace.governance.user.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRole enum 테스트")
class UserRoleTest {

    @Test
    @DisplayName("UserRole enum의 모든 값이 올바르게 정의되어 있다")
    void userRoleEnumValues() {
        // when
        UserRole[] roles = UserRole.values();

        // then
        assertThat(roles).hasSize(3);
        assertThat(roles).contains(UserRole.USER);
        assertThat(roles).contains(UserRole.ADMIN);
        assertThat(roles).contains(UserRole.MODERATOR);
    }

    @Test
    @DisplayName("UserRole enum의 각 값이 올바른 순서로 정의되어 있다")
    void userRoleEnumOrder() {
        // when
        UserRole[] roles = UserRole.values();

        // then
        assertThat(roles[0]).isEqualTo(UserRole.USER);
        assertThat(roles[1]).isEqualTo(UserRole.ADMIN);
        assertThat(roles[2]).isEqualTo(UserRole.MODERATOR);
    }

    @Test
    @DisplayName("UserRole enum의 각 값이 올바른 이름을 가진다")
    void userRoleEnumNames() {
        // when & then
        assertThat(UserRole.USER.name()).isEqualTo("USER");
        assertThat(UserRole.ADMIN.name()).isEqualTo("ADMIN");
        assertThat(UserRole.MODERATOR.name()).isEqualTo("MODERATOR");
    }

    @Test
    @DisplayName("UserRole enum의 값Of 메서드가 올바르게 작동한다")
    void userRoleValueOf() {
        // when & then
        assertThat(UserRole.valueOf("USER")).isEqualTo(UserRole.USER);
        assertThat(UserRole.valueOf("ADMIN")).isEqualTo(UserRole.ADMIN);
        assertThat(UserRole.valueOf("MODERATOR")).isEqualTo(UserRole.MODERATOR);
    }

    @Test
    @DisplayName("UserRole enum의 각 값이 고유하다")
    void userRoleEnumUniqueness() {
        // when
        UserRole user = UserRole.USER;
        UserRole admin = UserRole.ADMIN;
        UserRole moderator = UserRole.MODERATOR;

        // then
        assertThat(user).isNotEqualTo(admin);
        assertThat(user).isNotEqualTo(moderator);
        assertThat(admin).isNotEqualTo(moderator);
    }
} 