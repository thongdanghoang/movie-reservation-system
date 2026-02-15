package com.atomicbunker.reservation.domain;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "movies")
public class Movie extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    public String title;

    @Column(name = "poster_url")
    public String posterUrl;

    public String genre;

    @Enumerated(EnumType.STRING)
    public Status status;

    public enum Status {
        NOW_PLAYING,
        COMING_SOON
    }
}
