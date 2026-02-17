'use client';

import { Showtime } from '@/shared/types';
import { useRouter } from 'next/navigation';

interface ShowtimeListProps {
    showtimes: Showtime[];
}

export function ShowtimeList({ showtimes }: ShowtimeListProps) {
    const router = useRouter();

    const handleShowtimeClick = (showtimeId: string) => {
        router.push(`/book/${showtimeId}`);
    };

    const formatTime = (isoString: string) => {
        const date = new Date(isoString);
        return date.toLocaleTimeString('en-US', {
            hour: 'numeric',
            minute: '2-digit',
            hour12: true,
        });
    };

    const isPast = (isoString: string) => {
        const showtimeDate = new Date(isoString);
        const now = new Date();
        return showtimeDate < now;
    };

    const isNearPast = (isoString: string) => {
        const showtimeDate = new Date(isoString);
        const now = new Date();
        const diffMs = showtimeDate.getTime() - now.getTime();
        const diffMins = diffMs / (1000 * 60);
        return diffMins > 0 && diffMins <= 15;
    };

    if (showtimes.length === 0) {
        return (
            <div className="text-center py-8">
                <p className="text-gray-500">No showtimes available for today</p>
            </div>
        );
    }

    return (
        <div className="space-y-3">
            <h2 className="text-xl font-semibold text-gray-900">Today&apos;s Showtimes</h2>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                {showtimes.map((showtime) => {
                    const past = isPast(showtime.startTime);
                    const nearPast = isNearPast(showtime.startTime);

                    return (
                        <button
                            key={showtime.id}
                            onClick={() => !past && handleShowtimeClick(showtime.id)}
                            disabled={past}
                            className={`p-4 rounded-lg border text-left transition-all ${
                                past
                                    ? 'bg-gray-100 border-gray-200 opacity-50 cursor-not-allowed'
                                    : nearPast
                                      ? 'bg-yellow-50 border-yellow-200 hover:bg-yellow-100'
                                      : 'bg-white border-gray-200 hover:border-blue-500 hover:shadow-md'
                            }`}
                        >
                            <div className={`font-semibold ${past ? 'text-gray-500' : 'text-gray-900'}`}>
                                {formatTime(showtime.startTime)}
                            </div>
                            <div className="text-sm text-gray-600 mt-1">{showtime.theaterName}</div>
                            <div className="text-xs text-gray-500 mt-1">
                                {showtime.availableSeats} seats available
                            </div>
                        </button>
                    );
                })}
            </div>
        </div>
    );
}
