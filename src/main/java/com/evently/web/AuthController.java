package com.evently.web;

import com.evently.domain.Role;
import com.evently.domain.User;
import com.evently.dto.SignupRequest;
import com.evently.repo.UserRepository;
import com.evently.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtService jwtService;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService, UserRepository users, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    // Deprecated demo /token endpoint removed – real signup/login only.

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String email = String.valueOf(auth.getPrincipal());
        Set<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return Map.of(
                "email", email,
                "roles", roles);
    }

    // Public signup: creates USER by default; if role=ADMIN is requested, require
    // current auth to have ADMIN
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> signup(@RequestBody SignupRequest req, Authentication auth) {
        Role desired = req.role() == null ? Role.USER : req.role();
        if (desired == Role.ADMIN) {
            if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can create ADMIN users");
            }
        }
        User u = users.findByEmail(req.email()).orElseGet(() -> {
            User nu = User.builder()
                    .email(req.email())
                    .name(req.name())
                    .role(desired)
                    .passwordHash(passwordEncoder.encode(req.password()))
                    .build();
            return users.save(nu);
        });
        String token = jwtService.generateToken(u.getEmail(), Map.of("role", u.getRole().name()));
        return Map.of("id", u.getId(), "email", u.getEmail(), "role", u.getRole().name(), "token", token);
    }

    // Login with email + password
    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String email, @RequestParam String password) {
        User u = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (u.getPasswordHash() == null || !passwordEncoder.matches(password, u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtService.generateToken(u.getEmail(), Map.of("role", u.getRole().name()));
        return Map.of("token", token);
    }
}
