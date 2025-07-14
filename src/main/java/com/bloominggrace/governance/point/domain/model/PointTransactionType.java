package com.bloominggrace.governance.point.domain.model;

public enum PointTransactionType {
    EARN,           // 포인트 적립
    FREEZE,         // 포인트 동결 (교환용)
    UNFREEZE,       // 포인트 해제 (교환 취소)
    EXCHANGE        // 포인트 교환 (토큰으로 변환)
} 