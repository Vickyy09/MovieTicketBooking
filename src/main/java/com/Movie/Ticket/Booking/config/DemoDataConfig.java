package com.Movie.Ticket.Booking.config;

import com.Movie.Ticket.Booking.entity.*;
import com.Movie.Ticket.Booking.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@Profile("demo")
public class DemoDataConfig implements ApplicationRunner {
    private final UserRepository users;
    private final MovieRepository movies;
    private final TheatreRepository theatres;
    private final SeatRepository seats;
    private final ShowRepository shows;
    private final ShowSeatRepository showSeats;
    private final PasswordEncoder encoder;
    private final String adminPassword;

    public DemoDataConfig(UserRepository users, MovieRepository movies, TheatreRepository theatres,
                          SeatRepository seats, ShowRepository shows, ShowSeatRepository showSeats,
                          PasswordEncoder encoder, @Value("${demo.admin-password}") String adminPassword) {
        this.users = users;
        this.movies = movies;
        this.theatres = theatres;
        this.seats = seats;
        this.shows = shows;
        this.showSeats = showSeats;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (adminPassword.length() < 12 || adminPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Set DEMO_ADMIN_PASSWORD to a strong password of 12+ characters, at most 72 UTF-8 bytes");
        }
        var existingAdmin = users.findByEmailIgnoreCase("admin@example.test");
        if (existingAdmin.isPresent() && existingAdmin.get().getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("Demo admin email is already owned by a regular user");
        }
        if (existingAdmin.isEmpty()) {
            User admin = new User();
            admin.setName("Demo administrator");
            admin.setEmail("admin@example.test");
            admin.setPhone("9000000000");
            admin.setPasswordHash(encoder.encode(adminPassword));
            admin.setRole(UserRole.ADMIN);
            users.save(admin);
        }
        if (movies.count() > 0) {
            return;
        }
        Movie movie = new Movie();
        movie.setTitle("Demo Movie");
        movie.setDurationMinutes(120);
        movie.setLanguage("English");
        movie.setRating("PG");
        movies.save(movie);
        Theatre theatre = new Theatre();
        theatre.setName("Demo Cinema");
        theatre.setLocation("Demo City");
        theatres.save(theatre);
        Show show = new Show();
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setShowTime(LocalDateTime.now().plusDays(1));
        show.setPricePerSeat(new BigDecimal("150.00"));
        shows.save(show);
        for (int seatNumber = 1; seatNumber <= 50; seatNumber++) {
            Seat seat = new Seat();
            seat.setSeatNumber("A" + seatNumber);
            seat.setTheatre(theatre);
            seat.setType("STANDARD");
            seats.save(seat);
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(show);
            showSeat.setSeat(seat);
            showSeats.save(showSeat);
        }
    }
}
