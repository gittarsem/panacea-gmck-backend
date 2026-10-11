package com.panacea.backend.service;

import com.panacea.backend.Enum.BrCardPaymentStatus;
import com.panacea.backend.Enum.BrCardStatus;
import com.panacea.backend.dto.admin.AdminDashboardResponse;
import com.panacea.backend.repository.BrCardPaymentRepository;
import com.panacea.backend.repository.BrCardRepository;
import com.panacea.backend.repository.EventRepository;
import com.panacea.backend.repository.ScheduleItemRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final BrCardRepository brCardRepository;
    private final BrCardPaymentRepository brCardPaymentRepository;
    private final EventRepository eventRepository;
    private final ScheduleItemRepository scheduleItemRepository;

    public AdminDashboardService(
            BrCardRepository brCardRepository,
            BrCardPaymentRepository brCardPaymentRepository,
            EventRepository eventRepository,
            ScheduleItemRepository scheduleItemRepository
    ) {
        this.brCardRepository = brCardRepository;
        this.brCardPaymentRepository = brCardPaymentRepository;
        this.eventRepository = eventRepository;
        this.scheduleItemRepository = scheduleItemRepository;
    }

    public AdminDashboardResponse getDashboard() {

        long totalBrCards =
                brCardRepository.count();

        long pendingPayments =
                brCardPaymentRepository.countByStatus(
                        BrCardPaymentStatus.PENDING
                );

        long activeBrCards =
                brCardRepository.countByStatus(
                        BrCardStatus.ACTIVE
                );

        long rejectedPayments =
                brCardPaymentRepository.countByStatus(
                        BrCardPaymentStatus.REJECTED
                );

        long totalEvents =
                eventRepository.count();

        long activeEvents =
                eventRepository.countByActiveTrue();

        long totalScheduleItems =
                scheduleItemRepository.count();

        long activeScheduleItems =
                scheduleItemRepository.countByActiveTrue();

        return new AdminDashboardResponse(
                totalBrCards,
                pendingPayments,
                activeBrCards,
                rejectedPayments,
                totalEvents,
                activeEvents,
                totalScheduleItems,
                activeScheduleItems
        );
    }
}