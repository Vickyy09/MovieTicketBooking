package com.Movie.Ticket.Booking.repository;

import com.Movie.Ticket.Booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
