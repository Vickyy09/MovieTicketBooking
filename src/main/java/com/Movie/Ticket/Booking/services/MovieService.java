package com.Movie.Ticket.Booking.services;

import com.Movie.Ticket.Booking.dto.ResponseDTO.MovieResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.MovieRequestDTO;
import com.Movie.Ticket.Booking.entity.Movie;
import com.Movie.Ticket.Booking.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public MovieResponseDTO createMovie(MovieRequestDTO dto) {

        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setDurationMinutes(dto.getDurationMinutes());
        movie.setLanguage(dto.getLanguage());
        movie.setRating(dto.getRating());

        Movie savedMovie = movieRepository.save(movie);

        return new MovieResponseDTO(
                savedMovie.getId(),
                savedMovie.getTitle(),
                savedMovie.getDurationMinutes(),
                savedMovie.getLanguage(),
                savedMovie.getRating()
        );
    }

    public MovieResponseDTO getMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        return new MovieResponseDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getDurationMinutes(),
                movie.getLanguage(),
                movie.getRating()
        );
    }

    public List<MovieResponseDTO> getAllMovies() {

        return movieRepository.findAll()
                .stream()
                .map(movie -> new MovieResponseDTO(
                        movie.getId(),
                        movie.getTitle(),
                        movie.getDurationMinutes(),
                        movie.getLanguage(),
                        movie.getRating()
                ))
                .collect(Collectors.toList());
    }
}
