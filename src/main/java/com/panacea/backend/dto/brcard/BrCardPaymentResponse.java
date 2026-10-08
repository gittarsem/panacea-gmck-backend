package com.panacea.backend.dto.brcard;

import com.panacea.backend.Enum.BrCardPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BrCardPaymentResponse(

        Long paymentId,

        Long brCardId,

        String transactionId,

        BigDecimal amount,

        BrCardPaymentStatus status,

        String rejectionReason,

        LocalDateTime submittedAt,

        LocalDateTime verifiedAt
) {
}