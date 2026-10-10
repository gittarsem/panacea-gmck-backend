package com.panacea.backend.repository;




import com.panacea.backend.Entity.BrCard;
import com.panacea.backend.Enum.BrCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrCardRepository
        extends JpaRepository<BrCard, Long> {

    Optional<BrCard> findByUserId(Long userId);

    Optional<BrCard> findByCardId(String cardId);

    Optional<BrCard> findByQrToken(String qrToken);

    List<BrCard> findAllByStatusOrderByCreatedAtAsc(
            BrCardStatus status
    );

    boolean existsByUserId(Long userId);

    long countByStatus(BrCardStatus status);

    List<BrCard> findAllByStatusOrderByCreatedAtDesc(
            BrCardStatus status
    );


}
