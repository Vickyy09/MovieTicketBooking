package com.Movie.Ticket.Booking.dto.ResponseDTO;


public class MovieResponseDTO {

    private Long id;
    private String title;
    private int durationMinutes;
    private String language;
    private String rating;

    public MovieResponseDTO(Long id, String title, int durationMinutes, String language, String rating) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.rating = rating;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getLanguage() {
        return language;
    }

    public String getRating() {
        return rating;
    }
}

