package com.evently.service;

import com.evently.api.dto.BookingRequest;
import com.evently.domain.*;
import com.evently.repo.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookings;
    private final EventRepository events;
    private final UserRepository users;
    private final SeatRepository seats;
    private final WaitlistRepository waitlists;

    @Transactional
    public Booking book(Long userId, BookingRequest req) {
        User user = users.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Event event = events.findById(req.eventId()).orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Try a few times in case of concurrent updates
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                int remaining = event.getCapacity() - event.getBookedCount();
                if (remaining <= 0) {
                    // add to waitlist
                    long pos = waitlists.countByEventId(event.getId()) + 1;
                    waitlists.findByUserIdAndEventId(user.getId(), event.getId()).orElseGet(
                            () -> waitlists
                                    .save(Waitlist.builder().user(user).event(event).position((int) pos).build()));
                    throw new IllegalArgumentException("Event full. Added to waitlist.");
                }

                Seat seat = null;
                if (req.seatId() != null) {
                    seat = seats.findById(req.seatId())
                            .orElseThrow(() -> new EntityNotFoundException("Seat not found"));
                    if (!seat.getEvent().getId().equals(event.getId())) {
                        throw new IllegalArgumentException("Seat does not belong to event");
                    }
                    if (seat.getStatus() != SeatStatus.AVAILABLE) {
                        throw new IllegalArgumentException("Seat not available");
                    }
                    seat.setStatus(SeatStatus.BOOKED);
                    seats.save(seat);
                }

                event.setBookedCount(event.getBookedCount() + 1);
                events.save(event); // Optimistic lock via @Version

                Booking booking = Booking.builder()
                        .user(user)
                        .event(event)
                        .seat(seat)
                        .status(BookingStatus.CONFIRMED)
                        .build();
                return bookings.save(booking);
            } catch (jakarta.persistence.OptimisticLockException ole) {
                if (attempt == 3)
                    throw ole;
                // Small jitter before retry
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(5, 25));
                } catch (InterruptedException ignored) {
                }
                // Reload a fresh copy for next attempt
                Event fresh = events.findById(req.eventId())
                        .orElseThrow(() -> new EntityNotFoundException("Event not found"));
                event.setBookedCount(fresh.getBookedCount());
                event.setVersion(fresh.getVersion());
                event.setCapacity(fresh.getCapacity());
            }
        }
        throw new IllegalStateException("Unable to process booking after retries");
    }

    @Transactional
    public Booking cancel(Long bookingId) {
        Booking b = bookings.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        if (b.getStatus() == BookingStatus.CANCELED) {
            return b;
        }
        b.setStatus(BookingStatus.CANCELED);
        bookings.save(b);

        Event event = b.getEvent();
        event.setBookedCount(Math.max(0, event.getBookedCount() - 1));
        events.save(event);

        if (b.getSeat() != null) {
            Seat seat = b.getSeat();
            seat.setStatus(SeatStatus.AVAILABLE);
            seats.save(seat);
        }

        // Promote waitlist: if capacity available, auto-book first in line
        List<Waitlist> wl = waitlists.findByEventIdOrderByPositionAsc(event.getId());
        if (!wl.isEmpty()) {
            Waitlist first = wl.get(0);
            int remaining = event.getCapacity() - event.getBookedCount();
            if (remaining > 0 && !bookings.existsByUserIdAndEventIdAndStatus(first.getUser().getId(), event.getId(),
                    BookingStatus.CONFIRMED)) {
                Booking promoted = Booking.builder()
                        .user(first.getUser())
                        .event(event)
                        .status(BookingStatus.CONFIRMED)
                        .build();
                bookings.save(promoted);
                event.setBookedCount(event.getBookedCount() + 1);
                events.save(event);
                waitlists.delete(first);
            }
        }
        return b;
    }
}
