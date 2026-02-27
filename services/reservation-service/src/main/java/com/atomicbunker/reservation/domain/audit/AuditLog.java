package com.atomicbunker.reservation.domain.audit;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.Instant;
import java.util.List;

import io.smallrye.mutiny.Uni;

@MongoEntity(collection = "audit_logs")
@Getter
@Setter
public class AuditLog extends ReactivePanacheMongoEntity {

    @BsonProperty("reservation_id")
    private String reservationId;

    @BsonProperty("seat_id")
    private String seatId;

    @BsonProperty("event_type")
    private String eventType;

    private Instant timestamp;

    private String metadata;

    @BsonProperty("showtime_id")
    private String showtimeId;

    public static Uni<List<AuditLog>> findByReservationId(String reservationId) {
        return list("reservationId", reservationId);
    }
}