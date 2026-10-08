package com.panacea.backend.Entity;


import com.panacea.backend.Enum.BrCardPaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
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

    public Long getId() {
        return id;
    }

    public BrCard getBrCard() {
        return brCard;
    }

    public void setBrCard(BrCard brCard) {
        this.brCard = brCard;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentScreenshotUrl() {
        return paymentScreenshotUrl;
    }

    public void setPaymentScreenshotUrl(
            String paymentScreenshotUrl
    ) {
        this.paymentScreenshotUrl =
                paymentScreenshotUrl;
    }

    public BrCardPaymentStatus getStatus() {
        return status;
    }

    public void setStatus(
            BrCardPaymentStatus status
    ) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(
            String rejectionReason
    ) {
        this.rejectionReason =
                rejectionReason;
    }

    public Long getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Long verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(
            LocalDateTime verifiedAt
    ) {
        this.verifiedAt = verifiedAt;
    }
}
