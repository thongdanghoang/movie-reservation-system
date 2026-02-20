package com.atomicbunker.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
                @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") String email,

                @NotBlank(message = "Phone is required") String phone) {
}
