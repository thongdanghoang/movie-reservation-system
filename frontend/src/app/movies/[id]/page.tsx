import { getMovie, getShowtimes } from '@/lib/api';
import { MovieDetail } from '@/features/movies/components/MovieDetail';
import { notFound } from 'next/navigation';

interface MoviePageProps {
    params: Promise<{ id: string }>;
}

export default async function MoviePage({ params }: MoviePageProps) {
    const { id } = await params;

    let movie;
    let showtimes;

    try {
        const [m, s] = await Promise.all([
            getMovie(id),
            getShowtimes(id),
        ]);
        movie = m;
        showtimes = s;
    } catch (error) {
        if (error instanceof Error && 'status' in error && (error as { status: number }).status === 404) {
            notFound();
        }
        throw error;
    }

    return <MovieDetail movie={movie} showtimes={showtimes} />;
}
