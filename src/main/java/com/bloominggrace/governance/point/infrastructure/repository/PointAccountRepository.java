package com.bloominggrace.governance.point.infrastructure.repository;

import com.bloominggrace.governance.point.domain.model.PointAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PointAccountRepository extends JpaRepository<PointAccount, UUID> {
    Optional<PointAccount> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
} 