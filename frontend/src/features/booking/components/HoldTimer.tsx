'use client';

import { useState, useEffect, useCallback } from 'react';

interface HoldTimerProps {
    expiresAt: Date;
    onExpire: () => void;
}

interface TimeRemaining {
    minutes: number;
    seconds: number;
    totalSeconds: number;
}

function calculateRemaining(expiresAt: Date): TimeRemaining {
    const now = new Date();
    const diff = expiresAt.getTime() - now.getTime();
    const totalSeconds = Math.max(0, Math.floor(diff / 1000));
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    
    return { minutes, seconds, totalSeconds };
}

export function HoldTimer({ expiresAt, onExpire }: HoldTimerProps) {
    const [timeRemaining, setTimeRemaining] = useState<TimeRemaining>(() => 
        calculateRemaining(expiresAt)
    );
    const [isWarning, setIsWarning] = useState(false);

    const handleExpire = useCallback(() => {
        onExpire();
    }, [onExpire]);

    useEffect(() => {
        const interval = setInterval(() => {
            const remaining = calculateRemaining(expiresAt);
            setTimeRemaining(remaining);
            setIsWarning(remaining.minutes === 0 && remaining.seconds < 60);

            if (remaining.totalSeconds <= 0) {
                handleExpire();
                clearInterval(interval);
            }
        }, 1000);

        return () => clearInterval(interval);
    }, [expiresAt, handleExpire]);

    return (
        <div 
            className={`p-4 rounded-lg border ${
                isWarning 
                    ? 'bg-red-50 border-red-300' 
                    : 'bg-blue-50 border-blue-200'
            }`}
        >
            <div className="flex items-center justify-between">
                <span className="font-medium text-gray-700">Hold expires in:</span>
                <span 
                    className={`text-2xl font-mono font-bold ${
                        isWarning ? 'text-red-600' : 'text-blue-600'
                    }`}
                >
                    {String(timeRemaining.minutes).padStart(2, '0')}:
                    {String(timeRemaining.seconds).padStart(2, '0')}
                </span>
            </div>
            {isWarning && (
                <p className="text-red-600 text-sm mt-2">
                    Hurry! Complete your booking soon or you&apos;ll lose this seat.
                </p>
            )}
        </div>
    );
}
