package com.evently.service;

import com.evently.api.dto.CreateEventRequest;
import com.evently.api.dto.UpdateEventRequest;
import com.evently.domain.Event;
import com.evently.repo.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository events;

    public List<Event> list() {
        return events.findAll();
    }

    @Transactional
    public Event create(CreateEventRequest req) {
        Event e = Event.builder()
                .name(req.name())
                .venue(req.venue())
                .startsAt(req.startsAt())
                .capacity(req.capacity())
                .build();
        return events.save(e);
    }

    @Transactional
    public Event update(Long id, UpdateEventRequest req) {
        Event e = events.findById(id).orElseThrow(() -> new EntityNotFoundException("Event not found"));
        if (req.version() != null && !req.version().equals(e.getVersion())) {
            throw new IllegalArgumentException("Event version mismatch (optimistic lock)");
        }
        e.setName(req.name());
        e.setVenue(req.venue());
        e.setStartsAt(req.startsAt());
        e.setCapacity(req.capacity());
        return events.save(e);
    }
}
