package com.panacea.backend.controller;


import com.panacea.backend.Entity.UserPrincipal;
import com.panacea.backend.Enum.BrCardPassType;
import com.panacea.backend.dto.brcard.AdminBrCardResponse;
import com.panacea.backend.dto.brcard.BrCardPaymentResponse;
import com.panacea.backend.dto.brcard.BrCardResponse;
import com.panacea.backend.service.BrCardService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/br-cards")
public class BrCardController {

    private final BrCardService brCardService;

    public BrCardController(BrCardService brCardService) {
        this.brCardService = brCardService;
    }

    // =========================================================
    // USER APIs
    // =========================================================

    /**
     * Submit BR Card payment proof.
     *
     * The user ID is taken from the authenticated JWT.
     * It is NEVER accepted from the frontend.
     */
    @PostMapping(
            value = "/payment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BrCardPaymentResponse> submitPayment(
            @RequestParam("passType")
            BrCardPassType passType,

            @RequestParam("transactionId")
            String transactionId,

            @RequestPart("paymentScreenshot")
            MultipartFile paymentScreenshot,

            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        BrCardPaymentResponse response =
                brCardService.submitPayment(
                        userId,
                        passType,
                        transactionId,
                        paymentScreenshot
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Get the currently authenticated user's BR Card.
     */
    @GetMapping("/me")
    public ResponseEntity<BrCardResponse> getMyBrCard(
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                brCardService.getMyBrCard(userId)
        );
    }

    // =========================================================
    // ADMIN APIs
    // =========================================================

    /**
     * Get all pending BR Card payments.
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminBrCardResponse>> getPendingPayments() {

        return ResponseEntity.ok(
                brCardService.getPendingPayments()
        );
    }

    /**
     * Approve a BR Card payment.
     */
    @PostMapping("/admin/payments/{paymentId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrCardResponse> approvePayment(
            @PathVariable Long paymentId,
            Authentication authentication
    ) {

        Long adminUserId = getUserId(authentication);

        return ResponseEntity.ok(
                brCardService.approvePayment(
                        paymentId,
                        adminUserId
                )
        );
    }

    /**
     * Reject a BR Card payment.
     */
    @PostMapping("/admin/payments/{paymentId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrCardResponse> rejectPayment(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {

        Long adminUserId = getUserId(authentication);

        String reason = body.get("reason");

        return ResponseEntity.ok(
                brCardService.rejectPayment(
                        paymentId,
                        adminUserId,
                        reason
                )
        );
    }

    /**
     * Verify a BR Card QR token at the gate.
     */
    @PostMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrCardResponse> verifyQr(
            @RequestBody Map<String, String> body
    ) {

        String qrToken = body.get("qrToken");

        return ResponseEntity.ok(
                brCardService.verifyQrToken(qrToken)
        );
    }

    @GetMapping("/admin/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminBrCardResponse>> getActiveBrCards() {

        return ResponseEntity.ok(
                brCardService.getActiveBrCards()
        );
    }

    // =========================================================
    // AUTHENTICATED USER RESOLUTION
    // =========================================================

    private Long getUserId(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal)) {
            throw new IllegalStateException(
                    "Invalid authenticated user principal"
            );
        }

        return userPrincipal
                .getUser()
                .getId();
    }

    @PostMapping(
            value = "/me/upgrade",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> submitAccommodationUpgrade(
            @RequestParam("transactionId")
            String transactionId,

            @RequestParam("paymentScreenshot")
            MultipartFile paymentScreenshot
    ) {

        brCardService.submitAccommodationUpgrade(
                transactionId,
                paymentScreenshot
        );

        return ResponseEntity.ok().build();
    }
}