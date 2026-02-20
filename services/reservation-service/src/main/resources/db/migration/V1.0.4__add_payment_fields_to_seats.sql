ALTER TABLE seats ADD COLUMN email VARCHAR(255) NULL;
ALTER TABLE seats ADD COLUMN phone VARCHAR(50) NULL;
ALTER TABLE seats ADD COLUMN paid_at TIMESTAMP WITH TIME ZONE NULL;

CREATE INDEX idx_seats_email ON seats(email);

-- Enforce uniqueness so getSingleResultOrNull() can never throw NonUniqueResultException
ALTER TABLE seats ADD CONSTRAINT uq_seats_reservation_id UNIQUE (reservation_id);
