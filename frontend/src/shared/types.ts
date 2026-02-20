export interface Movie {
    id: string;
    title: string;
    posterUrl: string;
    genre: string;
    status: 'NOW_PLAYING' | 'COMING_SOON';
}

export interface Showtime {
    id: string;
    movieId: string;
    startTime: string;
    theaterName: string;
    availableSeats: number;
}

export interface MoviesResponse {
    movies: Movie[];
}

export type SeatStatus = 'AVAILABLE' | 'HELD' | 'SOLD';

export interface Seat {
    id: string;
    seatRow: string;
    seatColumn: number;
    status: SeatStatus;
}

export interface SeatUpdate {
    seatId: string;
    seatRow: string;
    seatColumn: number;
    status: SeatStatus;
}

export interface ShowtimeDetails {
    id: string;
    movieTitle: string;
    startTime: string;
    theaterName: string;
}

export interface HoldSeatResponse {
    seatId: string;
    status: SeatStatus;
    heldAt: string;
    holdExpiresAt: string;
    reservationId: string;
}

export interface PaymentResponse {
    reservationId: string;
    status: SeatStatus;
    seatId: string;
    confirmationNumber: string;
}
