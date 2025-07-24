package com.bloominggrace.governance.point.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReceiveFreePointsRequest {
    private BigDecimal amount;
} 