package com.panacea.backend.dto.brcard;

import com.panacea.backend.Enum.BrCardPassType;
import com.panacea.backend.Enum.BrCardPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminBrCardResponse(

        Long paymentId,

        Long brCardId,

        Long userId,

        String cardId,

        BrCardPassType passType,

        BigDecimal expectedAmount,

        String transactionId,

        String paymentScreenshotUrl,

        BrCardPaymentStatus paymentStatus,

        String rejectionReason,

        LocalDateTime submittedAt,

        LocalDateTime verifiedAt
) {
}