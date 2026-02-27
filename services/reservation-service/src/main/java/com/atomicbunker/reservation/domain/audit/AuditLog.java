package com.atomicbunker.reservation.domain.audit;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.Instant;

@MongoEntity(collection = "audit_logs")
public class AuditLog extends PanacheMongoEntity {

    @BsonProperty("reservation_id")
    public String reservationId;

    @BsonProperty("seat_id")
    public String seatId;

    @BsonProperty("event_type")
    public String eventType;

    public Instant timestamp;

    public String metadata;

    @BsonProperty("showtime_id")
    public String showtimeId;

    public static java.util.List<AuditLog> findByReservationId(String reservationId) {
        return list("reservationId", reservationId);
    }
}