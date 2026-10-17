package com.Movie.Ticket.Booking.controller;

import com.Movie.Ticket.Booking.dto.ResponseDTO.BookingResponseDTO;
import com.Movie.Ticket.Booking.dto.ResponseDTO.SeatResponseDTO;
import com.Movie.Ticket.Booking.dto.ResponseDTO.ViewBookingResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.BookingRequestDTO;
import com.Movie.Ticket.Booking.repository.ShowSeatRepository;
import com.Movie.Ticket.Booking.repository.SeatRepository;
import com.Movie.Ticket.Booking.services.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;


    @GetMapping("/theatre/{theatreId}/seats")
    public List<SeatResponseDTO> getSeats(@PathVariable @Positive(message = "Theatre id must be greater than 0") Long theatreId) {
        return seatRepository.findByTheatreId(theatreId)
                .stream()
                .map(seat -> new SeatResponseDTO(seat.getId(), seat.getSeatNumber(), seat.getType(), seat.isAvailable()))
                .toList();
    }

    @GetMapping("/shows/{showId}/seats")
    public List<SeatResponseDTO> getShowSeats(@PathVariable @Positive(message = "Show id must be greater than 0") Long showId) {
        return showSeatRepository.findByShowId(showId)
                .stream()
                .map(showSeat -> new SeatResponseDTO(
                        showSeat.getSeat().getId(),
                        showSeat.getSeat().getSeatNumber(),
                        showSeat.getSeat().getType(),
                        showSeat.isAvailable()))
                .toList();
    }

    @PostMapping
    public BookingResponseDTO bookSeats(
            @Valid @RequestBody BookingRequestDTO request) {
        return bookingService.bookSeats(request);
    }

    @GetMapping("/{id}")
    public ViewBookingResponseDTO getBooking(@PathVariable Long id) {
        return bookingService.viewBooking(id);
    }


    @DeleteMapping("/{id}")
    public BookingResponseDTO cancelBooking(@PathVariable @Positive(message = "Booking id must be greater than 0") Long id) {
        return bookingService.cancelBooking(id);
    }
}
