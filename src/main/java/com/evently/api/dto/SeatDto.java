package com.evently.api.dto;

import com.evently.domain.Seat;
import com.evently.domain.SeatStatus;

public record SeatDto(Long id, Long eventId, String label, SeatStatus status) {
    public static SeatDto from(Seat s) {
        return new SeatDto(s.getId(), s.getEvent().getId(), s.getLabel(), s.getStatus());
    }
}
