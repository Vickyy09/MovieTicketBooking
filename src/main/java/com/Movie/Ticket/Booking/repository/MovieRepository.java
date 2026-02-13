package com.Movie.Ticket.Booking.repository;

import com.Movie.Ticket.Booking.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie,Long> {
}
