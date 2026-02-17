package com.atomicbunker.reservation.dto;

import com.atomicbunker.reservation.domain.Movie;
import java.util.UUID;

public class MovieDTO {
    public UUID id;
    public String title;
    public String posterUrl;
    public String genre;
    public String status;

    public MovieDTO() {
    }

    public static MovieDTO from(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.id = movie.id;
        dto.title = movie.title;
        dto.posterUrl = movie.posterUrl;
        dto.genre = movie.genre;
        dto.status = movie.status != null ? movie.status.name() : null;
        return dto;
    }
}
