package com.evently.api.dto;

import java.time.Instant;

public record EventDto(Long id, String name, String venue, Instant startsAt, Integer capacity, Integer bookedCount) {
}
