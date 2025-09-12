package com.evently.dto;

import com.evently.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @Email @NotBlank String email,
        @NotBlank String name,
        @NotBlank String password,
        Role role // optional; will be coerced to USER unless caller is ADMIN
) {
}
