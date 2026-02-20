package com.atomicbunker.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Phone is required") String phone) {
}
