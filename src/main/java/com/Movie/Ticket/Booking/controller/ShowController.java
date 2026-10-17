package com.Movie.Ticket.Booking.controller;

import com.Movie.Ticket.Booking.dto.ShowDTO;
import com.Movie.Ticket.Booking.entity.Show;
import com.Movie.Ticket.Booking.services.ShowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shows")
public class ShowController {

    @Autowired
    private ShowService showService;

    @PostMapping
    public Show createShow(@Valid @RequestBody ShowDTO showDTO) {
        return showService.createShow(showDTO);
    }

    @GetMapping("/movie/{movieId}")
    public List<ShowDTO> getShowsByMovie(@PathVariable Long movieId) {
        return showService.getShowsByMovie(movieId);
    }

}
