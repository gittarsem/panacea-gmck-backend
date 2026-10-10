package com.panacea.backend.repository;



import com.panacea.backend.Entity.BrCard;
import com.panacea.backend.Entity.BrCardPayment;
import com.panacea.backend.Enum.BrCardPaymentStatus;
import com.panacea.backend.Enum.BrCardPaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BrCardPaymentRepository
        extends JpaRepository<BrCardPayment, Long> {

    Optional<BrCardPayment>
    findByTransactionId(String transactionId);

    List<BrCardPayment>
    findAllByStatusOrderBySubmittedAtAsc(
            BrCardPaymentStatus status
    );

    List<BrCardPayment>
    findAllByBrCardIdOrderBySubmittedAtDesc(
            Long brCardId
    );

    Optional<BrCardPayment>
    findFirstByBrCardIdOrderBySubmittedAtDesc(
            Long brCardId
    );

    boolean existsByTransactionId(
            String transactionId
    );

    long countByStatus(BrCardPaymentStatus status);

    @Query("""
    SELECT p
    FROM BrCardPayment p
    JOIN p.brCard c
    WHERE p.status = com.panacea.backend.Enum.BrCardPaymentStatus.APPROVED
      AND c.status = com.panacea.backend.Enum.BrCardStatus.ACTIVE
    ORDER BY p.verifiedAt DESC
""")
    List<BrCardPayment> findActiveBrCardPayments();

    Optional<BrCardPayment> findFirstByBrCardIdAndAmountAndStatus(
            Long brCardId,
            BigDecimal amount,
            BrCardPaymentStatus status
    );

    boolean existsByBrCardIdAndPaymentTypeAndStatus(
            Long brCardId,
            BrCardPaymentType paymentType,
            BrCardPaymentStatus status
    );

    Optional<BrCardPayment> findFirstByBrCardIdAndPaymentTypeAndStatusOrderBySubmittedAtDesc(
            Long brCardId,
            BrCardPaymentType paymentType,
            BrCardPaymentStatus status
    );

    BrCardPayment findByBrCard(BrCard card);

    boolean existsByBrCardIdAndAmountAndStatus(
            Long brCardId,
            BigDecimal amount,
            BrCardPaymentStatus status
    );
}
