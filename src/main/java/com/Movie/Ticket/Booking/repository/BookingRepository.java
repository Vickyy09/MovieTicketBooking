package com.Movie.Ticket.Booking.repository;

import com.Movie.Ticket.Booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking,Long> {
}
