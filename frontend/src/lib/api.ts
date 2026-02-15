import { Movie } from '@/shared/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export async function getNowPlayingMovies(): Promise<Movie[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/movies/now-playing`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        throw new Error(`Failed to fetch movies: ${response.statusText}`);
    }

    return response.json();
}
