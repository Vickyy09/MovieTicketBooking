package com.Movie.Ticket.Booking.dto.ResponseDTO;

public class SeatResponseDTO {

    private Long id;
    private String seatNumber;
    private String type;
    private boolean available;

    public SeatResponseDTO(Long id, String seatNumber, String type, boolean available) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.type = type;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getType() {
        return type;
    }

    public boolean isAvailable() {
        return available;
    }
}
