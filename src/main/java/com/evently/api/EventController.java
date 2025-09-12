package com.evently.api;

import com.evently.api.dto.CreateEventRequest;
import com.evently.api.dto.EventDto;
import com.evently.api.dto.UpdateEventRequest;
import com.evently.domain.Event;
import com.evently.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping
    public List<EventDto> list() {
        return eventService.list().stream()
                .map(e -> new EventDto(e.getId(), e.getName(), e.getVenue(), e.getStartsAt(), e.getCapacity(),
                        e.getBookedCount()))
                .toList();
    }

    @PostMapping
    public EventDto create(@Valid @RequestBody CreateEventRequest req) {
        Event e = eventService.create(req);
        return new EventDto(e.getId(), e.getName(), e.getVenue(), e.getStartsAt(), e.getCapacity(), e.getBookedCount());
    }

    @PutMapping("/{id}")
    public EventDto update(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest req) {
        Event e = eventService.update(id, req);
        return new EventDto(e.getId(), e.getName(), e.getVenue(), e.getStartsAt(), e.getCapacity(), e.getBookedCount());
    }
}
