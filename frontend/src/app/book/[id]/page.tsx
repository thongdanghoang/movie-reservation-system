'use client';

import { useEffect, useState } from 'react';
import { SeatMap } from '@/features/seatmap/components/SeatMap';
import { useSeatWebSocket } from '@/features/seatmap/hooks/useSeatWebSocket';
import { useSeatStore } from '@/stores/seatStore';
import { getSeats } from '@/lib/api';
import type { Seat, ShowtimeDetails } from '@/shared/types';

interface BookingPageProps {
    params: Promise<{ id: string }>;
}

export default function BookingPage({ params }: BookingPageProps) {
    const [showtimeId, setShowtimeId] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    
    const setSeats = useSeatStore((state) => state.setSeats);
    const selectedSeats = useSeatStore((state) => state.selectedSeats);
    const seats = useSeatStore((state) => state.seats);
    
    useSeatWebSocket(showtimeId);

    useEffect(() => {
        params.then(({ id }) => {
            setShowtimeId(id);
        });
    }, [params]);

    useEffect(() => {
        if (!showtimeId) return;

        const fetchSeats = async () => {
            try {
                setLoading(true);
                const seatsData = await getSeats(showtimeId);
                setSeats(seatsData);
                setError(null);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Failed to load seats');
            } finally {
                setLoading(false);
            }
        };

        fetchSeats();
    }, [showtimeId, setSeats]);

    const toggleSeatSelection = useSeatStore((state) => state.toggleSeatSelection);

    const handleSeatClick = (seat: Seat) => {
        toggleSeatSelection(seat.id);
    };

    if (loading) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="flex flex-col items-center justify-center min-h-[400px]">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
                    <p className="text-gray-500">Loading seat map...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="bg-red-50 border border-red-200 rounded-lg p-6">
                    <h2 className="text-lg font-semibold text-red-800 mb-2">Failed to Load Seats</h2>
                    <p className="text-red-600 mb-4">{error}</p>
                    <div className="flex gap-3">
                        <button 
                            onClick={() => window.location.reload()}
                            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                        >
                            Try Again
                        </button>
                        <button 
                            onClick={() => window.history.back()}
                            className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
                        >
                            Go Back
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    const availableSeats = seats.filter(s => s.status === 'AVAILABLE').length;
    const heldSeats = seats.filter(s => s.status === 'HELD').length;
    const soldSeats = seats.filter(s => s.status === 'SOLD').length;

    // Show empty state when no seats are available
    if (seats.length === 0 && !loading) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="mb-6">
                    <h1 className="text-2xl font-bold mb-2">Select Your Seats</h1>
                </div>
                
                <div className="flex flex-col items-center justify-center min-h-[400px] bg-gray-50 rounded-lg border border-gray-200 p-8">
                    <svg 
                        className="w-16 h-16 text-gray-400 mb-4" 
                        fill="none" 
                        stroke="currentColor" 
                        viewBox="0 0 24 24"
                    >
                        <path 
                            strokeLinecap="round" 
                            strokeLinejoin="round" 
                            strokeWidth={1.5} 
                            d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" 
                        />
                    </svg>
                    <h2 className="text-xl font-semibold text-gray-700 mb-2">No Seats Available</h2>
                    <p className="text-gray-500 text-center max-w-md">
                        There are no seats configured for this showtime. Please try selecting a different showtime.
                    </p>
                    <button 
                        onClick={() => window.history.back()}
                        className="mt-6 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        Go Back
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="mb-6">
                <h1 className="text-2xl font-bold mb-2">Select Your Seats</h1>
                <p className="text-gray-600">
                    {seats.length > 0 && (
                        <>
                            {availableSeats} available · {heldSeats} held · {soldSeats} sold
                        </>
                    )}
                </p>
            </div>

            {selectedSeats.length > 0 && (
                <div className="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                    <p className="text-blue-800">
                        <span className="font-medium">{selectedSeats.length}</span> seat(s) selected
                    </p>
                </div>
            )}

            <SeatMap onSeatClick={handleSeatClick} />
        </div>
    );
}
