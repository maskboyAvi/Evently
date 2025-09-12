package com.evently.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SeatGenerationRequest(
        @NotNull Long eventId,
        @Min(1) int rows,
        @Min(1) int seatsPerRow,
        String rowPrefix // optional, default A,B,C...
) {
}
