'use client';

import { useEffect, useRef, useCallback } from 'react';
import { useSeatStore } from '@/stores/seatStore';
import type { SeatUpdate } from '@/shared/types';

export function useSeatWebSocket(showtimeId: string | null) {
    const wsRef = useRef<WebSocket | null>(null);
    const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const updateSeatStatus = useSeatStore((state) => state.updateSeatStatus);

    const connect = useCallback(() => {
        if (!showtimeId || wsRef.current?.readyState === WebSocket.OPEN) {
            return;
        }

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = process.env.NEXT_PUBLIC_API_URL || 'localhost:8080';
        const wsUrl = `${protocol}//${host}/ws/seats/${showtimeId}`;

        try {
            wsRef.current = new WebSocket(wsUrl);

            wsRef.current.onopen = () => {
                console.log(`WebSocket connected for showtime ${showtimeId}`);
            };

            wsRef.current.onmessage = (event) => {
                try {
                    const update: SeatUpdate = JSON.parse(event.data);
                    updateSeatStatus(update.seatId, update.status);
                } catch (error) {
                    console.error('Failed to parse WebSocket message:', error);
                }
            };

            wsRef.current.onclose = () => {
                console.log(`WebSocket disconnected for showtime ${showtimeId}`);
                reconnectTimeoutRef.current = setTimeout(() => {
                    connect();
                }, 3000);
            };

            wsRef.current.onerror = (error) => {
                console.error('WebSocket error:', error);
            };
        } catch (error) {
            console.error('Failed to create WebSocket:', error);
        }
    }, [showtimeId, updateSeatStatus]);

    const disconnect = useCallback(() => {
        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
        }
        if (wsRef.current) {
            wsRef.current.close();
            wsRef.current = null;
        }
    }, []);

    useEffect(() => {
        connect();
        return () => disconnect();
    }, [connect, disconnect]);

    return {
        isConnected: wsRef.current?.readyState === WebSocket.OPEN,
    };
}
