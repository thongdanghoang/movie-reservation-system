package com.atomicbunker.reservation.mapper;

import com.atomicbunker.reservation.domain.Showtime;
import com.atomicbunker.reservation.dto.ShowtimeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface ShowtimeMapper {

    @Mapping(source = "movie.id", target = "movieId")
    @Mapping(source = "movie.title", target = "movieTitle")
    ShowtimeDTO toDTO(Showtime showtime);

    List<ShowtimeDTO> toDTOList(List<Showtime> showtimes);
}
