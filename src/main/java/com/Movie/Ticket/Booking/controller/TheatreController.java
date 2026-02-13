package com.Movie.Ticket.Booking.controller;

import com.Movie.Ticket.Booking.dto.ResponseDTO.TheatreResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.TheatreRequestDTO;
import com.Movie.Ticket.Booking.entity.Theatre;

import com.Movie.Ticket.Booking.services.TheatreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/theatres")
public class TheatreController {

    @Autowired
    private TheatreService theatreService;

    @PostMapping
    public TheatreResponseDTO createTheatre(@Valid @RequestBody TheatreRequestDTO dto) {
        return theatreService.createTheatre(dto);
    }

}

