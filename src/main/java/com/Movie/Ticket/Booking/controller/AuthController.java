package com.Movie.Ticket.Booking.controller;

import com.Movie.Ticket.Booking.dto.ResponseDTO.LoginResponseDTO;
import com.Movie.Ticket.Booking.dto.requestDTO.LoginRequestDTO;
import com.Movie.Ticket.Booking.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository users;
    private final JwtEncoder encoder;
    private final String issuer;

    public AuthController(AuthenticationManager authenticationManager, UserRepository users,
                          JwtEncoder encoder, @Value("${security.jwt.issuer}") String issuer) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.encoder = encoder;
        this.issuer = issuer;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        Instant issuedAt = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(issuer).subject(user.getId().toString())
                .issuedAt(issuedAt).expiresAt(issuedAt.plusSeconds(3600))
                .claim("roles", List.of(user.getRole().name())).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponseDTO(token, "Bearer", 3600);
    }
}
