# Evently Design & Trade-offs

## Goals

Provide a scalable, correct ticket booking backend with concurrency safety, clear APIs, analytics, and extensibility for real-time features.

## Concurrency Strategy

- Optimistic locking on `Event` row (version field) ensures no oversell: booking increments `booked_count`, retries on `OptimisticLockException` up to 3 times.
- Seat selection: seat row status updated (simple state transition). A future hardening could add an optimistic version on `Seat` for high contention.
- Waitlist promotion runs in same transaction on cancellation; small race window acceptable. Future: move to async event.

Alternatives considered:

- Pessimistic locking: higher contention & blocking under bursts.
- Queue (e.g., Kafka) for bookings: strong serialization but added latency & complexity—deferred until scaling requires.

## Data Integrity

- Invariants: `booked_count <= capacity`. Enforced by optimistic update & retry.
- Unique constraints: prevent duplicate booking per (user,event,seat) AND duplicate waitlist entries.
- Flyway migrations version schema.

## Scalability & Performance

- Vertical: stateless service behind load balancer; JWT avoids server session.
- Horizontal: all state in Postgres; idempotent booking attempts.
- Index plan (V4 migration): composite indexes to accelerate analytics & lookups.
- Caching (future): top events analytics & event list.
- Sharding (future): partition bookings by event or time window; out of present scope.

## Security

- JWT (HS256) signed with configurable secret; roles (USER/ADMIN) in claims.
- Controllers enforce role boundaries; booking now tied to principal.
- CORS configurable via env for frontend integration.

## Error Handling

- Central exception translation (ApiExceptionHandler) ensures consistent JSON.
- Validation via Bean Validation annotations.

## Analytics

- Derived from Booking + Event: overview, top events, daily confirmed, cancellation rate.
- Potential future: materialized views or time-series DB if volume grows.

## Extensibility

- Domain events layer placeholder (future outbox/queue integration) for notifications (waitlist promotion).
- PlantUML diagrams (`architecture.puml`, `er.puml`).

## Deployment

- Multi-stage Docker builds minimal JRE layer.
- prod profile externalizes credentials & reduces logs.
- Platform: Render/Railway – supply env vars, Postgres addon, automatic image build.

## Future Enhancements (Deferred)

- Notification system (WebSocket / email / queue).
- Seat hold TTL with expiration scheduler (avoids ghost reservations).
- Additional indexes after observing slow queries.
- Rate limiting / abuse protection.
- Observability: tracing (OpenTelemetry), metrics dashboards.

## Testing

- Unit: JWT service sample
- Integration: Booking flow
- Missing (deferred): concurrency stress test, analytics correctness, security boundary tests.

## Trade-offs Summary

| Concern             | Choice             | Rationale                                               |
| ------------------- | ------------------ | ------------------------------------------------------- |
| Oversell prevention | Optimistic locking | Simple & low overhead for mostly non-conflicting writes |
| Complex ordering    | In-memory retry    | Avoids queue overhead initially                         |
| Password storage    | BCrypt             | Industry standard                                       |
| Analytics           | On-demand queries  | Simplicity until volume necessitates pre-aggregation    |
| Architecture style  | Monolith (modular) | Faster iteration; can later extract services            |
