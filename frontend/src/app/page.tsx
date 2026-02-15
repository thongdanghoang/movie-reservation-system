'use client';

import { useQuery } from '@tanstack/react-query';
import { getNowPlayingMovies } from '@/lib/api';
import { MovieGrid } from '@/features/movies/components/MovieGrid';

export default function HomePage() {
  const { data: movies, isLoading, error } = useQuery({
    queryKey: ['movies', 'now-playing'],
    queryFn: getNowPlayingMovies,
  });

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <header className="mb-8">
          <h1 className="text-4xl font-bold text-gray-900">Now Playing</h1>
          <p className="mt-2 text-gray-600">
            Discover the latest movies in theaters
          </p>
        </header>

        {isLoading && (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">
              Failed to load movies. Please try again later.
            </p>
          </div>
        )}

        {movies && <MovieGrid movies={movies} />}
      </div>
    </main>
  );
}
