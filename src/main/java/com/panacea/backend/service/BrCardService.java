package com.panacea.backend.service;

import com.panacea.backend.Entity.BrCard;
import com.panacea.backend.Entity.BrCardPayment;
import com.panacea.backend.Entity.UserEntity;
import com.panacea.backend.Enum.BrCardPassType;
import com.panacea.backend.Enum.BrCardPaymentStatus;
import com.panacea.backend.Enum.BrCardPaymentType;
import com.panacea.backend.Enum.BrCardStatus;
import com.panacea.backend.dto.brcard.AdminBrCardResponse;
import com.panacea.backend.dto.brcard.BrCardPaymentResponse;
import com.panacea.backend.dto.brcard.BrCardResponse;
import com.panacea.backend.repository.BrCardPaymentRepository;
import com.panacea.backend.repository.BrCardRepository;
import com.panacea.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.panacea.backend.utils.AppUtils.giveMeCurrentUser;

@Service
@RequiredArgsConstructor
public class BrCardService {

    private static final String CARD_PREFIX = "PN26-";

    private static final String TOKEN_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final BigDecimal ACCOMMODATION_UPGRADE_AMOUNT =
            new BigDecimal("500");

    private final SecureRandom secureRandom = new SecureRandom();

    private final BrCardRepository brCardRepository;

    private final BrCardPaymentRepository paymentRepository;

    private final GoogleDriveService googleDriveService;

    private final UserRepository userRepository;


    // =========================================================
    // NEW BR CARD PAYMENT
    // =========================================================

    @Transactional
    public BrCardPaymentResponse submitPayment(
            Long userId,
            BrCardPassType passType,
            String transactionId,
            MultipartFile screenshot
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "Please Relogin"
            );
        }

        if (passType == null) {
            throw new IllegalArgumentException(
                    "Pass type is required"
            );
        }

        String normalizedTransactionId =
                normalizeTransactionId(transactionId);

        validateTransactionId(normalizedTransactionId);


        /*
         * Check whether this user already has a BR Card.
         */
        BrCard brCard =
                brCardRepository
                        .findByUserId(userId)
                        .orElse(null);


        /*
         * Existing active card cannot submit another
         * normal BR Card payment.
         */
        if (brCard != null &&
                brCard.getStatus() == BrCardStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Your BR Card is already active. Use the accommodation upgrade option if required."
            );
        }


        /*
         * If no card exists, create a new card.
         */
        if (brCard == null) {

            brCard = createBrCard(
                    userId,
                    passType
            );

        } else {

            /*
             * Do not allow changing pass type while a
             * previous payment is pending.
             */
            if (brCard.getStatus() ==
                    BrCardStatus.PAYMENT_PENDING) {

                throw new IllegalStateException(
                        "Your payment is already under verification."
                );
            }


            /*
             * If previous payment was rejected,
             * allow resubmission with a fresh pass type.
             */
            if (brCard.getStatus() ==
                    BrCardStatus.REJECTED) {

                brCard.setPassType(passType);

                brCard.setAmount(
                        passType.getAmount()
                );
            }
        }


        /*
         * Transaction ID must be unique.
         */
        if (paymentRepository.existsByTransactionId(
                normalizedTransactionId
        )) {

            throw new IllegalArgumentException(
                    "This transaction ID has already been submitted."
            );
        }


        /*
         * Prevent another pending normal payment.
         */
        Optional<BrCardPayment> existingPending =
                paymentRepository
                        .findFirstByBrCardIdOrderBySubmittedAtDesc(
                                brCard.getId()
                        );

        if (existingPending.isPresent() &&
                existingPending.get().getStatus() ==
                        BrCardPaymentStatus.PENDING) {

            throw new IllegalStateException(
                    "Your payment is already under verification."
            );
        }


        /*
         * Payment screenshot is OPTIONAL.
         *
         * If provided, upload directly to Google Drive.
         *
         * IMPORTANT:
         * We are NOT decoding/compressing the image here.
         * The file is streamed directly to Google Drive,
         * which avoids the ImageIO heap/OOM issue on Render.
         */
        String screenshotUrl = null;

        if (screenshot != null &&
                !screenshot.isEmpty()) {

            screenshotUrl =
                    googleDriveService.uploadFile(
                            screenshot
                    );
        }


        /*
         * Create payment.
         *
         * paymentScreenshotUrl is intentionally kept as
         * the existing field name so the frontend does not
         * need any changes.
         *
         * Its value is now the Google Drive FILE ID.
         */
        BrCardPayment payment =
                BrCardPayment.builder()
                        .brCard(brCard)
                        .transactionId(
                                normalizedTransactionId
                        )
                        .amount(
                                brCard.getAmount()
                        )
                        .paymentScreenshotUrl(
                                screenshotUrl
                        )
                        .status(
                                BrCardPaymentStatus.PENDING
                        )
                        .build();


        /*
         * Card is waiting for admin verification.
         */
        brCard.setStatus(
                BrCardStatus.PAYMENT_PENDING
        );

        brCardRepository.save(brCard);

        BrCardPayment saved =
                paymentRepository.save(payment);

        return toPaymentResponse(saved);
    }


    // =========================================================
    // ACCOMMODATION UPGRADE
    // =========================================================

    @Transactional
    public BrCardPaymentResponse submitAccommodationUpgrade(
            String transactionId,
            MultipartFile paymentScreenshot
    ) {

        UserEntity user =
                giveMeCurrentUser();

        if (user == null) {
            throw new IllegalStateException(
                    "Please Relogin"
            );
        }

        String normalizedTransactionId =
                normalizeTransactionId(transactionId);

        validateTransactionId(
                normalizedTransactionId
        );


        BrCard brCard =
                brCardRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "BR Card not found."
                                )
                        );


        /*
         * User must already have an active card.
         */
        if (brCard.getStatus() !=
                BrCardStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only an active BR Card can be upgraded."
            );
        }


        /*
         * Already upgraded.
         */
        if (brCard.getPassType() ==
                BrCardPassType.WITH_ACCOMMODATION) {

            throw new IllegalStateException(
                    "Accommodation is already included in your BR Card."
            );
        }


        /*
         * Prevent duplicate pending upgrade.
         */
        boolean alreadyPending =
                paymentRepository
                        .existsByBrCardIdAndPaymentTypeAndStatus(
                                brCard.getId(),
                                BrCardPaymentType.ACCOMMODATION_UPGRADE,
                                BrCardPaymentStatus.PENDING
                        );

        if (alreadyPending) {

            throw new IllegalStateException(
                    "Accommodation upgrade payment is already pending verification."
            );
        }


        /*
         * Transaction ID must be unique.
         */
        if (paymentRepository.existsByTransactionId(
                normalizedTransactionId
        )) {

            throw new IllegalArgumentException(
                    "This transaction ID has already been submitted."
            );
        }


        /*
         * Upgrade screenshot.
         *
         * Keep the existing field name and store the
         * Google Drive file ID in it.
         */
        String screenshotUrl = null;

        if (paymentScreenshot != null &&
                !paymentScreenshot.isEmpty()) {

            screenshotUrl =
                    googleDriveService.uploadFile(
                            paymentScreenshot
                    );
        }


        /*
         * Create ONLY a ₹500 upgrade payment.
         *
         * Existing card remains:
         *
         * status = ACTIVE
         * passType = WITHOUT_ACCOMMODATION
         *
         * until admin approves this payment.
         */
        BrCardPayment payment =
                BrCardPayment.builder()
                        .brCard(brCard)
                        .paymentType(
                                BrCardPaymentType
                                        .ACCOMMODATION_UPGRADE
                        )
                        .amount(
                                ACCOMMODATION_UPGRADE_AMOUNT
                        )
                        .transactionId(
                                normalizedTransactionId
                        )
                        .paymentScreenshotUrl(
                                screenshotUrl
                        )
                        .status(
                                BrCardPaymentStatus.PENDING
                        )
                        .build();

        BrCardPayment saved =
                paymentRepository.save(payment);

        return toPaymentResponse(saved);
    }


    // =========================================================
    // GET MY BR CARD
    // =========================================================

    public BrCardResponse getMyBrCard() {

        UserEntity user =
                giveMeCurrentUser();

        if (user == null) {
            throw new IllegalStateException(
                    "Please Relogin"
            );
        }

        return getMyBrCard(user.getId());
    }


    public BrCardResponse getMyBrCard(
            Long userId
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }

        BrCard card =
                brCardRepository
                        .findByUserId(userId)
                        .orElse(null);

        if (card == null) {
            return null;
        }


        /*
         * Get latest payment.
         *
         * IMPORTANT:
         * Do not assume payment exists.
         */
        BrCardPayment latestPayment =
                paymentRepository
                        .findFirstByBrCardIdOrderBySubmittedAtDesc(
                                card.getId()
                        )
                        .orElse(null);


        /*
         * Check whether an accommodation upgrade
         * is currently pending.
         */
        boolean upgradePending =
                paymentRepository
                        .existsByBrCardIdAndPaymentTypeAndStatus(
                                card.getId(),
                                BrCardPaymentType.ACCOMMODATION_UPGRADE,
                                BrCardPaymentStatus.PENDING
                        );


        /*
         * Build response.
         */
        return BrCardResponse.builder()
                .id(card.getId())
                .cardId(card.getCardId())
                .passType(card.getPassType())
                .amount(card.getAmount())
                .status(card.getStatus())
                .qrToken(card.getQrToken())
                .transactionId(
                        latestPayment != null
                                ? latestPayment.getTransactionId()
                                : null
                )
                .rejectionReason(
                        latestPayment != null
                                ? latestPayment.getRejectionReason()
                                : null
                )
                .upgradePending(upgradePending)
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }


    // =========================================================
    // ADMIN - GET PENDING PAYMENTS
    // =========================================================

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


    // =========================================================
    // ADMIN - APPROVE PAYMENT
    // =========================================================

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
                                        "Payment not found."
                                )
                        );


        /*
         * Only pending payments can be approved.
         */
        if (payment.getStatus() !=
                BrCardPaymentStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending payments can be approved."
            );
        }

        BrCard card =
                payment.getBrCard();


        /*
         * =====================================================
         * ACCOMMODATION UPGRADE
         * =====================================================
         *
         * Detect using paymentType.
         *
         * DO NOT detect using:
         *
         * amount == 500
         *
         * because amount alone is not sufficient business logic.
         */
        boolean isUpgrade =
                payment.getPaymentType() ==
                        BrCardPaymentType.ACCOMMODATION_UPGRADE;


        if (isUpgrade) {

            /*
             * Safety check.
             */
            if (card.getPassType() ==
                    BrCardPassType.WITH_ACCOMMODATION) {

                throw new IllegalStateException(
                        "Accommodation is already enabled for this BR Card."
                );
            }


            /*
             * Safety check amount.
             */
            if (payment.getAmount().compareTo(
                    ACCOMMODATION_UPGRADE_AMOUNT
            ) != 0) {

                throw new IllegalStateException(
                        "Invalid accommodation upgrade amount."
                );
            }


            /*
             * Upgrade existing card.
             *
             * DO NOT create a new card ID.
             * DO NOT create a new QR token.
             */
            card.setPassType(
                    BrCardPassType.WITH_ACCOMMODATION
            );

            card.setAmount(
                    BrCardPassType.WITH_ACCOMMODATION
                            .getAmount()
            );

            card.setStatus(
                    BrCardStatus.ACTIVE
            );

        } else {

            /*
             * =================================================
             * NORMAL BR CARD PAYMENT
             * =================================================
             */

            if (payment.getAmount().compareTo(
                    card.getAmount()
            ) != 0) {

                throw new IllegalStateException(
                        "Payment amount does not match BR Card amount."
                );
            }


            /*
             * Generate card ID ONLY for the first
             * successful BR Card payment.
             */
            if (card.getCardId() == null ||
                    card.getCardId().isBlank()) {

                card.setCardId(
                        generateUniqueCardId()
                );
            }


            /*
             * Generate QR token ONLY for the first
             * successful BR Card payment.
             */
            if (card.getQrToken() == null ||
                    card.getQrToken().isBlank()) {

                card.setQrToken(
                        generateQrToken()
                );
            }

            card.setStatus(
                    BrCardStatus.ACTIVE
            );
        }


        /*
         * Mark payment approved.
         */
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

        BrCardPayment savedPayment =
                paymentRepository.save(payment);

        return toBrCardResponse(
                card,
                savedPayment
        );
    }


    // =========================================================
    // ADMIN - REJECT PAYMENT
    // =========================================================

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
                                        "Payment not found."
                                )
                        );


        if (payment.getStatus() !=
                BrCardPaymentStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending payments can be rejected."
            );
        }


        if (rejectionReason == null ||
                rejectionReason.isBlank()) {

            throw new IllegalArgumentException(
                    "Rejection reason is required."
            );
        }


        if (rejectionReason.length() > 500) {

            throw new IllegalArgumentException(
                    "Rejection reason cannot exceed 500 characters."
            );
        }


        BrCard card =
                payment.getBrCard();

        boolean isUpgrade =
                payment.getPaymentType() ==
                        BrCardPaymentType.ACCOMMODATION_UPGRADE;


        /*
         * Reject payment.
         */
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


        /*
         * IMPORTANT:
         *
         * If upgrade payment is rejected,
         * existing BR Card must remain ACTIVE.
         */
        if (isUpgrade) {

            card.setStatus(
                    BrCardStatus.ACTIVE
            );

        } else {

            /*
             * Normal initial payment rejected.
             */
            card.setStatus(
                    BrCardStatus.REJECTED
            );
        }


        brCardRepository.save(card);

        paymentRepository.save(payment);

        return toBrCardResponse(
                card,
                payment
        );
    }


    // =========================================================
    // ADMIN - ACTIVE CARDS
    // =========================================================

    public List<AdminBrCardResponse>
    getActiveBrCards() {

        return paymentRepository
                .findActiveBrCardPayments()
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }


    // =========================================================
    // QR VERIFICATION
    // =========================================================

    public BrCardResponse verifyQrToken(
            String qrToken
    ) {

        if (qrToken == null ||
                qrToken.isBlank()) {

            throw new IllegalArgumentException(
                    "QR token is required."
            );
        }


        BrCard card =
                brCardRepository
                        .findByQrToken(qrToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid BR Card QR."
                                )
                        );


        if (card.getStatus() !=
                BrCardStatus.ACTIVE) {

            throw new IllegalStateException(
                    "BR Card is not active."
            );
        }


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


    // =========================================================
    // CREATE BR CARD
    // =========================================================

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


    // =========================================================
    // NORMALIZE TRANSACTION ID
    // =========================================================

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


    // =========================================================
    // VALIDATE TRANSACTION ID
    // =========================================================

    private void validateTransactionId(
            String transactionId
    ) {

        if (transactionId.isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction ID is required."
            );
        }


        /*
         * Generic UTR / transaction validation.
         *
         * We intentionally don't force a specific
         * bank's format.
         */
        if (!transactionId.matches(
                "^[A-Z0-9]{8,40}$"
        )) {

            throw new IllegalArgumentException(
                    "Invalid transaction ID format."
            );
        }
    }


    // =========================================================
    // GENERATE CARD ID
    // =========================================================

    private String generateUniqueCardId() {

        for (int i = 0; i < 20; i++) {

            int number =
                    secureRandom.nextInt(900000)
                            + 100000;

            String cardId =
                    CARD_PREFIX + number;

            if (brCardRepository
                    .findByCardId(cardId)
                    .isEmpty()) {

                return cardId;
            }
        }

        throw new IllegalStateException(
                "Unable to generate unique BR Card ID."
        );
    }


    // =========================================================
    // GENERATE QR TOKEN
    // =========================================================

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


    // =========================================================
    // PAYMENT RESPONSE
    // =========================================================

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


    // =========================================================
    // BR CARD RESPONSE
    // =========================================================

    private BrCardResponse
    toBrCardResponse(
            BrCard card,
            BrCardPayment payment
    ) {

        boolean upgradePending =
                paymentRepository
                        .existsByBrCardIdAndPaymentTypeAndStatus(
                                card.getId(),
                                BrCardPaymentType.ACCOMMODATION_UPGRADE,
                                BrCardPaymentStatus.PENDING
                        );


        return BrCardResponse.builder()
                .id(card.getId())
                .cardId(card.getCardId())
                .passType(card.getPassType())
                .amount(card.getAmount())
                .status(card.getStatus())
                .qrToken(card.getQrToken())
                .transactionId(
                        payment != null
                                ? payment.getTransactionId()
                                : null
                )
                .rejectionReason(
                        payment != null
                                ? payment.getRejectionReason()
                                : null
                )
                .upgradePending(upgradePending)
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }


    // =========================================================
    // ADMIN RESPONSE
    // =========================================================

    private AdminBrCardResponse
    toAdminResponse(
            BrCardPayment payment
    ) {

        BrCard card =
                payment.getBrCard();

        /*
         * FIX:
         *
         * card.getId() = BR Card ID
         * card.getUserId() = User ID
         *
         * We need the latter here.
         */
        UserEntity user =
                userRepository
                        .findById(card.getUserId())
                        .orElse(null);


        /*
         * The payment type is the source of truth.
         */
        boolean isUpgrade =
                payment.getPaymentType() ==
                        BrCardPaymentType.ACCOMMODATION_UPGRADE;


        return new AdminBrCardResponse(
                payment.getId(),
                card.getId(),
                card.getUserId(),

                user != null
                        ? user.getFullName()
                        : "",

                user != null
                        ? user.getEmail()
                        : "",

                user != null
                        ? user.getPhoneNumber()
                        : "",

                user != null
                        ? user.getCollegeName()
                        : "",

                user != null
                        ? user.getCity()
                        : "",

                user != null
                        ? user.getState()
                        : "",

                card.getCardId(),
                card.getPassType(),

                card.getAmount(),
                payment.getAmount(),

                isUpgrade,

                payment.getTransactionId(),

                /*
                 * Keep the existing API field name.
                 *
                 * This now contains the Google Drive FILE ID,
                 * not a Cloudinary URL.
                 */
                payment.getPaymentScreenshotUrl(),

                payment.getStatus(),
                payment.getRejectionReason(),
                payment.getSubmittedAt(),
                payment.getVerifiedAt()
        );
    }
}