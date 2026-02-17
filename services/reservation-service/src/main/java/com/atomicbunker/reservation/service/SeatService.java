package com.atomicbunker.reservation.service;

import com.atomicbunker.reservation.domain.Seat;
import com.atomicbunker.reservation.domain.SeatRepository;
import com.atomicbunker.reservation.dto.SeatDTO;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SeatService {

    @Inject
    SeatRepository seatRepository;

    @WithSession
    public Uni<List<SeatDTO>> getSeatsByShowtime(UUID showtimeId) {
        return seatRepository.findByShowtimeId(showtimeId)
                .map(seats -> {
                    if (seats == null) {
                        return Collections.<SeatDTO>emptyList();
                    }
                    return seats.stream()
                            .map(SeatDTO::from)
                            .toList();
                });
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
