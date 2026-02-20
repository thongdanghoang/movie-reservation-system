import { create } from 'zustand';
import type { Seat, SeatStatus, HoldSeatResponse } from '@/shared/types';

interface SeatStore {
    seats: Seat[];
    selectedSeats: string[];
    heldSeat: HoldSeatResponse | null;
    isHolding: boolean;
    holdError: string | null;
    paymentStatus: 'idle' | 'processing' | 'success' | 'error';
    paymentError: string | null;
    confirmationNumber: string | null;
    setSeats: (seats: Seat[]) => void;
    updateSeatStatus: (seatId: string, status: SeatStatus) => void;
    toggleSeatSelection: (seatId: string) => void;
    clearSelection: () => void;
    setHeldSeat: (seat: HoldSeatResponse | null) => void;
    setIsHolding: (isHolding: boolean) => void;
    setHoldError: (error: string | null) => void;
    clearHold: () => void;
    setPaymentStatus: (status: 'idle' | 'processing' | 'success' | 'error') => void;
    setPaymentError: (error: string | null) => void;
    setConfirmationNumber: (number: string | null) => void;
}

export const useSeatStore = create<SeatStore>((set) => ({
    seats: [],
    selectedSeats: [],
    heldSeat: null,
    isHolding: false,
    holdError: null,
    paymentStatus: 'idle',
    paymentError: null,
    confirmationNumber: null,

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

    setHeldSeat: (heldSeat) => set({ heldSeat }),

    setIsHolding: (isHolding) => set({ isHolding }),

    setHoldError: (holdError) => set({ holdError }),

    clearHold: () => set({
        heldSeat: null,
        holdError: null,
        isHolding: false,
        paymentStatus: 'idle',
        paymentError: null,
        confirmationNumber: null,
    }),

    setPaymentStatus: (paymentStatus) => set({ paymentStatus }),
    setPaymentError: (paymentError) => set({ paymentError }),
    setConfirmationNumber: (confirmationNumber) => set({ confirmationNumber }),
}));
