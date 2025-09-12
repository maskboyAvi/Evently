package com.evently.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyticsOverview {
    private long totalUsers;
    private long totalEvents;
    private long totalBookingsConfirmed;
    private double avgUtilization; // 0..1 across events with capacity > 0
}
