package com.Movie.Ticket.Booking.dto.ResponseDTO;

public class BookingResponseDTO {
    private Long bookingId;
    private Double totalAmount;
    private String status;
//    private String barcode;
    public BookingResponseDTO(Long bookingId, String status,
                              double totalAmount) {
        this.bookingId = bookingId;
        this.status = status;
        this.totalAmount = totalAmount;
//        this.barcode = barcodeUrl;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

//    public String getBarcode() {
//        return barcode;
//    }
}
