package com.atomicbunker.reservation.mapper;

import com.atomicbunker.reservation.domain.Movie;
import com.atomicbunker.reservation.dto.MovieDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface MovieMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    MovieDTO toDTO(Movie movie);

    List<MovieDTO> toDTOList(List<Movie> movies);

    @Named("statusToString")
    default String statusToString(com.atomicbunker.reservation.domain.MovieStatus status) {
        return status != null ? status.name() : null;
    }
}
