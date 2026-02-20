'use client';

import { useEffect, useState, useCallback } from 'react';
import { SeatMap } from '@/features/seatmap/components/SeatMap';
import { HoldTimer } from '@/features/booking/components/HoldTimer';
import { CheckoutForm } from '@/features/booking/components/CheckoutForm';
import { useSeatWebSocket } from '@/features/seatmap/hooks/useSeatWebSocket';
import { useSeatStore } from '@/stores/seatStore';
import { getSeats, holdSeat, deleteSeatHold, SeatTakenError } from '@/lib/api';
import type { Seat, ShowtimeDetails } from '@/shared/types';
import { toast } from 'sonner';

// Generate a session ID for this booking session
const SESSION_ID = typeof window !== 'undefined'
    ? localStorage.getItem('booking_session_id') || crypto.randomUUID()
    : crypto.randomUUID();

if (typeof window !== 'undefined') {
    localStorage.setItem('booking_session_id', SESSION_ID);
}

interface BookingPageProps {
    params: Promise<{ id: string }>;
}

export default function BookingPage({ params }: BookingPageProps) {
    const [showtimeId, setShowtimeId] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const setSeats = useSeatStore((state) => state.setSeats);
    const updateSeatStatus = useSeatStore((state) => state.updateSeatStatus);
    const selectedSeats = useSeatStore((state) => state.selectedSeats);
    const seats = useSeatStore((state) => state.seats);
    const heldSeat = useSeatStore((state) => state.heldSeat);
    const isHolding = useSeatStore((state) => state.isHolding);
    const setHeldSeat = useSeatStore((state) => state.setHeldSeat);
    const setIsHolding = useSeatStore((state) => state.setIsHolding);
    const clearHold = useSeatStore((state) => state.clearHold);

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

    const handleSeatClick = useCallback(async (seat: Seat) => {
        // Only allow clicking AVAILABLE seats
        if (seat.status !== 'AVAILABLE') {
            if (seat.status === 'HELD') {
                toast.info('Seat already held', {
                    description: 'This seat is being held by another user. Try another seat.'
                });
            } else if (seat.status === 'SOLD') {
                toast.info('Seat sold', {
                    description: 'This seat has already been sold. Try another seat.'
                });
            }
            return;
        }

        // If we already have a held seat, release it first
        if (heldSeat) {
            clearHold();
        }

        setIsHolding(true);

        try {
            const response = await holdSeat(seat.id, SESSION_ID);

            // Success - update local state
            updateSeatStatus(seat.id, 'HELD');
            setHeldSeat(response);

            toast.success('Seat held!', {
                description: 'You have 5 minutes to complete your booking.'
            });
        } catch (error) {
            if (error instanceof SeatTakenError) {
                toast.error('Seat Taken', {
                    description: 'Someone else just grabbed this seat. Try another one!'
                });
                // Refresh seat map to get latest status
                const seatsData = await getSeats(showtimeId!);
                setSeats(seatsData);
            } else {
                toast.error('Error', {
                    description: error instanceof Error ? error.message : 'Failed to hold seat'
                });
            }
        } finally {
            setIsHolding(false);
        }
    }, [heldSeat, showtimeId, updateSeatStatus, setHeldSeat, setIsHolding, setSeats, clearHold]);

    const handleHoldExpire = useCallback(() => {
        if (heldSeat) {
            updateSeatStatus(heldSeat.seatId, 'AVAILABLE');
            clearHold();
            toast.warning('Hold expired', {
                description: 'Your seat hold has expired. Please select a new seat.'
            });
        }
    }, [heldSeat, updateSeatStatus, clearHold]);

    const handleCancelHold = useCallback(async () => {
        if (heldSeat) {
            try {
                await deleteSeatHold(heldSeat.seatId, heldSeat.sessionId);
                updateSeatStatus(heldSeat.seatId, 'AVAILABLE');
                clearHold();
                toast.info('Hold cancelled', {
                    description: 'Your seat hold has been released.'
                });
            } catch (error) {
                toast.error('Error', {
                    description: error instanceof Error ? error.message : 'Failed to release seat hold'
                });
            }
        }
    }, [heldSeat, updateSeatStatus, clearHold]);

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

            {isHolding && (
                <div className="mb-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                    <p className="text-yellow-800 flex items-center gap-2">
                        <span className="animate-spin rounded-full h-4 w-4 border-b-2 border-yellow-600"></span>
                        Holding seat...
                    </p>
                </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2">
                    {heldSeat && (
                        <div className="mb-4">
                            <HoldTimer
                                expiresAt={new Date(heldSeat.holdExpiresAt)}
                                onExpire={handleHoldExpire}
                            />
                        </div>
                    )}

                    {selectedSeats.length > 0 && (
                        <div className="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                            <p className="text-blue-800">
                                <span className="font-medium">{selectedSeats.length}</span> seat(s) selected
                            </p>
                        </div>
                    )}

                    <SeatMap onSeatClick={handleSeatClick} />
                </div>

                <div className="lg:col-span-1">
                    <CheckoutForm onCancel={handleCancelHold} />
                </div>
            </div>
        </div>
    );
}
