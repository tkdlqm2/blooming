package com.bloominggrace.governance.shared.security.domain.service;

public interface EncryptionService {
    String encrypt(String plainText);
    String decrypt(String encryptedText);
} 