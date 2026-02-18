package com.atomicbunker.reservation.exception;

public class SeatAlreadyTakenException extends RuntimeException {
    
    public SeatAlreadyTakenException(String message) {
        super(message);
    }
}
