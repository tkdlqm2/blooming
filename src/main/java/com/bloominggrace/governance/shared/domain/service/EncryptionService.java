package com.bloominggrace.governance.shared.domain.service;

public interface EncryptionService {
    String encrypt(String plainText);
    String decrypt(String encryptedText);
} 