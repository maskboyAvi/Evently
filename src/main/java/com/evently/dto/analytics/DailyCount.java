package com.evently.dto.analytics;

import java.time.LocalDate;

public record DailyCount(LocalDate date, long count) {
}
