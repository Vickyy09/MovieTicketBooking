package com.Movie.Ticket.Booking.services;

import com.Movie.Ticket.Booking.dto.ResponseDTO.BookingResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.BookingRequestDTO;
import com.Movie.Ticket.Booking.entity.*;
import com.Movie.Ticket.Booking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarcodeService barcodeService;


    @Transactional
    public BookingResponseDTO bookSeats(BookingRequestDTO request) {

        List<Long> seatIds = request.getSeatIds();

        if (seatIds.size() > 9) {
            throw new RuntimeException("Maximum 9 seats allowed");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found"));

        if (show.getShowTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book seats for past show");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRMED");

        double totalAmount = seatIds.size() * show.getPricePerSeat();
        booking.setTotalAmount(totalAmount);

        bookingRepository.save(booking);

        List<Ticket> tickets = new ArrayList<>();

        for (Long seatId : seatIds) {

            ShowSeat showSeat = showSeatRepository
                    .findByShowIdAndSeatId(request.getShowId(), seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found for this show"));

            if (!showSeat.isAvailable()) {
                throw new RuntimeException("Seat already booked");
            }

            showSeat.setAvailable(false);
            showSeatRepository.save(showSeat);

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(showSeat.getSeat());
            ticket.setIssuedAt(LocalDateTime.now());

            String barcode = barcodeService.generateBarcode(
                    "BOOKING_" + booking.getId() + "_SEAT_" + seatId
            );

            ticket.setBarcode(barcode);

            ticketRepository.save(ticket);
            tickets.add(ticket);
        }

        booking.setTickets(tickets);
        bookingRepository.save(booking);

        return new BookingResponseDTO(
                booking.getId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                tickets.get(0).getBarcode()
        );
    }


    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus("CANCELLED");

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);

        for (Ticket ticket : tickets) {

            ShowSeat showSeat = showSeatRepository
                    .findByShowIdAndSeatId(
                            booking.getShow().getId(),
                            ticket.getSeat().getId()
                    )
                    .orElseThrow(() -> new RuntimeException("Show seat not found"));

            showSeat.setAvailable(true);
            showSeatRepository.save(showSeat);
        }

        bookingRepository.save(booking);

        String barcode = tickets.isEmpty() ? null : tickets.get(0).getBarcode();

        return new BookingResponseDTO(
                booking.getId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                barcode
        );
    }
}