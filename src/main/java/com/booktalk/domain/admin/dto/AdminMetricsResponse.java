package com.booktalk.domain.admin.dto;

/**
 * 어드민 대시보드 핵심 지표.
 */
public record AdminMetricsResponse(
        long totalUsers,
        long newUsersToday,
        long newUsers7d,
        long totalBooks,
        long readingRecords,
        long completedRecords,
        long wishlistRecords,
        long totalMeetings,
        long recruitingMeetings,
        long ongoingMeetings,
        long closedMeetings,
        long totalBookNotes,
        long totalChatMessages
) {}
