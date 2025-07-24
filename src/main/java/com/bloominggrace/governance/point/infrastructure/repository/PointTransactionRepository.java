package com.bloominggrace.governance.point.infrastructure.repository;

import com.bloominggrace.governance.point.domain.model.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, UUID> {
    List<PointTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
} 