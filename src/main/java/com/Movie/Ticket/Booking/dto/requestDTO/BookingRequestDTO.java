package com.Movie.Ticket.Booking.dto.requestDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class BookingRequestDTO {

    @NotNull(message = "User id is required")
    @Positive(message = "User id must be greater than 0")
    private Long userId;

    @NotNull(message = "Show id is required")
    @Positive(message = "Show id must be greater than 0")
    private Long showId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<@NotNull(message = "Seat id cannot be null")
    @Positive(message = "Seat id must be greater than 0")
            Long> seatIds;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }
}
