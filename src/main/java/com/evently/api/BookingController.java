package com.evently.api;

import com.evently.api.dto.BookingDto;
import com.evently.api.dto.BookingRequest;
import com.evently.repo.BookingRepository;
import com.evently.repo.UserRepository;
import com.evently.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserRepository users;

    @PostMapping
    public BookingDto book(@Valid @RequestBody BookingRequest req,
            org.springframework.security.core.Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        String email = String.valueOf(auth.getPrincipal());
        Long userId = users.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();
        return BookingDto.from(bookingService.book(userId, req));
    }

    @PostMapping("/{id}/cancel")
    public BookingDto cancel(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        // Authorization: users can cancel their own bookings; admins can cancel any
        var booking = bookingRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
        String email = String.valueOf(auth.getPrincipal());
        var user = users.findByEmail(email).orElse(null);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (user == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        if (!isAdmin && !booking.getUser().getId().equals(user.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
        return BookingDto.from(bookingService.cancel(id));
    }

    @GetMapping("/me")
    public List<BookingDto> myBookings(org.springframework.security.core.Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        String email = String.valueOf(auth.getPrincipal());
        Long userId = users.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();
        return bookingRepository.findDtosByUserId(userId);
    }
}
