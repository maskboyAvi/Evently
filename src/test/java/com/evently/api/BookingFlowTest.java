package com.evently.api;

import com.evently.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtService jwtService;

    @Test
    void bookAndListMyBookings() throws Exception {
        String token = jwtService.generateToken("user@evently.local", java.util.Map.of("role", "USER"));
        // get seeded event
        mvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("{\"eventId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1));

        mvc.perform(get("/api/bookings/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.eventId == 1)]").exists());
    }
}