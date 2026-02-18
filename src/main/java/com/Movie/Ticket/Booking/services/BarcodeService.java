package com.Movie.Ticket.Booking.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BarcodeService {
    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] generateBarcode(String text) {

        try {
            String url = "https://quickchart.io/barcode?type=code128&text=" + text;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(MediaType.parseMediaTypes("image/png"));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Barcode API failed", e);
        }
    }
}
