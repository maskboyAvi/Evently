package com.evently.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class JwtServiceTest {

    @Test
    void generateAndParseRoundTrip() {
        // Using default constructor values (secret + ttl) via explicit secret for
        // determinism
        String secret = "ZmFrZV9zZWNyZXRfZm9yX2RlbW9fYW5kX2xvY2FsX3Rlc3Rz"; // matches default
        JwtService svc = new JwtService(secret, 3600);
        String token = svc.generateToken("user@example.com", Map.of("role", "USER"));
        assertNotNull(token);
        var claims = svc.parse(token);
        assertEquals("user@example.com", claims.getSubject());
        assertEquals("USER", claims.get("role"));
        assertNotNull(claims.getExpiration());
    }
}