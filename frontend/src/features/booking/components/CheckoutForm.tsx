'use client';

import { useState } from 'react';
import { toast } from 'sonner';
import { useSeatStore } from '@/stores/seatStore';
import { processPayment, SeatTakenError, ApiError } from '@/lib/api';
import { BookingConfirmation } from './BookingConfirmation';

interface CheckoutFormProps {
    onCancel?: () => void;
}

export function CheckoutForm({ onCancel }: CheckoutFormProps) {
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);

    const heldSeat = useSeatStore((state) => state.heldSeat);
    const updateSeatStatus = useSeatStore((state) => state.updateSeatStatus);
    const setPaymentStatus = useSeatStore((state) => state.setPaymentStatus);
    const setConfirmationInStore = useSeatStore((state) => state.setConfirmationNumber);
    const clearHold = useSeatStore((state) => state.clearHold);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!heldSeat) return;

        setIsSubmitting(true);
        setPaymentStatus('processing');

        try {
            const result = await processPayment(
                heldSeat.reservationId,
                email,
                phone,
                heldSeat.sessionId  // forward session ownership proof
            );

            // Transition seat to SOLD in the UI
            updateSeatStatus(heldSeat.seatId, 'SOLD');
            setConfirmationInStore(result.confirmationNumber);
            setPaymentStatus('success');
            setIsSuccess(true);
        } catch (err) {
            setPaymentStatus('error');

            if (err instanceof SeatTakenError) {
                // 409: seat taken — terminal error, user must pick a new seat
                clearHold();
                toast.error('Seat is no longer available. Please select another seat.');
            } else if (err instanceof ApiError && err.status === 410) {
                // 410: hold expired — terminal error, user must pick a new seat
                clearHold();
                toast.error('Your hold has expired. Please select a new seat.');
            } else {
                // Network / server error — form can be retried, keep hold
                toast.error('Payment failed. Please try again.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!heldSeat) {
        return (
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 opacity-60">
                <h3 className="text-lg font-semibold mb-2">Guest Checkout</h3>
                <p className="text-gray-500">Hold a seat to proceed with checkout.</p>
            </div>
        );
    }

    if (isSuccess && heldSeat) {
        return (
            <BookingConfirmation
                reservationId={heldSeat.reservationId}
                sessionId={heldSeat.sessionId}
            />
        );
    }

    return (
        <form onSubmit={handleSubmit} className="bg-white border border-gray-200 shadow-sm rounded-lg p-6">
            <h3 className="text-xl font-bold mb-4">Guest Checkout</h3>

            <div className="mb-4">
                <div className="bg-blue-50 text-blue-800 p-3 rounded-md text-sm mb-4">
                    <p><strong>Reservation ID:</strong> {heldSeat.reservationId}</p>
                    <p><strong>Seat:</strong> {heldSeat.seatId}</p>
                </div>
            </div>

            <div className="space-y-4">
                <div>
                    <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                        Email Address <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="email"
                        id="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                        placeholder="you@example.com"
                        disabled={isSubmitting}
                    />
                </div>

                <div>
                    <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">
                        Phone Number <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="tel"
                        id="phone"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        required
                        pattern="[0-9+() \-]+"
                        className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                        placeholder="(555) 123-4567"
                        disabled={isSubmitting}
                    />
                </div>

                <div className="pt-4 flex gap-3">
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
                    >
                        {isSubmitting ? 'Processing...' : 'Pay Now'}
                    </button>
                    {onCancel && (
                        <button
                            type="button"
                            onClick={onCancel}
                            disabled={isSubmitting}
                            className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
                        >
                            Cancel
                        </button>
                    )}
                </div>
            </div>
        </form>
    );
}
