package com.panacea.backend.controller;

import com.panacea.backend.Entity.BrCard;
import com.panacea.backend.Entity.BrCardPayment;
import com.panacea.backend.Enum.BrCardPaymentStatus;
import com.panacea.backend.repository.BrCardPaymentRepository;
import com.panacea.backend.repository.BrCardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/br-cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBrCardController {

    private final BrCardRepository brCardRepository;
    private final BrCardPaymentRepository brCardPaymentRepository;

    public AdminBrCardController(
            BrCardRepository brCardRepository,
            BrCardPaymentRepository brCardPaymentRepository
    ) {
        this.brCardRepository = brCardRepository;
        this.brCardPaymentRepository = brCardPaymentRepository;
    }

    @GetMapping
    public ResponseEntity<List<BrCard>> getAllBrCards() {
        return ResponseEntity.ok(
                brCardRepository.findAll()
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<List<BrCardPayment>> getPendingPayments() {
        return ResponseEntity.ok(
                brCardPaymentRepository
                        .findAllByStatusOrderBySubmittedAtAsc(
                                BrCardPaymentStatus.PENDING
                        )
        );
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<BrCardPayment> getPayment(
            @PathVariable Long paymentId
    ) {
        return brCardPaymentRepository
                .findById(paymentId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/{brCardId}/payments")
    public ResponseEntity<List<BrCardPayment>> getCardPayments(
            @PathVariable Long brCardId
    ) {
        return ResponseEntity.ok(
                brCardPaymentRepository
                        .findAllByBrCardIdOrderBySubmittedAtDesc(
                                brCardId
                        )
        );
    }
}