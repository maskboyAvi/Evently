-- Users table
CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER', -- USER | ADMIN
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Events table
CREATE TABLE IF NOT EXISTS event (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    venue VARCHAR(255) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity >= 0),
    booked_count INTEGER NOT NULL DEFAULT 0 CHECK (booked_count >= 0),
    version BIGINT NOT NULL DEFAULT 0, -- for optimistic locking
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_event_starts_at ON event (starts_at);

CREATE INDEX IF NOT EXISTS idx_event_capacity ON event (capacity);

-- Seat map (optional per event). If present, booking can reserve seat_id specifically
CREATE TABLE IF NOT EXISTS seat (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES event (id) ON DELETE CASCADE,
    label VARCHAR(32) NOT NULL, -- e.g., A1, B12
    status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE | HELD | BOOKED
    UNIQUE (event_id, label)
);

-- Bookings
CREATE TABLE IF NOT EXISTS booking (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES event (id) ON DELETE CASCADE,
    seat_id BIGINT NULL REFERENCES seat (id) ON DELETE SET NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'CONFIRMED', -- CONFIRMED | CANCELED
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, event_id, seat_id)
);

CREATE INDEX IF NOT EXISTS idx_booking_event ON booking (event_id);

CREATE INDEX IF NOT EXISTS idx_booking_user ON booking (user_id);

-- Waitlist
CREATE TABLE IF NOT EXISTS waitlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES event (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_waitlist_event_position ON waitlist (event_id, position);

-- Triggers to maintain updated_at and enforce capacity bookkeeping
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_event_updated_at') THEN
    CREATE TRIGGER trg_event_updated_at BEFORE UPDATE ON event
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_booking_updated_at') THEN
    CREATE TRIGGER trg_booking_updated_at BEFORE UPDATE ON booking
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_user_updated_at') THEN
    CREATE TRIGGER trg_user_updated_at BEFORE UPDATE ON app_user
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;