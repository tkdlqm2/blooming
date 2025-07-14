package com.bloominggrace.governance.wallet.domain.model;

import com.bloominggrace.governance.shared.domain.AggregateRoot;
import com.bloominggrace.governance.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor
public class Wallet extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true)
    private String walletAddress;

    @Column(nullable = false)
    private String encryptedPrivateKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NetworkType networkType;

    @Column(nullable = false)
    private boolean active = true;

    public Wallet(User user, String walletAddress, NetworkType networkType, String encryptedPrivateKey) {
        this.user = user;
        this.walletAddress = walletAddress;
        this.networkType = networkType;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.active = true;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }
} 