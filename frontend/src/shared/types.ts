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
