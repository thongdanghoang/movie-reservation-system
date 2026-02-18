ALTER TABLE seats ADD COLUMN held_at TIMESTAMP NULL;
ALTER TABLE seats ADD COLUMN held_by VARCHAR(255) NULL;
ALTER TABLE seats ADD COLUMN reservation_id UUID NULL;

CREATE INDEX idx_seats_held_at ON seats(held_at) WHERE held_at IS NOT NULL;
CREATE INDEX idx_seats_reservation_id ON seats(reservation_id) WHERE reservation_id IS NOT NULL;
