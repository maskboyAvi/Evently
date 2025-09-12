package com.evently.api.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record CreateEventRequest(
        @NotBlank String name,
        @NotBlank String venue,
        @NotNull Instant startsAt,
        @Positive @Max(100000) Integer capacity) {
}
