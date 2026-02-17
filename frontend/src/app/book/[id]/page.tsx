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
                <div className="flex items-center justify-center min-h-[400px]">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                    <h2 className="text-lg font-semibold text-red-800">Error</h2>
                    <p className="text-red-600">{error}</p>
                </div>
            </div>
        );
    }

    const availableSeats = seats.filter(s => s.status === 'AVAILABLE').length;
    const heldSeats = seats.filter(s => s.status === 'HELD').length;
    const soldSeats = seats.filter(s => s.status === 'SOLD').length;

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
