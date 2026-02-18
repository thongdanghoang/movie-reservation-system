import { Movie, Showtime, Seat, HoldSeatResponse } from '@/shared/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
    constructor(public status: number, message: string) {
        super(message);
        this.name = 'ApiError';
    }
}

export async function getNowPlayingMovies(): Promise<Movie[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/movies/now-playing`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        throw new ApiError(response.status, `Failed to fetch movies: ${response.statusText}`);
    }

    return response.json();
}

export async function getMovie(movieId: string): Promise<Movie> {
    const response = await fetch(`${API_BASE_URL}/api/v1/movies/${encodeURIComponent(movieId)}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw new ApiError(404, `Movie not found`);
        }
        throw new ApiError(response.status, `Failed to fetch movie: ${response.statusText}`);
    }

    return response.json();
}

export async function getShowtimes(movieId: string, date?: string): Promise<Showtime[]> {
    const dateParam = date ? `?date=${encodeURIComponent(date)}` : '';
    const response = await fetch(`${API_BASE_URL}/api/v1/movies/${encodeURIComponent(movieId)}/showtimes${dateParam}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        throw new ApiError(response.status, `Failed to fetch showtimes: ${response.statusText}`);
    }

    return response.json();
}

export async function getSeats(showtimeId: string): Promise<Seat[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/showtimes/${encodeURIComponent(showtimeId)}/seats`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw new ApiError(404, `Showtime not found`);
        }
        throw new ApiError(response.status, `Failed to fetch seats: ${response.statusText}`);
    }

    return response.json();
}

export class SeatTakenError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'SeatTakenError';
    }
}

export async function holdSeat(seatId: string, sessionId: string): Promise<HoldSeatResponse> {
    const response = await fetch(`${API_BASE_URL}/api/v1/seats/${encodeURIComponent(seatId)}/hold`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ sessionId }),
    });

    if (response.status === 409) {
        const error = await response.json();
        throw new SeatTakenError(error.message || 'Seat is no longer available');
    }

    if (!response.ok) {
        throw new ApiError(response.status, `Failed to hold seat: ${response.statusText}`);
    }

    return response.json();
}
