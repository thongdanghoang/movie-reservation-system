'use client';

import { Movie } from '@/shared/types';
import { MovieCard } from './MovieCard';
import { useRouter } from 'next/navigation';

interface MovieGridProps {
    movies: Movie[];
}

export function MovieGrid({ movies }: MovieGridProps) {
    const router = useRouter();

    const handleMovieClick = (movieId: string) => {
        router.push(`/movies/${movieId}`);
    };

    if (movies.length === 0) {
        return (
            <div className="text-center py-12">
                <p className="text-gray-500 text-lg">No movies currently playing</p>
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {movies.map((movie) => (
                <MovieCard
                    key={movie.id}
                    movie={movie}
                    onClick={() => handleMovieClick(movie.id)}
                />
            ))}
        </div>
    );
}
