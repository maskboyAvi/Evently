package com.evently.service;

import com.evently.domain.Role;
import com.evently.domain.User;
import com.evently.repo.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;

    public List<User> list() {
        return users.findAll();
    }

    @Transactional
    public User create(@NotBlank @Email String email, @NotBlank String name, Role role) {
        return users.findByEmail(email).orElseGet(() -> users.save(User.builder()
                .email(email)
                .name(name)
                .role(role == null ? Role.USER : role)
                .build()));
    }
}
