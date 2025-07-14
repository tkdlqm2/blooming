package com.bloominggrace.governance.user.application.dto;

import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.user.domain.model.UserRole;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;

import java.util.List;
import java.util.UUID;

public class UserDto {
    private final UUID id;
    private final String email;
    private final String username;
    private final UserRole role;
    private final List<WalletDto> wallets;

    public UserDto(UUID id, String email, String username, UserRole role, List<WalletDto> wallets) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.wallets = wallets;
    }

    public static UserDto from(User user) {
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getRole(),
            user.getWallets() == null ? List.of() : user.getWallets().stream()
                .map(WalletDto::from)
                .collect(java.util.stream.Collectors.toList())
        );
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public List<WalletDto> getWallets() { return wallets; }
} 