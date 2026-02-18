package com.atomicbunker.reservation.mapper;

import com.atomicbunker.reservation.domain.Seat;
import com.atomicbunker.reservation.dto.SeatDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "cdi")
public interface SeatMapper {

    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    SeatDTO toDTO(Seat seat);

    List<SeatDTO> toDTOList(List<Seat> seats);

    @Named("uuidToString")
    default String uuidToString(UUID id) {
        return id != null ? id.toString() : null;
    }

    @Named("statusToString")
    default String statusToString(com.atomicbunker.reservation.domain.SeatStatus status) {
        return status != null ? status.name() : null;
    }
}
