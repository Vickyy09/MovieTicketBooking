package com.Movie.Ticket.Booking.services;

import com.Movie.Ticket.Booking.dto.ResponseDTO.TheatreResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.TheatreRequestDTO;
import com.Movie.Ticket.Booking.entity.Seat;
import com.Movie.Ticket.Booking.entity.Theatre;
import com.Movie.Ticket.Booking.repository.SeatRepository;
import com.Movie.Ticket.Booking.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TheatreService {

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private SeatRepository seatRepository;

    public TheatreResponseDTO createTheatre(TheatreRequestDTO dto) {

        Theatre theatre = new Theatre();
        theatre.setName(dto.getName());
        theatre.setLocation(dto.getLocation());
        Theatre savedTheatre = theatreRepository.save(theatre);
        int seatCount = 0;

        for (char row = 'A'; row <= 'E'; row++) {
            for (int number = 1; number <= 10; number++) {

                Seat seat = new Seat();
                seat.setSeatNumber(row + String.valueOf(number));
                seat.setAvailable(true);
                seat.setTheatre(savedTheatre);

                seatRepository.save(seat);
                seatCount++;
            }
        }

        return new TheatreResponseDTO(
                savedTheatre.getId(),
                savedTheatre.getName(),
                savedTheatre.getLocation(),
                seatCount
        );
    }
}
