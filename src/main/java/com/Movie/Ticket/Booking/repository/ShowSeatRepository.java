package com.Movie.Ticket.Booking.repository;

import com.Movie.Ticket.Booking.entity.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    Optional<ShowSeat> findByShowIdAndSeatId(Long showId, Long seatId);

    List<ShowSeat> findByShowId(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ss from ShowSeat ss where ss.show.id = :showId and ss.seat.id = :seatId")
    Optional<ShowSeat> findByShowIdAndSeatIdForUpdate(@Param("showId") Long showId, @Param("seatId") Long seatId);

}
