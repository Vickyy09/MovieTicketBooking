package com.Movie.Ticket.Booking.controller;

import com.Movie.Ticket.Booking.dto.ResponseDTO.MovieResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.MovieRequestDTO;
import com.Movie.Ticket.Booking.entity.Movie;
import com.Movie.Ticket.Booking.services.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @PostMapping
    public MovieResponseDTO createMovie(@Valid @RequestBody MovieRequestDTO dto) {
        return movieService.createMovie(dto);
    }

    @GetMapping
    public List<MovieResponseDTO> getAllMovies() {
        return movieService.getAllMovies();
    }
}


