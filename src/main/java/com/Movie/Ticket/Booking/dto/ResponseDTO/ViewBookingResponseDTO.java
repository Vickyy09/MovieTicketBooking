package com.Movie.Ticket.Booking.dto.ResponseDTO;

import java.util.List;
import java.math.BigDecimal;

public class ViewBookingResponseDTO {

    private Long bookingId;
    private BigDecimal totalAmount;
    private String status;
    private List<TicketResponseDTO> tickets;

    public ViewBookingResponseDTO(Long bookingId,
                                  BigDecimal totalAmount,
                                  String status,
                                  List<TicketResponseDTO> tickets) {
        this.bookingId = bookingId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.tickets = tickets;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public List<TicketResponseDTO> getTickets() {
        return tickets;
    }
}
