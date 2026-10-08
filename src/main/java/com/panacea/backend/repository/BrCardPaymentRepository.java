package com.panacea.backend.repository;



import com.panacea.backend.Entity.BrCardPayment;
import com.panacea.backend.Enum.BrCardPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
