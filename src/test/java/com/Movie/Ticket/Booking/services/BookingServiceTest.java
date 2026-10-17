package com.Movie.Ticket.Booking.services;

import com.Movie.Ticket.Booking.dto.ResponseDTO.BookingResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.BookingRequestDTO;
import com.Movie.Ticket.Booking.entity.Booking;
import com.Movie.Ticket.Booking.entity.Seat;
import com.Movie.Ticket.Booking.entity.Show;
import com.Movie.Ticket.Booking.entity.ShowSeat;
import com.Movie.Ticket.Booking.entity.User;
import com.Movie.Ticket.Booking.exception.BookingConflictException;
import com.Movie.Ticket.Booking.exception.InvalidBookingException;
import com.Movie.Ticket.Booking.repository.BookingRepository;
import com.Movie.Ticket.Booking.repository.ShowRepository;
import com.Movie.Ticket.Booking.repository.ShowSeatRepository;
import com.Movie.Ticket.Booking.repository.TicketRepository;
import com.Movie.Ticket.Booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BarcodeService barcodeService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void bookSeatsRejectsDuplicateSeatIds() {
        BookingRequestDTO request = bookingRequest(List.of(10L, 10L));

        assertThrows(InvalidBookingException.class, () -> bookingService.bookSeats(request));

        verify(userRepository, never()).findById(any());
    }

    @Test
    void bookSeatsRejectsAlreadyBookedSeat() {
        BookingRequestDTO request = bookingRequest(List.of(10L));
        Show show = show();
        ShowSeat showSeat = showSeat(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(showRepository.findById(2L)).thenReturn(Optional.of(show));
        when(showSeatRepository.findByShowIdAndSeatIdForUpdate(2L, 10L)).thenReturn(Optional.of(showSeat));

        assertThrows(BookingConflictException.class, () -> bookingService.bookSeats(request));
    }

    @Test
    void bookSeatsMarksSeatUnavailableAndReturnsConfirmedBooking() {
        BookingRequestDTO request = bookingRequest(List.of(10L));
        ShowSeat showSeat = showSeat(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(showRepository.findById(2L)).thenReturn(Optional.of(show()));
        when(showSeatRepository.findByShowIdAndSeatIdForUpdate(2L, 10L)).thenReturn(Optional.of(showSeat));
        when(barcodeService.generateBarcode(any())).thenReturn(new byte[]{1, 2, 3});

        BookingResponseDTO response = bookingService.bookSeats(request);

        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(150.0, response.getTotalAmount());
        assertFalse(showSeat.isAvailable());
        verify(ticketRepository).save(any());
    }

    private BookingRequestDTO bookingRequest(List<Long> seatIds) {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setUserId(1L);
        request.setShowId(2L);
        request.setSeatIds(seatIds);
        return request;
    }

    private Show show() {
        Show show = new Show();
        show.setId(2L);
        show.setShowTime(LocalDateTime.now().plusDays(1));
        show.setPricePerSeat(150.0);
        return show;
    }

    private ShowSeat showSeat(boolean available) {
        Seat seat = new Seat();
        seat.setId(10L);
        seat.setSeatNumber("A1");

        ShowSeat showSeat = new ShowSeat();
        showSeat.setSeat(seat);
        showSeat.setAvailable(available);
        return showSeat;
    }
}
