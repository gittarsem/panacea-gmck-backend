package com.panacea.backend.Entity;


import com.panacea.backend.Enum.BrCardPaymentStatus;
import com.panacea.backend.Enum.BrCardPaymentType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Super;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(
        name = "br_card_payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_br_card_payment_transaction_id",
                        columnNames = "transaction_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_br_card_payment_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_br_card_payment_br_card",
                        columnList = "br_card_id"
                )
        }
)
@SuperBuilder
public class BrCardPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "br_card_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_br_payment_card"
            )
    )
    private BrCard brCard;

    /**
     * Normalized UPI transaction / UTR reference.
     */
    @Column(
            name = "transaction_id",
            nullable = false,
            unique = true,
            length = 40
    )
    private String transactionId;

    /**
     * Expected amount determined by backend.
     */
    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "payment_screenshot_url",
            nullable = false,
            length = 500
    )
    private String paymentScreenshotUrl;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private BrCardPaymentStatus status;

    @Enumerated(EnumType.STRING)
    private BrCardPaymentType paymentType;

    @Column(
            name = "rejection_reason",
            length = 500
    )
    private String rejectionReason;

    @Column(
            name = "verified_by"
    )
    private Long verifiedBy;

    @Column(
            name = "submitted_at",
            nullable = false
    )
    private LocalDateTime submittedAt;

    @Column(
            name = "verified_at"
    )
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

}
