package com.Movie.Ticket.Booking.dto.ResponseDTO;

import java.time.LocalDateTime;

public class TicketResponseDTO {

    private Long id;
    private String barcode;
    private LocalDateTime issuedAt;
    private Long bookingId;
    private Long seatId;

    public TicketResponseDTO(Long id,
                             String barcode,
                             LocalDateTime issuedAt,
                             Long bookingId,
                             Long seatId) {
        this.id = id;
        this.barcode = barcode;
        this.issuedAt = issuedAt;
        this.bookingId = bookingId;
        this.seatId = seatId;
    }

    public Long getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getSeatId() {
        return seatId;
    }
}
