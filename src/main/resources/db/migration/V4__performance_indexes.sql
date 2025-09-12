-- Performance & analytics aiding indexes
CREATE INDEX IF NOT EXISTS idx_booking_event_status ON booking (event_id, status);

CREATE INDEX IF NOT EXISTS idx_booking_user ON booking (user_id);

CREATE INDEX IF NOT EXISTS idx_waitlist_event_position ON waitlist (event_id, position);

CREATE INDEX IF NOT EXISTS idx_seat_event_status ON seat (event_id, status);