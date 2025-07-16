package com.bloominggrace.governance.point.application.service;

import com.bloominggrace.governance.point.domain.model.PointAccount;
import com.bloominggrace.governance.point.domain.model.PointAmount;
import com.bloominggrace.governance.point.domain.model.PointTransaction;
import com.bloominggrace.governance.point.infrastructure.repository.PointAccountRepository;
import com.bloominggrace.governance.point.infrastructure.repository.PointTransactionRepository;
import com.bloominggrace.governance.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PointManagementService {

    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final DomainEventPublisher eventPublisher;

    // 포인트 적립
    public void earnPoints(UUID userId, PointAmount amount, String reason) {
        PointAccount account = getOrCreatePointAccount(userId);
        account.earnPoints(amount, reason);
        pointAccountRepository.save(account);
        eventPublisher.publishAll(account.getDomainEvents());
    }

    // 포인트 동결 (교환용)
    public void freezePoints(UUID userId, PointAmount amount, String exchangeRequestId) {
        PointAccount account = getPointAccount(userId);
        account.freezePoints(amount, exchangeRequestId);
        pointAccountRepository.save(account);
        eventPublisher.publishAll(account.getDomainEvents());
    }

    // 포인트 해제 (교환 취소시)
    public void unfreezePoints(UUID userId, PointAmount amount, String exchangeRequestId) {
        PointAccount account = getPointAccount(userId);
        account.unfreezePoints(amount, exchangeRequestId);
        pointAccountRepository.save(account);
        eventPublisher.publishAll(account.getDomainEvents());
    }

    // 무료 포인트 수령
    public void receiveFreePoints(UUID userId, PointAmount amount) {
        PointAccount account = getOrCreatePointAccount(userId);
        account.receiveFreePoints(amount);
        pointAccountRepository.save(account);
        eventPublisher.publishAll(account.getDomainEvents());
    }

    // 포인트 잔액 조회
    @Transactional(readOnly = true)
    public PointBalance getPointBalance(UUID userId) {
        PointAccount account = getPointAccount(userId);
        return new PointBalance(
            account.getAvailableBalance(),
            account.getFrozenBalance(),
            account.getTotalBalance()
        );
    }

    // 포인트 거래 내역 조회
    @Transactional(readOnly = true)
    public List<PointTransaction> getPointTransactions(UUID userId) {
        return pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public PointAccount getOrCreatePointAccount(UUID userId) {
        return pointAccountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    PointAccount newAccount = new PointAccount(userId);
                    return pointAccountRepository.save(newAccount);
                });
    }

    private PointAccount getPointAccount(UUID userId) {
        return pointAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 계정을 찾을 수 없습니다: " + userId));
    }

    public static class PointBalance {
        private final PointAmount availableBalance;
        private final PointAmount frozenBalance;
        private final PointAmount totalBalance;

        public PointBalance(PointAmount availableBalance, PointAmount frozenBalance, PointAmount totalBalance) {
            this.availableBalance = availableBalance;
            this.frozenBalance = frozenBalance;
            this.totalBalance = totalBalance;
        }

        public PointAmount getAvailableBalance() { return availableBalance; }
        public PointAmount getFrozenBalance() { return frozenBalance; }
        public PointAmount getTotalBalance() { return totalBalance; }
    }
} 