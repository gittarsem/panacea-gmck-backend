package com.panacea.backend.dto.admin;

public class AdminDashboardResponse {

    private long totalBrCards;
    private long pendingPayments;
    private long activeBrCards;
    private long rejectedPayments;
    private long totalEvents;
    private long activeEvents;
    private long totalScheduleItems;
    private long activeScheduleItems;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(
            long totalBrCards,
            long pendingPayments,
            long activeBrCards,
            long rejectedPayments,
            long totalEvents,
            long activeEvents,
            long totalScheduleItems,
            long activeScheduleItems
    ) {
        this.totalBrCards = totalBrCards;
        this.pendingPayments = pendingPayments;
        this.activeBrCards = activeBrCards;
        this.rejectedPayments = rejectedPayments;
        this.totalEvents = totalEvents;
        this.activeEvents = activeEvents;
        this.totalScheduleItems = totalScheduleItems;
        this.activeScheduleItems = activeScheduleItems;
    }

    public long getTotalBrCards() {
        return totalBrCards;
    }

    public void setTotalBrCards(long totalBrCards) {
        this.totalBrCards = totalBrCards;
    }

    public long getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(long pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public long getActiveBrCards() {
        return activeBrCards;
    }

    public void setActiveBrCards(long activeBrCards) {
        this.activeBrCards = activeBrCards;
    }

    public long getRejectedPayments() {
        return rejectedPayments;
    }

    public void setRejectedPayments(long rejectedPayments) {
        this.rejectedPayments = rejectedPayments;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getActiveEvents() {
        return activeEvents;
    }

    public void setActiveEvents(long activeEvents) {
        this.activeEvents = activeEvents;
    }

    public long getTotalScheduleItems() {
        return totalScheduleItems;
    }

    public void setTotalScheduleItems(long totalScheduleItems) {
        this.totalScheduleItems = totalScheduleItems;
    }

    public long getActiveScheduleItems() {
        return activeScheduleItems;
    }

    public void setActiveScheduleItems(long activeScheduleItems) {
        this.activeScheduleItems = activeScheduleItems;
    }
}
