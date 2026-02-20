package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SeatRepository implements PanacheRepositoryBase<Seat, UUID> {

    public Uni<List<Seat>> findByShowtimeId(UUID showtimeId) {
        return list("showtime.id", showtimeId);
    }

    public Uni<Long> countByShowtimeIdAndStatus(UUID showtimeId, SeatStatus status) {
        return count("showtime.id = ?1 and status = ?2", showtimeId, status);
    }

    /**
     * Find seat by ID with pessimistic write lock (FOR UPDATE NOWAIT).
     * Fails immediately if seat is already locked.
     */
    public Uni<Seat> findByIdWithLock(UUID seatId) {
        return find("id", seatId)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResult();
    }

    /**
     * Find available seat by ID. Returns empty if seat doesn't exist or is not
     * available.
     */
    public Uni<Optional<Seat>> findAvailableById(UUID seatId) {
        return findById(seatId)
                .map(seat -> {
                    if (seat == null || seat.getStatus() != SeatStatus.AVAILABLE) {
                        return Optional.empty();
                    }
                    return Optional.of(seat);
                });
    }

    /**
     * Find seat by reservationId with pessimistic write lock (FOR UPDATE NOWAIT).
     * Fails immediately (PessimisticLockException) if row is already locked,
     * preventing the Vert.x event-loop from blocking on competing transactions.
     *
     * A UNIQUE constraint on reservation_id (migration V1.0.4) ensures
     * getSingleResultOrNull() never throws NonUniqueResultException.
     */
    public Uni<Seat> findByReservationIdWithLock(UUID reservationId) {
        return getSession()
                .chain(session -> session.createQuery(
                        "FROM Seat WHERE reservationId = :rid", Seat.class)
                        .setParameter("rid", reservationId)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .getSingleResultOrNull());
    }
}
