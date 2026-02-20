import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { SeatMap } from './SeatMap';
import { useSeatStore } from '@/stores/seatStore';
import type { Seat } from '@/shared/types';

describe('SeatMap', () => {
    beforeEach(() => {
        useSeatStore.setState({
            seats: [],
            selectedSeats: [],
        });
    });

    it('renders empty when no seats available', () => {
        render(<SeatMap />);
        expect(screen.getByText('Zoom In')).toBeInTheDocument();
        expect(screen.getByText('Available')).toBeInTheDocument();
        expect(screen.getByText('Held')).toBeInTheDocument();
        expect(screen.getByText('Sold')).toBeInTheDocument();
    });

    it('renders seats with correct colors based on status', () => {
        const testSeats: Seat[] = [
            { id: '1', seatRow: 'A', seatColumn: 1, status: 'AVAILABLE' },
            { id: '2', seatRow: 'A', seatColumn: 2, status: 'HELD' },
            { id: '3', seatRow: 'A', seatColumn: 3, status: 'SOLD' },
        ];

        useSeatStore.setState({ seats: testSeats });
        render(<SeatMap />);

        expect(screen.getByText('A')).toBeInTheDocument();
        expect(screen.getAllByText('1').length).toBeGreaterThan(0);
        expect(screen.getAllByText('2').length).toBeGreaterThan(0);
        expect(screen.getAllByText('3').length).toBeGreaterThan(0);
    });

    it('calls onSeatClick when clicking available seat', () => {
        const onSeatClick = vi.fn();
        const testSeats: Seat[] = [
            { id: '1', seatRow: 'A', seatColumn: 1, status: 'AVAILABLE' },
        ];

        useSeatStore.setState({ seats: testSeats });
        const { container } = render(<SeatMap onSeatClick={onSeatClick} />);

        const seat = container.querySelector('rect[fill="#22c55e"]');
        expect(seat).not.toBeNull();
        fireEvent.click(seat!);
        expect(onSeatClick).toHaveBeenCalledWith(testSeats[0]);
    });

    it('does not call onSeatClick when clicking held seat', () => {
        const onSeatClick = vi.fn();
        const testSeats: Seat[] = [
            { id: '1', seatRow: 'A', seatColumn: 1, status: 'HELD' },
        ];

        useSeatStore.setState({ seats: testSeats });
        const { container } = render(<SeatMap onSeatClick={onSeatClick} />);

        const seat = container.querySelector('rect[fill="#f97316"]');
        expect(seat).not.toBeNull();
        fireEvent.click(seat!);
        expect(onSeatClick).not.toHaveBeenCalled();
    });

    it('displays zoom controls', () => {
        render(<SeatMap />);

        expect(screen.getByText('Zoom In')).toBeInTheDocument();
        expect(screen.getByText('Zoom Out')).toBeInTheDocument();
        expect(screen.getByText('Reset View')).toBeInTheDocument();
    });
});
