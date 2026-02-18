package com.atomicbunker.reservation.dto;

import java.util.UUID;

public record MovieDTO(
    UUID id,
    String title,
    String posterUrl,
    String genre,
    String status
) {}
