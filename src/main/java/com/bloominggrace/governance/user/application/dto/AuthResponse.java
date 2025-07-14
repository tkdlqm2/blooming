package com.bloominggrace.governance.user.application.dto;

import java.util.UUID;

public class AuthResponse {
    
    private final String token;
    private final UUID userId;
    private final String email;
    private final String username;
    private final String role;
    
    public AuthResponse(String token, UUID userId, String email, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.role = role;
    }
    
    public String getToken() {
        return token;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getRole() {
        return role;
    }
} 