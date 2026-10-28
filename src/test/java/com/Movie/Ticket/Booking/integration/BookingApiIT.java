package com.Movie.Ticket.Booking.integration;

import com.Movie.Ticket.Booking.entity.*;
import com.Movie.Ticket.Booking.exception.BarcodeGenerationException;
import com.Movie.Ticket.Booking.repository.*;
import com.Movie.Ticket.Booking.services.BarcodeService;
import com.Movie.Ticket.Booking.services.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingApiIT {
    @Container
    static final MySQLContainer database = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void configuration(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", database::getJdbcUrl);
        properties.add("spring.datasource.username", database::getUsername);
        properties.add("spring.datasource.password", database::getPassword);
        properties.add("security.jwt.secret", () -> Base64.getEncoder().encodeToString(new byte[32]));
    }

    @LocalServerPort
    private int port;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository users;
    @Autowired private MovieRepository movies;
    @Autowired private TheatreRepository theatres;
    @Autowired private ShowRepository shows;
    @Autowired private SeatRepository seats;
    @Autowired private ShowSeatRepository showSeats;
    @Autowired private BookingRepository bookings;
    @Autowired private BookingService bookingService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TransactionTemplate transactions;
    @MockitoBean private BarcodeService barcodeService;

    private final JsonMapper json = JsonMapper.builder().build();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private User owner;
    private User other;
    private Show show;
    private Seat firstSeat;
    private Seat secondSeat;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void fixtures() throws Exception {
        for (String table : List.of("ticket", "booking", "show_seat", "seat", "shows", "movie", "theatre", "users")) {
            jdbc.update("DELETE FROM " + table);
        }
        when(barcodeService.generateBarcode(anyString())).thenReturn(new byte[]{1, 2, 3});
        owner = user("owner@example.test", UserRole.USER);
        other = user("other@example.test", UserRole.USER);
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setDurationMinutes(120);
        movies.save(movie);
        Theatre theatre = new Theatre();
        theatre.setName("Test Theatre");
        theatres.save(theatre);
        show = new Show();
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setShowTime(LocalDateTime.now().plusDays(1));
        show.setPricePerSeat(new BigDecimal("150.10"));
        shows.save(show);
        firstSeat = seat(theatre, "A1");
        secondSeat = seat(theatre, "A2");
        ownerToken = login(owner.getEmail());
        otherToken = login(other.getEmail());
    }

    @Test
    void simultaneousHttpBookingsHaveExactlyOneWinner() throws Exception {
        var responses = concurrently(List.of(
                () -> book(ownerToken, firstSeat.getId()),
                () -> book(otherToken, firstSeat.getId())));
        assertEquals(List.of(200, 409), responses.stream().map(HttpResponse::statusCode).sorted().toList());
        assertEquals(1L, bookings.count());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM ticket", Integer.class));
        assertFalse(available(firstSeat));
    }

    @Test
    void oppositeSeatOrderFinishesWithoutPartialBookings() throws Exception {
        var responses = concurrently(List.of(
                () -> book(ownerToken, firstSeat.getId(), secondSeat.getId()),
                () -> book(otherToken, secondSeat.getId(), firstSeat.getId())));
        assertEquals(List.of(200, 409), responses.stream().map(HttpResponse::statusCode).sorted().toList());
        assertEquals(1L, bookings.count());
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM ticket", Integer.class));
        assertFalse(available(firstSeat));
        assertFalse(available(secondSeat));
        assertEquals(new BigDecimal("300.20"), bookings.findAll().getFirst().getTotalAmount());
    }

    @Test
    void barcodeFailureRollsBackPreviouslyCreatedTicketsAndSeats() throws Exception {
        when(barcodeService.generateBarcode(anyString())).thenReturn(new byte[]{1})
                .thenThrow(new BarcodeGenerationException("provider unavailable"));
        assertEquals(502, book(ownerToken, firstSeat.getId(), secondSeat.getId()).statusCode());
        assertEquals(0L, bookings.count());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM ticket", Integer.class));
        assertTrue(available(firstSeat));
        assertTrue(available(secondSeat));
    }

    @Test
    void emptyBarcodeRollsBackBooking() throws Exception {
        when(barcodeService.generateBarcode(anyString())).thenReturn(new byte[0]);
        assertEquals(502, book(ownerToken, firstSeat.getId()).statusCode());
        assertEquals(0L, bookings.count());
        assertTrue(available(firstSeat));
    }

    @Test
    void cancellationIsIdempotentAndDoesNotReleaseARebookedSeat() throws Exception {
        long bookingId = bookingId(book(ownerToken, firstSeat.getId()));
        assertEquals(200, request("DELETE", "/bookings/" + bookingId, ownerToken, null).statusCode());
        assertTrue(available(firstSeat));
        assertEquals(200, book(otherToken, firstSeat.getId()).statusCode());
        assertEquals(200, request("DELETE", "/bookings/" + bookingId, ownerToken, null).statusCode());
        assertFalse(available(firstSeat));
        var cancelled = request("GET", "/bookings/" + bookingId, ownerToken, null);
        assertTrue(json.readTree(cancelled.body()).get("tickets").get(0).get("barcode").isNull());
    }

    @Test
    void overlappingCancellationsAndRebookingKeepNewReservation() throws Exception {
        long bookingId = bookingId(book(ownerToken, firstSeat.getId()));
        var cancelledButUncommitted = new CountDownLatch(1);
        var allowCommit = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(3)) {
            var firstCancellation = executor.submit(() -> transactions.execute(status -> {
                bookingService.cancelBooking(bookingId, owner.getId());
                cancelledButUncommitted.countDown();
                await(allowCommit);
                return true;
            }));
            assertTrue(cancelledButUncommitted.await(10, TimeUnit.SECONDS));
            var requestsReady = new CountDownLatch(2);
            var secondCancellation = executor.submit(() -> {
                requestsReady.countDown();
                return request("DELETE", "/bookings/" + bookingId, ownerToken, null);
            });
            var rebooking = executor.submit(() -> {
                requestsReady.countDown();
                return book(otherToken, firstSeat.getId());
            });
            assertTrue(requestsReady.await(10, TimeUnit.SECONDS));
            allowCommit.countDown();
            assertTrue(firstCancellation.get(20, TimeUnit.SECONDS));
            assertEquals(200, secondCancellation.get(20, TimeUnit.SECONDS).statusCode());
            assertEquals(200, rebooking.get(20, TimeUnit.SECONDS).statusCode());
        } finally {
            allowCommit.countDown();
        }
        assertFalse(available(firstSeat));
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM booking WHERE status = 'CONFIRMED'", Integer.class));
    }

    @Test
    void bookingAccessRequiresOwnershipAndAuthentication() throws Exception {
        long bookingId = bookingId(book(ownerToken, firstSeat.getId()));
        assertEquals(403, request("GET", "/bookings/" + bookingId, otherToken, null).statusCode());
        assertEquals(403, request("DELETE", "/bookings/" + bookingId, otherToken, null).statusCode());
        assertEquals(401, request("GET", "/bookings/" + bookingId, null, null).statusCode());
        assertEquals(401, request("GET", "/bookings/" + bookingId, "invalid-token", null).statusCode());
        assertFalse(available(firstSeat));
    }

    @Test
    void bookingOwnerComesFromTokenAndInputIsValidated() throws Exception {
        String body = json.writeValueAsString(Map.of("showId", show.getId(), "seatIds",
                List.of(firstSeat.getId()), "userId", other.getId()));
        var response = request("POST", "/bookings", ownerToken, body);
        assertEquals(200, response.statusCode());
        assertEquals(owner.getId(), bookings.findById(bookingId(response)).orElseThrow().getUser().getId());
        assertEquals(400, book(ownerToken, secondSeat.getId(), secondSeat.getId()).statusCode());
        assertEquals(400, request("POST", "/bookings", ownerToken, "{}").statusCode());
    }

    @Test
    void cancellationAfterShowStartsIsRejected() throws Exception {
        long bookingId = bookingId(book(ownerToken, firstSeat.getId()));
        jdbc.update("UPDATE shows SET show_time = ? WHERE id = ?", LocalDateTime.now().minusMinutes(1), show.getId());
        assertEquals(400, request("DELETE", "/bookings/" + bookingId, ownerToken, null).statusCode());
        assertFalse(available(firstSeat));
    }

    @Test
    void registrationHashesPasswordAndCannotGrantAdmin() throws Exception {
        String body = json.writeValueAsString(Map.of("name", "Registered User", "email", "new@example.test",
                "phone", "9876543210", "password", "LongTestPassword123!", "role", "ADMIN"));
        var response = request("POST", "/users", null, body);
        assertEquals(200, response.statusCode());
        var registered = users.findByEmailIgnoreCase("new@example.test").orElseThrow();
        assertEquals(UserRole.USER, registered.getRole());
        assertTrue(passwordEncoder.matches("LongTestPassword123!", registered.getPasswordHash()));
        assertFalse(response.body().contains("password"));
        assertEquals(409, request("POST", "/users", null, body).statusCode());
        assertFalse(login("new@example.test").isBlank());
    }

    @Test
    void adminOnlyCatalogWritesAndBadLoginReturnsUnauthorized() throws Exception {
        String movie = json.writeValueAsString(Map.of("title", "Admin Movie", "durationMinutes", 120,
                "language", "English", "rating", "PG"));
        assertEquals(403, request("POST", "/movies", ownerToken, movie).statusCode());
        user("admin@example.test", UserRole.ADMIN);
        assertEquals(200, request("POST", "/movies", login("admin@example.test"), movie).statusCode());
        String credentials = json.writeValueAsString(Map.of("email", owner.getEmail(), "password", "incorrect"));
        assertEquals(401, request("POST", "/auth/login", null, credentials).statusCode());
    }

    @Test
    void swaggerAndHealthAreAvailableAndMigrationsRan() throws Exception {
        var specification = request("GET", "/v3/api-docs", null, null);
        assertEquals(200, specification.statusCode());
        assertNotNull(json.readTree(specification.body()).get("paths").get("/auth/login"));
        assertEquals(200, request("GET", "/swagger-ui/index.html", null, null).statusCode());
        assertEquals(200, request("GET", "/actuator/health", null, null).statusCode());
        assertEquals(2, jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1", Integer.class));
    }

    private User user(String email, UserRole role) {
        User user = new User();
        user.setName(email);
        user.setEmail(email);
        user.setPhone("9876543210");
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode("LongTestPassword123!"));
        return users.saveAndFlush(user);
    }

    private Seat seat(Theatre theatre, String number) {
        Seat seat = new Seat();
        seat.setTheatre(theatre);
        seat.setSeatNumber(number);
        seats.saveAndFlush(seat);
        ShowSeat showSeat = new ShowSeat();
        showSeat.setShow(show);
        showSeat.setSeat(seat);
        showSeats.saveAndFlush(showSeat);
        return seat;
    }

    private String login(String email) throws Exception {
        var response = request("POST", "/auth/login", null,
                json.writeValueAsString(Map.of("email", email, "password", "LongTestPassword123!")));
        assertEquals(200, response.statusCode(), response.body());
        return json.readTree(response.body()).get("accessToken").asString();
    }

    private HttpResponse<String> book(String token, Long... seatIds) throws Exception {
        return request("POST", "/bookings", token,
                json.writeValueAsString(Map.of("showId", show.getId(), "seatIds", List.of(seatIds))));
    }

    private long bookingId(HttpResponse<String> response) {
        assertEquals(200, response.statusCode(), response.body());
        return json.readTree(response.body()).get("bookingId").asLong();
    }

    private boolean available(Seat seat) {
        return showSeats.findByShowIdAndSeatId(show.getId(), seat.getId()).orElseThrow().isAvailable();
    }

    private HttpResponse<String> request(String method, String path, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private List<HttpResponse<String>> concurrently(List<Callable<HttpResponse<String>>> operations) throws Exception {
        var ready = new CountDownLatch(operations.size());
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(operations.size())) {
            var pending = new ArrayList<java.util.concurrent.Future<HttpResponse<String>>>();
            for (var operation : operations) {
                pending.add(executor.submit(() -> {
                    ready.countDown();
                    await(start);
                    return operation.call();
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            var results = new ArrayList<HttpResponse<String>>();
            for (var future : pending) {
                results.add(future.get(25, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            start.countDown();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for concurrent operation");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }
}
