package com.bloominggrace.governance.exchange.domain.model;

public enum ExchangeStatus {
    REQUESTED,      // 교환 요청됨
    PROCESSING,     // 처리 중
    COMPLETED,      // 완료
    CANCELLED,      // 취소됨
    FAILED          // 실패
} 