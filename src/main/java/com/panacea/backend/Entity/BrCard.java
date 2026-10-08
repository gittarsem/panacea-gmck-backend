package com.panacea.backend.Entity;


import com.panacea.backend.Enum.BrCardPassType;
import com.panacea.backend.Enum.BrCardStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "br_cards",
        indexes = {
                @Index(name = "idx_br_card_user", columnList = "user_id"),
                @Index(name = "idx_br_card_status", columnList = "status"),
                @Index(name = "idx_br_card_card_id", columnList = "card_id")
        }
)
public class BrCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * We keep this as a plain user ID rather than a JPA relationship
     * so this module does not tightly couple itself to UserEntity.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_type", nullable = false, length = 40)
    private BrCardPassType passType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BrCardStatus status;

    /**
     * Generated ONLY after payment approval.
     * Example: PN26-000427
     */
    @Column(name = "card_id", unique = true, length = 20)
    private String cardId;

    /**
     * Cryptographically random value used by the entry scanner.
     *
     * Do NOT put name/email/phone inside this token.
     */
    @Column(name = "qr_token", unique = true, length = 100)
    private String qrToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BrCardPassType getPassType() {
        return passType;
    }

    public void setPassType(BrCardPassType passType) {
        this.passType = passType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BrCardStatus getStatus() {
        return status;
    }

    public void setStatus(BrCardStatus status) {
        this.status = status;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
