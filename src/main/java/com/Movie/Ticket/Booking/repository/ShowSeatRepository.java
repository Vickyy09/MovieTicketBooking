package com.Movie.Ticket.Booking.repository;

import com.Movie.Ticket.Booking.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    Optional<ShowSeat> findByShowIdAndSeatId(Long showId, Long seatId);

}
