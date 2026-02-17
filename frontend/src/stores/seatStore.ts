import { create } from 'zustand';
import type { Seat, SeatStatus } from '@/shared/types';

interface SeatStore {
    seats: Seat[];
    selectedSeats: string[];
    setSeats: (seats: Seat[]) => void;
    updateSeatStatus: (seatId: string, status: SeatStatus) => void;
    toggleSeatSelection: (seatId: string) => void;
    clearSelection: () => void;
}

export const useSeatStore = create<SeatStore>((set) => ({
    seats: [],
    selectedSeats: [],

    setSeats: (seats) => set({ seats }),

    updateSeatStatus: (seatId, status) =>
        set((state) => ({
            seats: state.seats.map((seat) =>
                seat.id === seatId ? { ...seat, status } : seat
            ),
        })),

    toggleSeatSelection: (seatId) =>
        set((state) => {
            const isSelected = state.selectedSeats.includes(seatId);
            return {
                selectedSeats: isSelected
                    ? state.selectedSeats.filter((id) => id !== seatId)
                    : [...state.selectedSeats, seatId],
            };
        }),

    clearSelection: () => set({ selectedSeats: [] }),
}));
