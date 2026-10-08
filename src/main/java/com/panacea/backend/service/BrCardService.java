package com.panacea.backend.service;

import com.panacea.backend.Entity.BrCard;
import com.panacea.backend.Entity.BrCardPayment;
import com.panacea.backend.Enum.BrCardPassType;
import com.panacea.backend.Enum.BrCardPaymentStatus;
import com.panacea.backend.Enum.BrCardStatus;
import com.panacea.backend.dto.brcard.AdminBrCardResponse;
import com.panacea.backend.dto.brcard.BrCardPaymentResponse;
import com.panacea.backend.dto.brcard.BrCardResponse;
import com.panacea.backend.repository.BrCardPaymentRepository;
import com.panacea.backend.repository.BrCardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BrCardService {

    private static final String CARD_PREFIX =
            "PN26-";

    private static final String TOKEN_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom secureRandom =
            new SecureRandom();

    private final BrCardRepository brCardRepository;

    private final BrCardPaymentRepository paymentRepository;

    private final PaymentScreenshotService
            screenshotService;

    public BrCardService(
            BrCardRepository brCardRepository,
            BrCardPaymentRepository paymentRepository,
            PaymentScreenshotService screenshotService
    ) {
        this.brCardRepository =
                brCardRepository;

        this.paymentRepository =
                paymentRepository;

        this.screenshotService =
                screenshotService;
    }

    @Transactional
    public BrCardPaymentResponse submitPayment(
            Long userId,
            BrCardPassType passType,
            String transactionId,
            MultipartFile screenshot
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "Authenticated user is required"
            );
        }

        if (passType == null) {
            throw new IllegalArgumentException(
                    "Pass type is required"
            );
        }

        String normalizedTransactionId =
                normalizeTransactionId(
                        transactionId
                );

        validateTransactionId(
                normalizedTransactionId
        );

        /*
         * A user can have only one active BR Card
         * registration.
         */
        BrCard brCard =
                brCardRepository
                        .findByUserId(userId)
                        .orElseGet(() ->
                                createBrCard(
                                        userId,
                                        passType
                                )
                        );

        /*
         * If the existing card is ACTIVE,
         * do not allow another payment.
         */
        if (brCard.getStatus() ==
                BrCardStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Your BR Card is already active"
            );
        }

        /*
         * Backend controls the amount.
         * Never trust an amount coming from frontend.
         */
        if (brCard.getPassType() !=
                passType) {

            /*
             * If an earlier payment was rejected,
             * allow the user to choose a new pass type.
             */
            if (brCard.getStatus() ==
                    BrCardStatus.REJECTED) {

                brCard.setPassType(passType);
                brCard.setAmount(
                        passType.getAmount()
                );

            } else {
                throw new IllegalStateException(
                        "Pass type cannot be changed while payment is pending"
                );
            }
        }

        /*
         * Prevent the same UTR from being reused.
         */
        if (paymentRepository
                .existsByTransactionId(
                        normalizedTransactionId
                )) {

            throw new IllegalArgumentException(
                    "This transaction ID has already been submitted"
            );
        }

        /*
         * If there is already a pending payment,
         * don't create another one.
         */
        BrCardPayment existingPending =
                paymentRepository
                        .findFirstByBrCardIdOrderBySubmittedAtDesc(
                                brCard.getId()
                        )
                        .orElse(null);

        if (existingPending != null &&
                existingPending.getStatus() ==
                        BrCardPaymentStatus.PENDING) {

            throw new IllegalStateException(
                    "Your payment is already under verification"
            );
        }

        String screenshotUrl =
                screenshotService.upload(
                        screenshot
                );

        BrCardPayment payment =
                new BrCardPayment();

        payment.setBrCard(brCard);

        payment.setTransactionId(
                normalizedTransactionId
        );

        /*
         * IMPORTANT:
         * Amount comes from the server.
         */
        payment.setAmount(
                brCard.getAmount()
        );

        payment.setPaymentScreenshotUrl(
                screenshotUrl
        );

        payment.setStatus(
                BrCardPaymentStatus.PENDING
        );

        brCard.setStatus(
                BrCardStatus.PAYMENT_PENDING
        );

        brCardRepository.save(brCard);

        BrCardPayment saved =
                paymentRepository.save(
                        payment
                );

        return toPaymentResponse(saved);
    }

    @Transactional
    public BrCardResponse getMyBrCard(
            Long userId
    ) {

        BrCard card =
                brCardRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "BR Card registration not found"
                                )
                        );

        BrCardPayment latestPayment =
                paymentRepository
                        .findFirstByBrCardIdOrderBySubmittedAtDesc(
                                card.getId()
                        )
                        .orElse(null);

        return toBrCardResponse(
                card,
                latestPayment
        );
    }

    public List<AdminBrCardResponse>
    getPendingPayments() {

        return paymentRepository
                .findAllByStatusOrderBySubmittedAtAsc(
                        BrCardPaymentStatus.PENDING
                )
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public BrCardResponse approvePayment(
            Long paymentId,
            Long adminUserId
    ) {

        BrCardPayment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment not found"
                                )
                        );

        if (payment.getStatus() !=
                BrCardPaymentStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending payments can be approved"
            );
        }

        BrCard card =
                payment.getBrCard();

        /*
         * Double protection:
         * payment amount must match card amount.
         */
        if (payment.getAmount()
                .compareTo(
                        card.getAmount()
                ) != 0) {

            throw new IllegalStateException(
                    "Payment amount does not match BR Card amount"
            );
        }

        /*
         * Generate card ID only after approval.
         */
        String cardId =
                generateUniqueCardId();

        /*
         * Generate secure random token.
         */
        String qrToken =
                generateQrToken();

        card.setCardId(cardId);
        card.setQrToken(qrToken);
        card.setStatus(
                BrCardStatus.ACTIVE
        );

        payment.setStatus(
                BrCardPaymentStatus.APPROVED
        );

        payment.setVerifiedBy(
                adminUserId
        );

        payment.setVerifiedAt(
                LocalDateTime.now()
        );

        brCardRepository.save(card);
        paymentRepository.save(payment);

        return toBrCardResponse(
                card,
                payment
        );
    }

    @Transactional
    public BrCardResponse rejectPayment(
            Long paymentId,
            Long adminUserId,
            String rejectionReason
    ) {

        BrCardPayment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment not found"
                                )
                        );

        if (payment.getStatus() !=
                BrCardPaymentStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending payments can be rejected"
            );
        }

        if (rejectionReason == null ||
                rejectionReason.isBlank()) {

            throw new IllegalArgumentException(
                    "Rejection reason is required"
            );
        }

        if (rejectionReason.length() >
                500) {

            throw new IllegalArgumentException(
                    "Rejection reason cannot exceed 500 characters"
            );
        }

        payment.setStatus(
                BrCardPaymentStatus.REJECTED
        );

        payment.setRejectionReason(
                rejectionReason.trim()
        );

        payment.setVerifiedBy(
                adminUserId
        );

        payment.setVerifiedAt(
                LocalDateTime.now()
        );

        BrCard card =
                payment.getBrCard();

        card.setStatus(
                BrCardStatus.REJECTED
        );

        brCardRepository.save(card);
        paymentRepository.save(payment);

        return toBrCardResponse(
                card,
                payment
        );
    }

    public BrCardResponse verifyQrToken(
            String qrToken
    ) {

        if (qrToken == null ||
                qrToken.isBlank()) {

            throw new IllegalArgumentException(
                    "QR token is required"
            );
        }

        BrCard card =
                brCardRepository
                        .findByQrToken(qrToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid BR Card QR"
                                )
                        );

        if (card.getStatus() !=
                BrCardStatus.ACTIVE) {

            throw new IllegalStateException(
                    "BR Card is not active"
            );
        }

        return toBrCardResponse(
                card,
                paymentRepository
                        .findFirstByBrCardIdOrderBySubmittedAtDesc(
                                card.getId()
                        )
                        .orElse(null)
        );
    }

    private BrCard createBrCard(
            Long userId,
            BrCardPassType passType
    ) {

        BrCard card =
                new BrCard();

        card.setUserId(userId);

        card.setPassType(passType);

        card.setAmount(
                passType.getAmount()
        );

        card.setStatus(
                BrCardStatus.PAYMENT_PENDING
        );

        return brCardRepository.save(card);
    }

    private String normalizeTransactionId(
            String transactionId
    ) {

        if (transactionId == null) {
            return "";
        }

        return transactionId
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase();
    }

    private void validateTransactionId(
            String transactionId
    ) {

        if (transactionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction ID is required"
            );
        }

        /*
         * We intentionally don't enforce one bank-specific
         * UTR format.
         *
         * We only reject obviously invalid values.
         */
        if (!transactionId.matches(
                "^[A-Z0-9]{8,40}$"
        )) {

            throw new IllegalArgumentException(
                    "Invalid transaction ID format"
            );
        }
    }

    private String generateUniqueCardId() {

        for (int i = 0; i < 20; i++) {

            int number =
                    secureRandom.nextInt(
                            900000
                    ) + 100000;

            String cardId =
                    CARD_PREFIX + number;

            if (!brCardRepository
                    .findByCardId(cardId)
                    .isPresent()) {

                return cardId;
            }
        }

        throw new IllegalStateException(
                "Unable to generate unique BR Card ID"
        );
    }

    private String generateQrToken() {

        StringBuilder token =
                new StringBuilder(48);

        for (int i = 0; i < 48; i++) {

            token.append(
                    TOKEN_ALPHABET.charAt(
                            secureRandom.nextInt(
                                    TOKEN_ALPHABET.length()
                            )
                    )
            );
        }

        return token.toString();
    }

    private BrCardPaymentResponse
    toPaymentResponse(
            BrCardPayment payment
    ) {

        return new BrCardPaymentResponse(
                payment.getId(),
                payment.getBrCard().getId(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getRejectionReason(),
                payment.getSubmittedAt(),
                payment.getVerifiedAt()
        );
    }

    private BrCardResponse
    toBrCardResponse(
            BrCard card,
            BrCardPayment payment
    ) {

        return new BrCardResponse(
                card.getId(),
                card.getCardId(),
                card.getPassType(),
                card.getAmount(),
                card.getStatus(),
                card.getQrToken(),
                payment != null
                        ? payment.getTransactionId()
                        : null,
                payment != null
                        ? payment.getRejectionReason()
                        : null,
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    private AdminBrCardResponse
    toAdminResponse(
            BrCardPayment payment
    ) {

        BrCard card =
                payment.getBrCard();

        return new AdminBrCardResponse(
                payment.getId(),
                card.getId(),
                card.getUserId(),
                card.getCardId(),
                card.getPassType(),
                card.getAmount(),
                payment.getTransactionId(),
                payment.getPaymentScreenshotUrl(),
                payment.getStatus(),
                payment.getRejectionReason(),
                payment.getSubmittedAt(),
                payment.getVerifiedAt()
        );
    }
}
