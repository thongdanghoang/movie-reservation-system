'use client';

import { useEffect, useRef, useState } from 'react';
import { useSeatStore } from '@/stores/seatStore';
import type { SeatUpdate } from '@/shared/types';

export function useSeatWebSocket(showtimeId: string | null) {
    const wsRef = useRef<WebSocket | null>(null);
    const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const [isConnected, setIsConnected] = useState(false);
    const updateSeatStatus = useSeatStore((state) => state.updateSeatStatus);

    useEffect(() => {
        if (!showtimeId) return;

        let isUnmounted = false;

        function connect() {
            if (isUnmounted || wsRef.current?.readyState === WebSocket.OPEN) {
                return;
            }

            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const host = (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080')
                .replace(/^https?:\/\//, '');
            const wsUrl = `${protocol}//${host}/ws/seats/${showtimeId}`;

            try {
                wsRef.current = new WebSocket(wsUrl);

                wsRef.current.onopen = () => {
                    console.log(`WebSocket connected for showtime ${showtimeId}`);
                    setIsConnected(true);
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
                    setIsConnected(false);
                    if (!isUnmounted) {
                        reconnectTimeoutRef.current = setTimeout(() => {
                            connect();
                        }, 3000);
                    }
                };

                wsRef.current.onerror = (error) => {
                    console.error('WebSocket error:', error);
                };
            } catch (error) {
                console.error('Failed to create WebSocket:', error);
            }
        }

        connect();

        return () => {
            isUnmounted = true;
            if (reconnectTimeoutRef.current) {
                clearTimeout(reconnectTimeoutRef.current);
            }
            if (wsRef.current) {
                wsRef.current.close();
                wsRef.current = null;
            }
        };
    }, [showtimeId, updateSeatStatus]);

    return {
        isConnected,
    };
}
