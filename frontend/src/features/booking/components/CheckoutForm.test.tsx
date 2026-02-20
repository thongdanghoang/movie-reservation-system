import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { CheckoutForm } from './CheckoutForm';
import { useSeatStore } from '@/stores/seatStore';

describe('CheckoutForm', () => {
    beforeEach(() => {
        useSeatStore.setState({ heldSeat: null });
    });

    it('renders disabled state when no seat is held (AC3 partial)', () => {
        render(<CheckoutForm />);
        expect(screen.getByText('Hold a seat to proceed with checkout.')).toBeInTheDocument();
        expect(screen.queryByLabelText(/Email Address/i)).not.toBeInTheDocument();
    });

    it('renders email and phone inputs when seat is held (AC1)', () => {
        useSeatStore.setState({
            heldSeat: {
                reservationId: 'res-123',
                seatId: 'A-1',
                status: 'HELD',
                holdExpiresAt: new Date(Date.now() + 300000).toISOString(),
            },
        });

        render(<CheckoutForm />);
        expect(screen.getByText(/res-123/)).toBeInTheDocument();
        expect(screen.getByText(/A-1/)).toBeInTheDocument();

        const emailInput = screen.getByLabelText(/Email Address/i) as HTMLInputElement;
        const phoneInput = screen.getByLabelText(/Phone Number/i) as HTMLInputElement;

        expect(emailInput).toBeInTheDocument();
        expect(emailInput.required).toBe(true);
        expect(phoneInput).toBeInTheDocument();
        expect(phoneInput.required).toBe(true);
    });

    it('prevents submission if fields are invalid or empty (AC2)', () => {
        useSeatStore.setState({
            heldSeat: {
                reservationId: 'res-123',
                seatId: 'A-1',
                status: 'HELD',
                holdExpiresAt: new Date(Date.now() + 300000).toISOString(),
            },
        });

        render(<CheckoutForm />);
        const submitButton = screen.getByRole('button', { name: /Continue to Payment/i });
        expect(submitButton).toBeInTheDocument();

        // Form is invalid initially because fields are required and empty
        const emailInput = screen.getByLabelText(/Email Address/i) as HTMLInputElement;
        const phoneInput = screen.getByLabelText(/Phone Number/i) as HTMLInputElement;

        expect(emailInput.required).toBe(true);
        expect(phoneInput.required).toBe(true);
    });

    it('allows submission when fields are valid', () => {
        useSeatStore.setState({
            heldSeat: {
                reservationId: 'res-123',
                seatId: 'A-1',
                status: 'HELD',
                holdExpiresAt: new Date(Date.now() + 300000).toISOString(),
            },
        });

        render(<CheckoutForm />);

        const emailInput = screen.getByLabelText(/Email Address/i) as HTMLInputElement;
        fireEvent.change(emailInput, { target: { value: 'test@example.com' } });

        const phoneInput = screen.getByLabelText(/Phone Number/i) as HTMLInputElement;
        fireEvent.change(phoneInput, { target: { value: '555-1234' } });

        expect(emailInput.value).toBe('test@example.com');
        expect(phoneInput.value).toBe('555-1234');
    });

    it('calls onCancel when cancel button is clicked', () => {
        useSeatStore.setState({
            heldSeat: {
                reservationId: 'res-123',
                seatId: 'A-1',
                status: 'HELD',
                holdExpiresAt: new Date(Date.now() + 300000).toISOString(),
            },
        });

        const onCancel = vi.fn();
        render(<CheckoutForm onCancel={onCancel} />);

        const cancelButton = screen.getByRole('button', { name: /Cancel/i });
        fireEvent.click(cancelButton);
        expect(onCancel).toHaveBeenCalledTimes(1);
    });
});
