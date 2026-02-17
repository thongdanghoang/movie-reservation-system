'use client';

import { useEffect } from 'react';

interface ErrorProps {
    error: Error & { digest?: string };
    reset: () => void;
}

export default function MovieErrorPage({ error, reset }: ErrorProps) {
    useEffect(() => {
        console.error('Movie page error:', error);
    }, [error]);

    return (
        <div className="container mx-auto px-4 py-16 text-center">
            <div className="max-w-md mx-auto">
                <h1 className="text-4xl font-bold text-gray-900 mb-4">Something Went Wrong</h1>
                <p className="text-gray-600 mb-8">
                    An error occurred while loading the movie. Please try again.
                </p>
                <button
                    onClick={reset}
                    className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                    Try Again
                </button>
            </div>
        </div>
    );
}
