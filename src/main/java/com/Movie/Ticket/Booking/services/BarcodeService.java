package com.Movie.Ticket.Booking.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BarcodeService {

    public String generateBarcode(String text) {

        try {
            String url = "https://quickchart.io/barcode?type=code128&text=" + text;

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForObject(url, String.class);

            return url;

        } catch (Exception e) {
            throw new RuntimeException("Barcode API failed");
        }
    }
}
