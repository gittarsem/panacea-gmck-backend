package com.panacea.backend.dto.brcard;




import com.panacea.backend.Enum.BrCardPassType;
import com.panacea.backend.Enum.BrCardStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record BrCardResponse(

        Long id,

        String cardId,

        BrCardPassType passType,

        BigDecimal amount,

        BrCardStatus status,

        String qrToken,

        String transactionId,

        boolean upgradePending,

        String rejectionReason,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
