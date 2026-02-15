import { Movie } from '@/shared/types';
import Image from 'next/image';

interface MovieCardProps {
    movie: Movie;
    onClick?: () => void;
}

export function MovieCard({ movie, onClick }: MovieCardProps) {
    return (
        <div
            onClick={onClick}
            className="group cursor-pointer rounded-lg border border-gray-200 bg-white shadow-sm transition-all hover:shadow-lg hover:scale-105"
        >
            <div className="relative aspect-[2/3] w-full overflow-hidden rounded-t-lg bg-gray-100">
                <Image
                    src={movie.posterUrl}
                    alt={movie.title}
                    fill
                    className="object-cover"
                    sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                />
            </div>
            <div className="p-4">
                <h3 className="text-lg font-semibold text-gray-900 line-clamp-1">
                    {movie.title}
                </h3>
                <p className="mt-1 text-sm text-gray-600">{movie.genre}</p>
            </div>
        </div>
    );
}
