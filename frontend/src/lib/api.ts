import { Movie, Showtime } from '@/shared/types';

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
