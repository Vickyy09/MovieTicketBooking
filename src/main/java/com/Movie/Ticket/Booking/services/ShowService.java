package com.Movie.Ticket.Booking.services;

import com.Movie.Ticket.Booking.dto.ShowDTO;
import com.Movie.Ticket.Booking.entity.*;
import com.Movie.Ticket.Booking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class ShowService {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    public Show createShow(ShowDTO showDTO) {

        Movie movie = movieRepository.findById(showDTO.getMovieId()).orElse(null);
        Theatre theatre = theatreRepository.findById(showDTO.getTheatreId()).orElse(null);

        if (movie == null || theatre == null) {
            return null;
        }

        Show show = new Show();
        show.setShowTime(showDTO.getShowTime());
        show.setPricePerSeat(showDTO.getPrice());
        show.setMovie(movie);
        show.setTheatre(theatre);

        Show savedShow = showRepository.save(show);
        List<Seat> theatreSeats = seatRepository.findByTheatreId(theatre.getId());

        for (Seat seat : theatreSeats) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(savedShow);
            showSeat.setSeat(seat);
            showSeat.setAvailable(true);
            showSeatRepository.save(showSeat);
        }
        return savedShow;
    }

    public List<ShowDTO> getShowsByMovie(Long movieId) {

        List<Show> shows = showRepository.findByMovieId(movieId);
        return shows.stream().map(show -> {
            ShowDTO dto = new ShowDTO();
            dto.setShowTime(show.getShowTime());
            dto.setPrice(show.getPricePerSeat());
            dto.setMovieId(show.getMovie().getId());
            dto.setTheatreId(show.getTheatre().getId());
            return dto;
        }).toList();
    }

}
