'use client';

import { useEffect, useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { getTicket, ApiError } from '@/lib/api';
import { TicketResponse } from '@/shared/types';

interface BookingConfirmationProps {
    reservationId: string;
}

export function BookingConfirmation({ reservationId }: BookingConfirmationProps) {
    const [ticket, setTicket] = useState<TicketResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        let mounted = true;

        async function fetchTicket() {
            try {
                // To display the ticket immediately after booking, we might need a small delay 
                // in case of any database replication lag, or we can fetch immediately since 
                // we're hitting the same service.
                const data = await getTicket(reservationId);
                if (mounted) {
                    setTicket(data);
                    setIsLoading(false);
                }
            } catch (err) {
                if (mounted) {
                    if (err instanceof ApiError) {
                        setError(err.message);
                    } else {
                        setError('Failed to load ticket information');
                    }
                    setIsLoading(false);
                }
            }
        }

        fetchTicket();

        return () => {
            mounted = false;
        };
    }, [reservationId]);

    if (isLoading) {
        return (
            <div className="bg-white border border-gray-200 shadow-sm rounded-lg p-6 flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    if (error || !ticket) {
        return (
            <div className="bg-white border border-red-200 shadow-sm rounded-lg p-6">
                <div className="text-center">
                    <div className="text-red-500 text-5xl mb-4">!</div>
                    <h3 className="text-xl font-bold text-red-700 mb-2">Could Not Load Ticket</h3>
                    <p className="text-gray-600 mb-4">{error || 'Unknown error occurred.'}</p>
                </div>
            </div>
        );
    }

    // This URL could be the actual verification link a scanner would use
    const ticketVerificationUrl = `${window.location.origin}/verify?token=${ticket.signedTicketToken}`;

    return (
        <div className="bg-white border border-green-200 shadow-sm rounded-lg p-6">
            <div className="text-center">
                <div className="text-green-500 text-5xl mb-4">✓</div>
                <h3 className="text-xl font-bold text-green-700 mb-2">Payment Successful!</h3>
                <p className="text-gray-600 mb-6">Your booking is confirmed. Here is your digital ticket.</p>

                <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 mb-6 inline-block text-left w-full max-w-sm">
                    <div className="flex justify-between border-b pb-4 mb-4">
                        <div>
                            <p className="text-xs text-gray-500 uppercase font-semibold">Reservation ID</p>
                            <p className="font-mono text-sm">{ticket.reservationId.split('-')[0]}</p>
                        </div>
                        <div className="text-right">
                            <p className="text-xs text-gray-500 uppercase font-semibold">Status</p>
                            <p className="font-bold text-green-600">{ticket.status}</p>
                        </div>
                    </div>

                    <div className="flex justify-center bg-white p-4 rounded mb-4">
                        <QRCodeSVG value={ticketVerificationUrl} size={200} />
                    </div>

                    <div className="text-center text-sm text-gray-500">
                        <p>Scan for entry</p>
                    </div>
                </div>

                <p className="text-sm text-gray-500">
                    A confirmation email with this ticket has been sent to <strong>{ticket.email}</strong>.
                </p>
            </div>
        </div>
    );
}
