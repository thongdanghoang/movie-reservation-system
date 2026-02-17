import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ShowtimeList } from './ShowtimeList';

const mockRouter = {
    push: vi.fn(),
};

vi.mock('next/navigation', () => ({
    useRouter: () => mockRouter,
}));

describe('ShowtimeList', () => {
    beforeEach(() => {
        mockRouter.push.mockClear();
    });

    it('renders empty state when no showtimes', () => {
        render(<ShowtimeList showtimes={[]} />);
        expect(screen.getByText('No showtimes available for today')).toBeTruthy();
    });

    it('renders showtimes list', () => {
        const futureDate = new Date();
        futureDate.setHours(futureDate.getHours() + 2);

        const showtimes = [
            {
                id: '1',
                movieId: 'movie-1',
                startTime: futureDate.toISOString(),
                theaterName: 'Theater 1',
                availableSeats: 50,
            },
        ];

        render(<ShowtimeList showtimes={showtimes} />);
        expect(screen.getByText('Theater 1')).toBeTruthy();
        expect(screen.getByText('50 seats available')).toBeTruthy();
    });

    it('disables past showtimes', () => {
        const pastDate = new Date();
        pastDate.setHours(pastDate.getHours() - 2);

        const showtimes = [
            {
                id: '1',
                movieId: 'movie-1',
                startTime: pastDate.toISOString(),
                theaterName: 'Past Theater',
                availableSeats: 50,
            },
        ];

        render(<ShowtimeList showtimes={showtimes} />);
        const button = screen.getByRole('button', { name: /Past Theater/ });
        expect(button.hasAttribute('disabled')).toBe(true);
    });

    it('navigates to booking page on click', () => {
        const futureDate = new Date();
        futureDate.setHours(futureDate.getHours() + 2);

        const showtimes = [
            {
                id: 'test-showtime-id',
                movieId: 'movie-1',
                startTime: futureDate.toISOString(),
                theaterName: 'Future Theater',
                availableSeats: 50,
            },
        ];

        render(<ShowtimeList showtimes={showtimes} />);
        const button = screen.getByRole('button', { name: /Future Theater/ });
        button.click();
        expect(mockRouter.push).toHaveBeenCalledWith('/book/test-showtime-id');
    });
});
