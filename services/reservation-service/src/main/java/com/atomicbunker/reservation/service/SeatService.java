package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Seat;
import com.atomicbunker.reservation.domain.SeatRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @WithSession
    public Uni<List<Seat>> getSeatsByShowtime(UUID showtimeId) {
        return seatRepository.findByShowtimeId(showtimeId);
    }

    @WithSession
    public Uni<Seat> findById(UUID seatId) {
        return seatRepository.findById(seatId);
    }

    @WithTransaction
    public Uni<Seat> updateSeatStatus(Seat seat) {
        return seatRepository.persist(seat);
    }
}
