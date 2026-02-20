package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
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
     * Uses native SQL because Mutiny.SelectionQuery has no setHint() — NOWAIT
     * cannot be expressed via the HQL/Panache query API.
     * Fails immediately with PessimisticLockException if already locked.
     */
    public Uni<Seat> findByIdWithLock(UUID seatId) {
        return getSession()
                .chain(session -> session.createNativeQuery(
                        "SELECT * FROM seats WHERE id = :id FOR UPDATE NOWAIT",
                        Seat.class)
                        .setParameter("id", seatId)
                        .getSingleResultOrNull());
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
     * Uses native SQL because Mutiny.SelectionQuery has no setHint() — NOWAIT
     * cannot be expressed via the HQL query API in this version of Quarkus.
     * Fails immediately with PessimisticLockException if already locked,
     * preventing Vert.x event-loop blocking on competing transactions.
     *
     * A UNIQUE constraint on reservation_id (migration V1.0.4) ensures
     * getSingleResultOrNull() never throws NonUniqueResultException.
     */
    public Uni<Seat> findByReservationIdWithLock(UUID reservationId) {
        return getSession()
                .chain(session -> session.createNativeQuery(
                        "SELECT * FROM seats WHERE reservation_id = :rid FOR UPDATE NOWAIT",
                        Seat.class)
                        .setParameter("rid", reservationId)
                        .getSingleResultOrNull());
    }

    /**
     * Find seat by reservationId without locking.
     * Useful for read-only operations like fetching a ticket.
     */
    public Uni<Seat> findByReservationId(UUID reservationId) {
        return find("reservationId", reservationId).firstResult();
    }
}
