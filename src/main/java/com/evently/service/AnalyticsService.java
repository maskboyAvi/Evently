package com.evently.service;

import com.evently.domain.Event;
import com.evently.dto.analytics.DailyCount;
import com.evently.repo.BookingRepository;
import com.evently.dto.analytics.AnalyticsOverview;
import com.evently.dto.analytics.EventStat;
import com.evently.repo.EventRepository;
import com.evently.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
        private final UserRepository users;
        private final EventRepository events;
        private final BookingRepository bookings;

        public AnalyticsOverview overview() {
                long totalUsers = users.count();
                long totalEvents = events.count();
                long totalConfirmed = events.findAll().stream().mapToLong(e -> e.getBookedCount()).sum();
                List<Event> withCapacity = events.findAll().stream()
                                .filter(e -> e.getCapacity() != null && e.getCapacity() > 0)
                                .toList();
                double avgUtil = withCapacity.isEmpty() ? 0
                                : withCapacity.stream().mapToDouble(e -> (double) e.getBookedCount() / e.getCapacity())
                                                .average()
                                                .orElse(0);
                return new AnalyticsOverview(totalUsers, totalEvents, totalConfirmed, avgUtil);
        }

        public List<EventStat> topEvents(int limit) {
                return events.findAll().stream()
                                .sorted(Comparator.comparing(Event::getBookedCount).reversed())
                                .limit(limit)
                                .map(e -> new EventStat(e.getId(), e.getName(), e.getCapacity(), e.getBookedCount(),
                                                e.getCapacity() == 0 ? 0.0
                                                                : (double) e.getBookedCount() / e.getCapacity()))
                                .toList();
        }

        public List<DailyCount> confirmedDaily(int days) {
                Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
                return bookings.countDailyConfirmedSince(since).stream()
                                .map(r -> new DailyCount(((java.sql.Date) r[0]).toLocalDate(), (Long) r[1]))
                                .toList();
        }

        public double cancellationRate(int days) {
                Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
                long canc = bookings.countCanceledSince(since);
                long conf = bookings.countConfirmedSince(since);
                long denom = canc + conf;
                return denom == 0 ? 0.0 : (double) canc / denom;
        }
}
