package com.evently.web;

import com.evently.domain.Role;
import com.evently.domain.User;
import com.evently.dto.CreateUserRequest;
import com.evently.dto.UserResponse;
import com.evently.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponse> list() {
        return userService.list().stream().map(UserResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest req, Authentication auth) {
        Role desired = req.getRole();
        boolean isAdmin = auth != null
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (desired == Role.ADMIN && !isAdmin) {
            // Coerce to USER if non-admin attempts to create an admin
            desired = Role.USER;
        }
        User u = userService.create(req.getEmail(), req.getName(), desired);
        return UserResponse.from(u);
    }
}
