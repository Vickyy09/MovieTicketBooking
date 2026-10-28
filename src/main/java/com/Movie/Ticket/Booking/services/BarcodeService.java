package com.Movie.Ticket.Booking.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.util.UriComponentsBuilder;
import com.Movie.Ticket.Booking.exception.BarcodeGenerationException;
import java.time.Duration;
import java.net.URI;

@Service
public class BarcodeService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public BarcodeService(@Value("${barcode.url:https://quickchart.io/barcode}") String baseUrl,
                          @Value("${barcode.connect-timeout:2s}") Duration connectTimeout,
                          @Value("${barcode.read-timeout:3s}") Duration readTimeout) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restTemplate = new RestTemplate(factory);
        this.baseUrl = baseUrl;
    }

    public byte[] generateBarcode(String text) {

        try {
            URI url = UriComponentsBuilder.fromUriString(baseUrl).queryParam("type", "code128")
                    .queryParam("text", text).build().encode().toUri();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(MediaType.parseMediaTypes("image/png"));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                throw new BarcodeGenerationException("Barcode provider returned an empty image");
            }
            return response.getBody();

        } catch (Exception exception) {
            throw new BarcodeGenerationException("Barcode API failed", exception);
        }
    }
}
