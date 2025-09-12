package com.evently.api.dto;

import jakarta.validation.constraints.NotNull;

// User is now derived from the authenticated principal; userId removed.
public record BookingRequest(
                @NotNull Long eventId,
                Long seatId) {
}
