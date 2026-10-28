package com.Movie.Ticket.Booking.dto.ResponseDTO;

public record LoginResponseDTO(String accessToken, String tokenType, long expiresIn) {
}
