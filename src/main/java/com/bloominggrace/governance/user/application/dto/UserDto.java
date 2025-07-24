package com.bloominggrace.governance.user.application.dto;

import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.user.domain.model.UserRole;
import com.bloominggrace.governance.wallet.application.dto.WalletDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserDto {
    private final UUID id;
    private final String email;
    private final String username;
    private final UserRole role;
    private final List<WalletDto> wallets;

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
} 