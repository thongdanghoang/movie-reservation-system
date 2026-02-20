import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';

import { CheckoutForm } from './CheckoutForm';
import { useSeatStore } from '@/stores/seatStore';
import * as api from '@/lib/api';

// Mock the API module
vi.mock('@/lib/api', () => ({
    processPayment: vi.fn(),
    ApiError: class ApiError extends Error {
        constructor(public status: number, message: string) {
            super(message);
            this.name = 'ApiError';
        }
    },
    SeatTakenError: class SeatTakenError extends Error {
        constructor(message: string) {
            super(message);
            this.name = 'SeatTakenError';
        }
    },
}));

// Mock sonner
vi.mock('sonner', () => ({
    toast: {
        error: vi.fn(),
        success: vi.fn(),
    },
}));

const mockHeldSeat = {
    seatId: '770e8400-e29b-41d4-a716-446655440001',
    status: 'HELD' as const,
    heldAt: new Date().toISOString(),
    holdExpiresAt: new Date(Date.now() + 5 * 60 * 1000).toISOString(),
    reservationId: 'res-1234-5678-abcd-efgh',
};

const mockPaymentResponse = {
    reservationId: 'res-1234-5678-abcd-efgh',
    status: 'SOLD' as const,
    seatId: '770e8400-e29b-41d4-a716-446655440001',
    confirmationNumber: 'CNF-ABCD1234',
};

function setupStoreWithHeldSeat() {
    useSeatStore.setState({
        heldSeat: mockHeldSeat,
        paymentStatus: 'idle',
        paymentError: null,
        confirmationNumber: null,
    });
}

describe('CheckoutForm', () => {
    beforeEach(() => {
        // Reset store to a clean state before each test
        useSeatStore.setState({
            seats: [],
            selectedSeats: [],
            heldSeat: null,
            isHolding: false,
            holdError: null,
            paymentStatus: 'idle',
            paymentError: null,
            confirmationNumber: null,
        });
        vi.clearAllMocks();
    });

    describe('when no seat is held', () => {
        it('renders placeholder message when no held seat', () => {
            render(<CheckoutForm />);
            expect(screen.getByText(/hold a seat to proceed/i)).toBeInTheDocument();
        });
    });

    describe('when a seat is held', () => {
        beforeEach(() => {
            setupStoreWithHeldSeat();
        });

        it('renders the checkout form with email and phone inputs', () => {
            render(<CheckoutForm />);
            expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
            expect(screen.getByLabelText(/phone number/i)).toBeInTheDocument();
        });

        it('displays the reservation ID in the form', () => {
            render(<CheckoutForm />);
            expect(screen.getByText(mockHeldSeat.reservationId)).toBeInTheDocument();
        });

        it('renders Pay Now button', () => {
            render(<CheckoutForm />);
            expect(screen.getByRole('button', { name: /pay now/i })).toBeInTheDocument();
        });

        it('renders Cancel button when onCancel prop is provided', () => {
            const onCancel = vi.fn();
            render(<CheckoutForm onCancel={onCancel} />);
            expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
        });

        it('calls onCancel when Cancel is clicked', () => {
            const onCancel = vi.fn();
            render(<CheckoutForm onCancel={onCancel} />);
            fireEvent.click(screen.getByRole('button', { name: /cancel/i }));
            expect(onCancel).toHaveBeenCalledTimes(1);
        });

        it('disables submit button and shows Processing... during submission', async () => {
            const { processPayment } = api;
            vi.mocked(processPayment).mockImplementation(
                () => new Promise((resolve) => setTimeout(() => resolve(mockPaymentResponse), 200))
            );

            render(<CheckoutForm />);

            fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'test@example.com' } });
            fireEvent.change(screen.getByLabelText(/phone number/i), { target: { value: '555-0100' } });

            const form = screen.getByLabelText(/email address/i).closest('form')!;
            await act(async () => { fireEvent.submit(form); });

            expect(screen.getByRole('button', { name: /processing/i })).toBeDisabled();
        });

        it('shows confirmation screen on successful payment', async () => {
            const { processPayment } = api;
            vi.mocked(processPayment).mockResolvedValue(mockPaymentResponse);

            render(<CheckoutForm />);

            fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'test@example.com' } });
            fireEvent.change(screen.getByLabelText(/phone number/i), { target: { value: '555-0100' } });
            fireEvent.click(screen.getByRole('button', { name: /pay now/i }));

            await waitFor(() => {
                expect(screen.getByText(/payment successful/i)).toBeInTheDocument();
                expect(screen.getByText('CNF-ABCD1234')).toBeInTheDocument();
                expect(screen.getByText(/test@example.com/i)).toBeInTheDocument();
            });
        });

        it('calls processPayment with correct arguments', async () => {
            const { processPayment } = api;
            vi.mocked(processPayment).mockResolvedValue(mockPaymentResponse);

            render(<CheckoutForm />);

            fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'guest@example.com' } });
            fireEvent.change(screen.getByLabelText(/phone number/i), { target: { value: '555-9999' } });
            fireEvent.click(screen.getByRole('button', { name: /pay now/i }));

            await waitFor(() => {
                expect(processPayment).toHaveBeenCalledWith(
                    mockHeldSeat.reservationId,
                    'guest@example.com',
                    '555-9999'
                );
            });
        });

        it('shows error toast when seat is taken (409)', async () => {
            const { processPayment, SeatTakenError } = api;
            vi.mocked(processPayment).mockRejectedValue(new SeatTakenError('Seat is no longer available'));

            const { toast } = await import('sonner');

            render(<CheckoutForm />);

            fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'test@example.com' } });
            fireEvent.change(screen.getByLabelText(/phone number/i), { target: { value: '555-0100' } });
            fireEvent.click(screen.getByRole('button', { name: /pay now/i }));

            await waitFor(() => {
                expect(toast.error).toHaveBeenCalledWith(
                    expect.stringContaining('no longer available')
                );
            });
        });

        it('shows error toast when hold is expired (410)', async () => {
            const { processPayment, ApiError } = api;
            vi.mocked(processPayment).mockRejectedValue(new ApiError(410, 'Hold expired'));

            const { toast } = await import('sonner');

            render(<CheckoutForm />);

            fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'test@example.com' } });
            fireEvent.change(screen.getByLabelText(/phone number/i), { target: { value: '555-0100' } });
            fireEvent.click(screen.getByRole('button', { name: /pay now/i }));

            await waitFor(() => {
                expect(toast.error).toHaveBeenCalledWith(
                    expect.stringContaining('expired')
                );
            });
        });

        it('shows generic error toast on unexpected failure', async () => {
            const { processPayment } = api;
            vi.mocked(processPayment).mockRejectedValue(new Error('Network error'));

            const { toast } = await import('sonner');

            render(<CheckoutForm />);

            fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'test@example.com' } });
            fireEvent.change(screen.getByLabelText(/phone number/i), { target: { value: '555-0100' } });
            fireEvent.click(screen.getByRole('button', { name: /pay now/i }));

            await waitFor(() => {
                expect(toast.error).toHaveBeenCalledWith(
                    expect.stringContaining('failed')
                );
            });
        });

        it('re-enables form after payment error', async () => {
            const { processPayment } = api;
            vi.mocked(processPayment).mockRejectedValue(new Error('Network error'));

            render(<CheckoutForm />);

            fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'test@example.com' } });
            fireEvent.change(screen.getByLabelText(/phone number/i), { target: { value: '555-0100' } });
            fireEvent.click(screen.getByRole('button', { name: /pay now/i }));

            await waitFor(() => {
                expect(screen.getByRole('button', { name: /pay now/i })).not.toBeDisabled();
            });
        });
    });
});
