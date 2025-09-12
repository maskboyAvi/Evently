package com.evently.api;

import com.evently.dto.analytics.AnalyticsOverview;
import com.evently.dto.analytics.DailyCount;
import com.evently.dto.analytics.EventStat;
import com.evently.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public AnalyticsOverview overview() {
        return analyticsService.overview();
    }

    @GetMapping("/top-events")
    public List<EventStat> topEvents(@RequestParam(defaultValue = "5") int limit) {
        return analyticsService.topEvents(limit);
    }

    @GetMapping("/daily-confirmed")
    public List<DailyCount> dailyConfirmed(@RequestParam(defaultValue = "30") int days) {
        return analyticsService.confirmedDaily(days);
    }

    @GetMapping("/cancellation-rate")
    public double cancellationRate(@RequestParam(defaultValue = "30") int days) {
        return analyticsService.cancellationRate(days);
    }
}
