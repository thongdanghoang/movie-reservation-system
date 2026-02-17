DELETE FROM showtimes;
DELETE FROM movies;

INSERT INTO movies (id, title, poster_url, genre, status) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Inception', 'https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_.jpg', 'Sci-Fi', 'NOW_PLAYING'),
('550e8400-e29b-41d4-a716-446655440002', 'The Matrix', 'https://m.media-amazon.com/images/M/MV5BN2NmN2VhMTQtMDNiOS00NDlhLTliMjgtODE2ZTY0ODQyNDRhXkEyXkFqcGc@._V1_.jpg', 'Action', 'NOW_PLAYING'),
('550e8400-e29b-41d4-a716-446655440003', 'Interstellar', 'https://m.media-amazon.com/images/M/MV5BYzdjMDAxZGItMjI2My00ODA1LTlkNzItOWFjMDU5ZDJlYWY3XkEyXkFqcGc@._V1_.jpg', 'Adventure', 'NOW_PLAYING'),
('550e8400-e29b-41d4-a716-446655440004', 'Dune: Part Two', 'https://m.media-amazon.com/images/M/MV5BNTc0YmQxMjEtODI5MC00NjFiLTlkMWUtOGQ5NjFmYWUyZGJhXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg', 'Sci-Fi', 'COMING_SOON');

-- Showtimes for Inception (NOW_PLAYING)
INSERT INTO showtimes (id, movie_id, start_time, theater_name, available_seats) VALUES
('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', CURRENT_DATE + INTERVAL '10 hours', 'Theater 1 - IMAX', 120),
('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', CURRENT_DATE + INTERVAL '14 hours', 'Theater 2 - Standard', 80),
('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', CURRENT_DATE + INTERVAL '19 hours', 'Theater 1 - IMAX', 120),
('660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '10 hours', 'Theater 1 - IMAX', 120);

-- Showtimes for The Matrix (NOW_PLAYING)
INSERT INTO showtimes (id, movie_id, start_time, theater_name, available_seats) VALUES
('660e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', CURRENT_DATE + INTERVAL '11 hours', 'Theater 3 - Premium', 100),
('660e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440002', CURRENT_DATE + INTERVAL '16 hours', 'Theater 2 - Standard', 80),
('660e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440002', CURRENT_DATE + INTERVAL '21 hours', 'Theater 3 - Premium', 100);

-- Showtimes for Interstellar (NOW_PLAYING)
INSERT INTO showtimes (id, movie_id, start_time, theater_name, available_seats) VALUES
('660e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440003', CURRENT_DATE + INTERVAL '12 hours', 'Theater 4 - IMAX', 150),
('660e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440003', CURRENT_DATE + INTERVAL '17 hours', 'Theater 4 - IMAX', 150),
('660e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440003', CURRENT_DATE + INTERVAL '20 hours', 'Theater 2 - Standard', 80);

-- Showtimes for Dune: Part Two (COMING_SOON - future dates only)
INSERT INTO showtimes (id, movie_id, start_time, theater_name, available_seats) VALUES
('660e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440004', CURRENT_DATE + INTERVAL '7 days' + INTERVAL '10 hours', 'Theater 1 - IMAX', 120),
('660e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440004', CURRENT_DATE + INTERVAL '7 days' + INTERVAL '14 hours', 'Theater 2 - Standard', 80),
('660e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440004', CURRENT_DATE + INTERVAL '7 days' + INTERVAL '19 hours', 'Theater 1 - IMAX', 120);
