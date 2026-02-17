import { Movie, Showtime } from '@/shared/types';
import Image from 'next/image';
import { ShowtimeList } from './ShowtimeList';

interface MovieDetailProps {
    movie: Movie;
    showtimes: Showtime[];
}

export function MovieDetail({ movie, showtimes }: MovieDetailProps) {
    return (
        <div className="container mx-auto px-4 py-8 max-w-6xl">
            {/* Movie Info Section */}
            <div className="flex flex-col md:flex-row gap-8 mb-12">
                {/* Poster */}
                <div className="relative aspect-[2/3] w-full md:w-80 flex-shrink-0 overflow-hidden rounded-lg shadow-lg">
                    <Image
                        src={movie.posterUrl}
                        alt={movie.title}
                        fill
                        className="object-cover"
                        sizes="(max-width: 768px) 100vw, 320px"
                        priority
                    />
                </div>

                {/* Info */}
                <div className="flex-1">
                    <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
                        {movie.title}
                    </h1>
                    <p className="text-lg text-gray-600 mb-6">{movie.genre}</p>
                    <div className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
                        Now Playing
                    </div>
                </div>
            </div>

            {/* Showtimes Section */}
            <ShowtimeList showtimes={showtimes} />
        </div>
    );
}
