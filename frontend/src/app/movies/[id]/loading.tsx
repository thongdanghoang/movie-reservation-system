export default function Loading() {
    return (
        <div className="container mx-auto px-4 py-8 max-w-6xl">
            <div className="flex flex-col md:flex-row gap-8 mb-12">
                {/* Poster Skeleton */}
                <div className="aspect-[2/3] w-full md:w-80 bg-gray-200 rounded-lg animate-pulse" />

                {/* Info Skeleton */}
                <div className="flex-1 space-y-4">
                    <div className="h-10 bg-gray-200 rounded w-3/4 animate-pulse" />
                    <div className="h-6 bg-gray-200 rounded w-1/4 animate-pulse" />
                    <div className="h-8 bg-gray-200 rounded w-32 animate-pulse" />
                </div>
            </div>

            {/* Showtimes Skeleton */}
            <div className="space-y-4">
                <div className="h-8 bg-gray-200 rounded w-48 animate-pulse" />
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                    {[...Array(6)].map((_, i) => (
                        <div key={i} className="h-24 bg-gray-200 rounded-lg animate-pulse" />
                    ))}
                </div>
            </div>
        </div>
    );
}
