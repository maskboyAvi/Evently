package com.evently.api.dto;

import com.evently.domain.Booking;
import com.evently.domain.BookingStatus;

import java.time.Instant;

public record BookingDto(
        Long id,
        Long userId,
        Long eventId,
        Long seatId,
        BookingStatus status,
        Instant createdAt,
        Instant updatedAt) {
    public static BookingDto from(Booking b) {
        Long seatId = b.getSeat() != null ? b.getSeat().getId() : null;
        return new BookingDto(
                b.getId(),
                b.getUser() != null ? b.getUser().getId() : null,
                b.getEvent() != null ? b.getEvent().getId() : null,
                seatId,
                b.getStatus(),
                b.getCreatedAt(),
                b.getUpdatedAt());
    }
}
