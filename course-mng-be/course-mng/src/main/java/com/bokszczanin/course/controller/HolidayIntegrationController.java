package com.bokszczanin.course.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/holiday")
public class HolidayIntegrationController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public ResponseEntity<?> getTodayHoliday() {
        LocalDate today = LocalDate.now();
        String year = String.valueOf(today.getYear());
        String countryCode = "PL"; // Polska

        String url = "https://date.nager.at/api/v3/PublicHolidays/" + year + "/" + countryCode;

        try {
            ResponseEntity<List<Map<String, Object>>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

            List<Map<String, Object>> holidays = response.getBody();
            if (holidays == null) return ResponseEntity.ok(Map.of("isHoliday", false));

            Optional<Map<String, Object>> todayHoliday = holidays.stream()
                    .filter(h -> h.get("date").equals(today.toString()))
                    .findFirst();

            return todayHoliday
                    .<ResponseEntity<?>>map(holiday ->
                            ResponseEntity.ok(Map.of(
                                    "isHoliday", true,
                                    "name", holiday.get("localName"),
                                    "globalName", holiday.get("name")
                            ))
                    )
                    .orElseGet(() -> ResponseEntity.ok(Map.of("isHoliday", false)));

        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "Nie udało się pobrać danych o święcie."));
        }
    }
}

