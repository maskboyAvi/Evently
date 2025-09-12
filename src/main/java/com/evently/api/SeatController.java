package com.evently.api;

import com.evently.api.dto.SeatDto;
import com.evently.api.dto.SeatGenerationRequest;
import com.evently.domain.Event;
import com.evently.domain.Seat;
import com.evently.domain.SeatStatus;
import com.evently.repo.EventRepository;
import com.evently.repo.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {
    private final SeatRepository seats;
    private final EventRepository events;

    @GetMapping("/event/{eventId}")
    public List<SeatDto> list(@PathVariable Long eventId) {
        return seats.findByEventId(eventId).stream().map(SeatDto::from).toList();
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SeatDto> generate(@Valid @RequestBody SeatGenerationRequest req) {
        Event event = events.findById(req.eventId()).orElseThrow(() -> new EntityNotFoundException("Event not found"));
        List<Seat> existing = seats.findByEventId(event.getId());
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("Seats already exist for event");
        }
        List<Seat> batch = new ArrayList<>();
        for (int r = 0; r < req.rows(); r++) {
            char rowChar = (char) ('A' + r);
            String rowPrefix = req.rowPrefix() != null && !req.rowPrefix().isBlank() ? req.rowPrefix()
                    : String.valueOf(rowChar);
            for (int s = 1; s <= req.seatsPerRow(); s++) {
                String label = rowPrefix + s;
                batch.add(Seat.builder().event(event).label(label).status(SeatStatus.AVAILABLE).build());
            }
        }
        return seats.saveAll(batch).stream().map(SeatDto::from).toList();
    }
}
