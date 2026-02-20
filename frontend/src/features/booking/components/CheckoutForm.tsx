import { useState, useEffect } from 'react';
import { useSeatStore } from '@/stores/seatStore';

interface CheckoutFormProps {
    onCancel?: () => void;
}

export function CheckoutForm({ onCancel }: CheckoutFormProps) {
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const heldSeat = useSeatStore((state) => state.heldSeat);

    useEffect(() => {
        if (!isSubmitting) return;

        // Simulate submission (will be implemented in Story 2.3)
        const timerId = setTimeout(() => {
            setIsSubmitting(false);
            console.log('Submitted booking for reservation:', heldSeat?.reservationId, '[REDACTED]', '[REDACTED]');
            // TODO(Story 2.3): transition to payment processing
        }, 500);

        return () => clearTimeout(timerId);
    }, [isSubmitting, heldSeat?.reservationId]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (!heldSeat) return;

        // Basic frontend validation is handled by HTML5 attributes
        // but we can add more specific rules here if needed
        setIsSubmitting(true);
    };

    if (!heldSeat) {
        return (
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 opacity-60">
                <h3 className="text-lg font-semibold mb-2">Guest Checkout</h3>
                <p className="text-gray-500">Hold a seat to proceed with checkout.</p>
            </div>
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
                        disabled={isSubmitting || !heldSeat}
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
                        disabled={isSubmitting || !heldSeat}
                    />
                </div>

                <div className="pt-4 flex gap-3">
                    <button
                        type="submit"
                        disabled={isSubmitting || !heldSeat}
                        className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
                    >
                        {isSubmitting ? 'Processing...' : 'Continue to Payment'}
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
