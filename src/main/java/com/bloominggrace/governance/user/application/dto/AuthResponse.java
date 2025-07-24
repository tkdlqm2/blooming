package com.bloominggrace.governance.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthResponse {
    
    private final String token;
    private final UUID userId;
    private final String email;
    private final String username;
    private final String role;
} 