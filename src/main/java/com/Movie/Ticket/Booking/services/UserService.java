package com.Movie.Ticket.Booking.services;
import com.Movie.Ticket.Booking.dto.ResponseDTO.UserResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.UserRequestDTO;
import com.Movie.Ticket.Booking.entity.User;
import com.Movie.Ticket.Booking.exception.ResourceNotFoundException;
import com.Movie.Ticket.Booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.Movie.Ticket.Booking.exception.BookingConflictException;
import com.Movie.Ticket.Booking.exception.InvalidBookingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO dto) {

        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BookingConflictException("Email is already registered");
        }
        if (dto.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidBookingException("Password must not exceed 72 UTF-8 bytes");
        }
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        User savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone()
        );
    }

    public UserResponseDTO getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone()
        );
    }
}
