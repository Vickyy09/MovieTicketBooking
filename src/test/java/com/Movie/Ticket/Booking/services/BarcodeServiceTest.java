package com.Movie.Ticket.Booking.services;

import com.Movie.Ticket.Booking.exception.BarcodeGenerationException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeServiceTest {
    private HttpServer server;
    private java.util.concurrent.ExecutorService executor;
    private BarcodeService service;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.start();
        service = new BarcodeService("http://127.0.0.1:" + server.getAddress().getPort() + "/barcode",
                Duration.ofMillis(200), Duration.ofMillis(200));
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
        executor.shutdownNow();
    }

    @Test
    void returnsImageBytes() {
        server.createContext("/barcode", exchange -> {
            byte[] image = new byte[]{1, 2, 3};
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, image.length);
            exchange.getResponseBody().write(image);
            exchange.close();
        });
        assertArrayEquals(new byte[]{1, 2, 3}, service.generateBarcode("booking with spaces"));
    }

    @Test
    void rejectsEmptyResponse() {
        server.createContext("/barcode", exchange -> {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        assertThrows(BarcodeGenerationException.class, () -> service.generateBarcode("booking"));
    }

    @Test
    void rejectsProviderError() {
        server.createContext("/barcode", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        assertThrows(BarcodeGenerationException.class, () -> service.generateBarcode("booking"));
    }

    @Test
    void slowProviderIsBoundedByReadTimeout() {
        server.createContext("/barcode", exchange -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> assertThrows(BarcodeGenerationException.class, () -> service.generateBarcode("booking")));
    }
}
