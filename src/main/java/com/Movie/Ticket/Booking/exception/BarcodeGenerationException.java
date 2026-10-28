package com.Movie.Ticket.Booking.exception;

public class BarcodeGenerationException extends RuntimeException {
    public BarcodeGenerationException(String message) {
        super(message);
    }

    public BarcodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
