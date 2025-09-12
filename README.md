# Evently Backend

Production-ready Spring Boot backend for event browsing, booking (with seat selection), waitlists, analytics, and JWT auth.

## Features

- Users: signup/login (JWT), browse events, book & cancel, view own bookings.
- Admins: create/update events, generate seats, view analytics.
- Concurrency: optimistic locking on events + retry loop.
- Waitlist: automatic add when full + promotion on cancellation.
- Seats: labeled seats per event; selection while booking.
- Analytics: overview, top events, daily confirmed, cancellation rate.
- Security: stateless JWT (HS256), role-based access.
- Migrations: Flyway versioned schema.
- Tests: unit + (extensible) integration test scaffolding.

## Quick Start (Local)

1. Ensure PostgreSQL running locally (or adjust `application.properties`).
2. Set env (optional): `APP_JWT_SECRET` (Base64 48+ bytes), `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
3. Run: `mvn spring-boot:run`.
4. Swagger UI: http://localhost:8080/swagger-ui/index.html

Seeded users:

- admin@evently.local / admin123
- user@evently.local / user123

## Booking Flow

1. Authenticate -> get token.
2. POST /api/bookings { "eventId": <id>, "seatId": optional }
3. If full -> 400 (waitlist joined automatically).
4. Cancel -> auto-promotes first waitlisted user.

## Docker

Local (with included compose):

```
cd docker
docker compose up --build
```

App: http://localhost:8080 DB: localhost:5432

## Testing

`mvn test`

## Roadmap / Stretch

- Seat hold TTL + expiration job
- Notifications (WebSocket / email mock)
- Caching popular endpoints
- Sharding discussion doc

---

See `task.md` for full specification mapping.

## Deployment (Production)

1. Create Postgres (Railway / Render). Note credentials.
2. Set env vars:
   - SPRING_DATASOURCE_URL
   - SPRING_DATASOURCE_USERNAME
   - SPRING_DATASOURCE_PASSWORD
   - APP_JWT_SECRET (Base64 256-bit)
   - APP_CORS_ORIGINS (comma list of frontend origins)
3. Build & deploy (platform auto-detects Dockerfile).
4. Ensure `SPRING_PROFILES_ACTIVE=prod` (Dockerfile already sets).
5. Verify /actuator/health then /swagger-ui.html.
6. Rotate secrets & add monitoring.

Diagrams: see `architecture.puml`, `er.puml` (render via https://www.plantuml.com/plantuml/ ).

# Evently

Backend for Ticket Booking System
