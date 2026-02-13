package com.Movie.Ticket.Booking.dto.ResponseDTO;


public class TheatreResponseDTO {

    private Long id;
    private String name;
    private String location;
    private int totalSeats;

    public TheatreResponseDTO(Long id, String name, String location, int totalSeats) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.totalSeats = totalSeats;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getTotalSeats() {
        return totalSeats;
    }
}

