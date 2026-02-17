import Link from 'next/link';

export default function NotFound() {
    return (
        <div className="container mx-auto px-4 py-16 text-center">
            <div className="max-w-md mx-auto">
                <h1 className="text-4xl font-bold text-gray-900 mb-4">Movie Not Found</h1>
                <p className="text-gray-600 mb-8">
                    The movie you are looking for does not exist or is no longer available.
                </p>
                <Link
                    href="/"
                    className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors inline-block"
                >
                    Go to Home
                </Link>
            </div>
        </div>
    );
}
