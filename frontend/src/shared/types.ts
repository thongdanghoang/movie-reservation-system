export interface Movie {
    id: string;
    title: string;
    posterUrl: string;
    genre: string;
    status: 'NOW_PLAYING' | 'COMING_SOON';
}

export interface MoviesResponse {
    movies: Movie[];
}
