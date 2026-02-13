package com.Movie.Ticket.Booking.controller;

import com.Movie.Ticket.Booking.dto.ShowDTO;
import com.Movie.Ticket.Booking.entity.Show;
import com.Movie.Ticket.Booking.services.ShowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shows")
public class ShowController {

    @Autowired
    private ShowService showService;

    @PostMapping
    public ResponseEntity<?> createShow(@Valid @RequestBody ShowDTO showDTO) {

        Show savedShow = showService.createShow(showDTO);

        if (savedShow == null) {
            return ResponseEntity.badRequest().body("Invalid Movie ID or Theatre ID");
        }

        return ResponseEntity.ok(savedShow);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowDTO>> getShowsByMovie(@PathVariable Long movieId) {
        List<ShowDTO> shows = showService.getShowsByMovie(movieId);
        return ResponseEntity.ok(shows);
    }

}