import { getMovie, getShowtimes } from '@/lib/api';
import { MovieDetail } from '@/features/movies/components/MovieDetail';

interface MoviePageProps {
    params: Promise<{ id: string }>;
}

export default async function MoviePage({ params }: MoviePageProps) {
    const { id } = await params;

    const [movie, showtimes] = await Promise.all([
        getMovie(id),
        getShowtimes(id),
    ]);

    return <MovieDetail movie={movie} showtimes={showtimes} />;
}
