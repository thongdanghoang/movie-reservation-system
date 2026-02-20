'use client';

import { useState, useRef, useCallback, useEffect } from 'react';
import { useSeatStore } from '@/stores/seatStore';
import type { Seat, SeatStatus } from '@/shared/types';

const SEAT_SIZE = 30;
const SEAT_GAP = 8;
const STATUS_COLORS: Record<SeatStatus, string> = {
    AVAILABLE: '#22c55e',
    HELD: '#f97316',
    SOLD: '#ef4444',
};

interface SeatMapProps {
    onSeatClick?: (seat: Seat) => void;
}

export function SeatMap({ onSeatClick }: SeatMapProps) {
    const { seats, selectedSeats } = useSeatStore();
    const svgRef = useRef<SVGSVGElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);
    const [containerSize, setContainerSize] = useState({ width: 800, height: 600 });
    const [viewBox, setViewBox] = useState({ x: 0, y: 0, width: 800, height: 600 });
    const [isPanning, setIsPanning] = useState(false);
    const [startPoint, setStartPoint] = useState({ x: 0, y: 0 });

    const rows = [...new Set(seats.map((s) => s.seatRow))].sort();
    const columns = [...new Set(seats.map((s) => s.seatColumn))].sort((a, b) => a - b);

    const svgWidth = columns.length * (SEAT_SIZE + SEAT_GAP) + 100;
    const svgHeight = rows.length * (SEAT_SIZE + SEAT_GAP) + 100;

    useEffect(() => {
        if (containerRef.current) {
            const updateSize = () => {
                setContainerSize({
                    width: containerRef.current?.clientWidth || 800,
                    height: containerRef.current?.clientHeight || 600,
                });
            };
            updateSize();
            const resizeObserver = new ResizeObserver(updateSize);
            resizeObserver.observe(containerRef.current);
            return () => resizeObserver.disconnect();
        }
    }, []);

    const getSeatPosition = (row: string, column: number) => {
        const rowIndex = rows.indexOf(row);
        const colIndex = columns.indexOf(column);
        return {
            x: colIndex * (SEAT_SIZE + SEAT_GAP) + 50,
            y: rowIndex * (SEAT_SIZE + SEAT_GAP) + 50,
        };
    };

    const handleZoom = useCallback((direction: 'in' | 'out') => {
        const factor = direction === 'in' ? 0.8 : 1.25;
        setViewBox((prev) => ({
            ...prev,
            width: prev.width * factor,
            height: prev.height * factor,
        }));
    }, []);

    const handleMouseDown = useCallback((e: React.MouseEvent) => {
        if (e.button === 0) {
            setIsPanning(true);
            setStartPoint({ x: e.clientX, y: e.clientY });
        }
    }, []);

    const handleMouseMove = useCallback(
        (e: React.MouseEvent) => {
            if (isPanning) {
                const dx = (e.clientX - startPoint.x) * (viewBox.width / containerSize.width);
                const dy = (e.clientY - startPoint.y) * (viewBox.height / containerSize.height);
                setViewBox((prev) => ({
                    ...prev,
                    x: prev.x - dx,
                    y: prev.y - dy,
                }));
                setStartPoint({ x: e.clientX, y: e.clientY });
            }
        },
        [isPanning, startPoint, viewBox.width, viewBox.height, containerSize]
    );

    const handleMouseUp = useCallback(() => {
        setIsPanning(false);
    }, []);

    const handleSeatClick = useCallback(
        (seat: Seat) => {
            if (seat.status === 'AVAILABLE' && onSeatClick) {
                onSeatClick(seat);
            }
        },
        [onSeatClick]
    );

    const handleResetView = useCallback(() => {
        setViewBox({ x: 0, y: 0, width: svgWidth, height: svgHeight });
    }, [svgWidth, svgHeight]);

    return (
        <div className="flex flex-col gap-4">
            <div className="flex gap-2">
                <button
                    onClick={() => handleZoom('in')}
                    className="px-3 py-1 bg-gray-200 rounded hover:bg-gray-300 text-sm font-medium"
                >
                    Zoom In
                </button>
                <button
                    onClick={() => handleZoom('out')}
                    className="px-3 py-1 bg-gray-200 rounded hover:bg-gray-300 text-sm font-medium"
                >
                    Zoom Out
                </button>
                <button
                    onClick={handleResetView}
                    className="px-3 py-1 bg-gray-200 rounded hover:bg-gray-300 text-sm font-medium"
                >
                    Reset View
                </button>
            </div>

            <div ref={containerRef} className="border rounded-lg overflow-hidden bg-gray-50">
                <svg
                    ref={svgRef}
                    viewBox={`${viewBox.x} ${viewBox.y} ${viewBox.width} ${viewBox.height}`}
                    width="100%"
                    height="100%"
                    className="cursor-grab active:cursor-grabbing"
                    style={{ minHeight: '400px' }}
                    onMouseDown={handleMouseDown}
                    onMouseMove={handleMouseMove}
                    onMouseUp={handleMouseUp}
                    onMouseLeave={handleMouseUp}
                >
                    <rect x="0" y="0" width={svgWidth} height={svgHeight} fill="#f9fafb" />

                    {rows.map((row) => (
                        <text
                            key={`row-label-${row}`}
                            x="20"
                            y={rows.indexOf(row) * (SEAT_SIZE + SEAT_GAP) + 50 + SEAT_SIZE / 2}
                            textAnchor="middle"
                            dominantBaseline="middle"
                            className="fill-gray-600 text-sm font-medium"
                        >
                            {row}
                        </text>
                    ))}

                    {columns.map((col) => (
                        <text
                            key={`col-label-${col}`}
                            x={columns.indexOf(col) * (SEAT_SIZE + SEAT_GAP) + 50 + SEAT_SIZE / 2}
                            y="30"
                            textAnchor="middle"
                            dominantBaseline="middle"
                            className="fill-gray-600 text-sm font-medium"
                        >
                            {col}
                        </text>
                    ))}

                    {seats.map((seat) => {
                        const pos = getSeatPosition(seat.seatRow, seat.seatColumn);
                        const isSelected = selectedSeats.includes(seat.id);
                        const isAvailable = seat.status === 'AVAILABLE';

                        return (
                            <g key={seat.id}>
                                <rect
                                    x={pos.x}
                                    y={pos.y}
                                    width={SEAT_SIZE}
                                    height={SEAT_SIZE}
                                    rx="4"
                                    fill={STATUS_COLORS[seat.status]}
                                    stroke={isSelected ? '#1d4ed8' : 'transparent'}
                                    strokeWidth={isSelected ? 3 : 0}
                                    className={`transition-all duration-150 ${isAvailable ? 'cursor-pointer hover:fill-[#16a34a]' : 'cursor-not-allowed'
                                        }`}
                                    onClick={() => handleSeatClick(seat)}
                                />
                                <text
                                    x={pos.x + SEAT_SIZE / 2}
                                    y={pos.y + SEAT_SIZE / 2}
                                    textAnchor="middle"
                                    dominantBaseline="middle"
                                    className="fill-white text-xs font-bold pointer-events-none"
                                >
                                    {seat.seatColumn}
                                </text>
                            </g>
                        );
                    })}

                    <rect
                        x={svgWidth / 2 - 80}
                        y={svgHeight - 30}
                        width="160"
                        height="20"
                        rx="4"
                        fill="#374151"
                    />
                    <text
                        x={svgWidth / 2}
                        y={svgHeight - 20}
                        textAnchor="middle"
                        dominantBaseline="middle"
                        className="fill-white text-xs font-medium"
                    >
                        SCREEN
                    </text>
                </svg>
            </div>

            <div className="flex gap-4 text-sm">
                <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded" style={{ backgroundColor: STATUS_COLORS.AVAILABLE }} />
                    <span>Available</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded" style={{ backgroundColor: STATUS_COLORS.HELD }} />
                    <span>Held</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded" style={{ backgroundColor: STATUS_COLORS.SOLD }} />
                    <span>Sold</span>
                </div>
            </div>
        </div>
    );
}
