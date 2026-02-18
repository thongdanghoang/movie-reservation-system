package com.atomicbunker.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HoldSeatRequest {
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
}
