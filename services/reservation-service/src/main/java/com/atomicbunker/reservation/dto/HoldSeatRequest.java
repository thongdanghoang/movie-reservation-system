package com.atomicbunker.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public record HoldSeatRequest(
    @NotBlank(message = "Session ID is required")
    String sessionId
) {}
