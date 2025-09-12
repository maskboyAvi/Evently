package com.evently.config;

import com.evently.domain.Event;
import com.evently.domain.Role;
import com.evently.domain.User;
import com.evently.repo.EventRepository;
import com.evently.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsersAndEvents(UserRepository users, EventRepository events, PasswordEncoder encoder) {
        return args -> {
            users.findByEmail("admin@evently.local").orElseGet(() -> users
                    .save(User.builder().email("admin@evently.local").name("Admin").role(Role.ADMIN)
                            .passwordHash(encoder.encode("admin123"))
                            .build()));

            users.findByEmail("user@evently.local").orElseGet(() -> users
                    .save(User.builder().email("user@evently.local").name("Test User").role(Role.USER)
                            .passwordHash(encoder.encode("user123"))
                            .build()));

            if (events.count() == 0) {
                Event e = Event.builder()
                        .name("Sample Concert")
                        .venue("City Hall")
                        .startsAt(Instant.now().plus(Duration.ofDays(7)))
                        .capacity(50)
                        .bookedCount(0)
                        .build();
                events.save(e);
            }
        };
    }
}
