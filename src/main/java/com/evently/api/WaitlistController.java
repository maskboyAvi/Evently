package com.evently.api;

import com.evently.domain.Waitlist;
import com.evently.repo.WaitlistRepository;
import com.evently.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {
    private final WaitlistRepository waitlists;
    private final UserRepository users;
    // EventRepository reserved for future enrichment (e.g., capacity snapshot) -
    // removed for now.
    // private final EventRepository events;

    @GetMapping("/event/{eventId}")
    public java.util.List<java.util.Map<String, Object>> byEvent(@PathVariable Long eventId) {
        return waitlists.findByEventIdOrderByPositionAsc(eventId).stream()
                .map(w -> {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", w.getId());
                    m.put("userId", w.getUser().getId());
                    m.put("eventId", w.getEvent().getId());
                    m.put("position", w.getPosition());
                    m.put("createdAt", w.getCreatedAt());
                    return m;
                }).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Waitlist w = waitlists.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String email = String.valueOf(auth.getPrincipal());
        var user = users.findByEmail(email).orElse(null);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (!isAdmin && !w.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        waitlists.delete(w);
    }
}