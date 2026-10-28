package com.Movie.Ticket.Booking.services;

import com.Movie.Ticket.Booking.dto.ResponseDTO.BookingResponseDTO;
import com.Movie.Ticket.Booking.dto.ResponseDTO.TicketResponseDTO;
import com.Movie.Ticket.Booking.dto.ResponseDTO.ViewBookingResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.BookingRequestDTO;
import com.Movie.Ticket.Booking.entity.*;
import com.Movie.Ticket.Booking.exception.BookingConflictException;
import com.Movie.Ticket.Booking.exception.BarcodeGenerationException;
import com.Movie.Ticket.Booking.exception.InvalidBookingException;
import com.Movie.Ticket.Booking.exception.ResourceNotFoundException;
import com.Movie.Ticket.Booking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.Base64;
import java.util.stream.Collectors;
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
    public BookingResponseDTO bookSeats(BookingRequestDTO request, Long authenticatedUserId) {

        List<Long> seatIds = request.getSeatIds();

        if (seatIds == null || seatIds.isEmpty() || seatIds.stream().anyMatch(
                seatId -> seatId == null || seatId <= 0)) {
            throw new InvalidBookingException("Select valid seats");
        }

        if (seatIds.size() > 9) {
            throw new InvalidBookingException("Maximum 9 seats allowed");
        }

        if (seatIds.stream().distinct().count() != seatIds.size()) {
            throw new InvalidBookingException("Duplicate seats are not allowed in a booking");
        }

        seatIds = seatIds.stream().sorted().toList();

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        if (show.getShowTime().isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("Cannot book seats for past show");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.CONFIRMED);

        BigDecimal totalAmount = show.getPricePerSeat().multiply(BigDecimal.valueOf(seatIds.size()));
        booking.setTotalAmount(totalAmount);

        bookingRepository.save(booking);

        List<Ticket> tickets = new ArrayList<>();

        for (Long seatId : seatIds) {
            ShowSeat showSeat = showSeatRepository.findByShowIdAndSeatIdForUpdate(request.getShowId(), seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found for this show"));

            if (!showSeat.isAvailable()) {
                throw new BookingConflictException("Seat already booked");
            }

            showSeat.setAvailable(false);
            showSeatRepository.save(showSeat);

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(showSeat.getSeat());
            ticket.setIssuedAt(LocalDateTime.now());
            bookingRepository.saveAndFlush(booking);

            byte[] barcodeImage = barcodeService.generateBarcode("BOOKING_" + booking.getId() + "_SEAT_" + seatId
            );
            if (barcodeImage == null || barcodeImage.length == 0) {
                throw new BarcodeGenerationException("Barcode provider returned an empty image");
            }
            ticket.setBarcode(barcodeImage);

            ticketRepository.save(ticket);
            tickets.add(ticket);
        }

        booking.setTickets(tickets);
        bookingRepository.save(booking);

        return new BookingResponseDTO(
                booking.getId(),
                booking.getStatus().name(),
                booking.getTotalAmount()
//                tickets.get(0).getBarcode()
        );
    }


    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId, Long authenticatedUserId) {

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        requireOwner(booking, authenticatedUserId);
        if (BookingStatus.CANCELLED == booking.getStatus()) {
            return new BookingResponseDTO(booking.getId(), booking.getStatus().name(), booking.getTotalAmount());
        }

        if (!booking.getShow().getShowTime().isAfter(LocalDateTime.now())) {
            throw new InvalidBookingException("Cannot cancel a booking after the show starts");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId).stream()
                .sorted(Comparator.comparing(ticket -> ticket.getSeat().getId())).toList();

        for (Ticket ticket : tickets) {
            ShowSeat showSeat = showSeatRepository.findByShowIdAndSeatIdForUpdate(booking.getShow().getId(), ticket.getSeat().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Show seat not found"));

            showSeat.setAvailable(true);
            showSeatRepository.save(showSeat);
        }

        bookingRepository.save(booking);
        return new BookingResponseDTO(
                booking.getId(),
                booking.getStatus().name(),
                booking.getTotalAmount()
        );
    }

    @Transactional(readOnly = true)
    public ViewBookingResponseDTO viewBooking(Long bookingId, Long authenticatedUserId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        requireOwner(booking, authenticatedUserId);

        List<TicketResponseDTO> ticketDTOs = booking.getTickets().stream().map(ticket -> new TicketResponseDTO(ticket.getId(),
                                booking.getStatus() == BookingStatus.CONFIRMED
                                        ? Base64.getEncoder().encodeToString(ticket.getBarcode()) : null,
                                ticket.getIssuedAt(), booking.getId(),
                                ticket.getSeat().getId())).collect(Collectors.toList());

        return new ViewBookingResponseDTO(booking.getId(),
                booking.getTotalAmount(),
                booking.getStatus().name(),
                ticketDTOs
        );
    }

    private void requireOwner(Booking booking, Long authenticatedUserId) {
        if (!Objects.equals(booking.getUser().getId(), authenticatedUserId)) {
            throw new AccessDeniedException("You can only access your own bookings");
        }
    }
}
