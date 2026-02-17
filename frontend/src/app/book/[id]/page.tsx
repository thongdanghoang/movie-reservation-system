interface BookingPageProps {
    params: Promise<{ id: string }>;
}

export default async function BookingPage({ params }: BookingPageProps) {
    const { id } = await params;

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-2xl font-bold mb-4">Booking Page</h1>
            <p className="text-gray-600">Showtime ID: {id}</p>
            <p className="text-gray-500 mt-4">
                Seat selection and booking functionality will be implemented in Story 1.3
            </p>
        </div>
    );
}
