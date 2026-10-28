package com.Movie.Ticket.Booking.dto.ResponseDTO;

import java.math.BigDecimal;

public class BookingResponseDTO {
    private Long bookingId;
    private BigDecimal totalAmount;
    private String status;
//    private String barcode;
    public BookingResponseDTO(Long bookingId, String status,
                              BigDecimal totalAmount) {
        this.bookingId = bookingId;
        this.status = status;
        this.totalAmount = totalAmount;
//        this.barcode = barcodeUrl;
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

//    public String getBarcode() {
//        return barcode;
//    }
}
