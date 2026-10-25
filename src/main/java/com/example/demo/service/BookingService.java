package com.example.demo.service;

import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Extracts all individual booked seat codes (e.g., ["S1", "S2", "S5"]) for a bus.
     */
    public Set<String> getBookedSeatsForBus(Long busId) {
        List<String> rawSeatStrings = bookingRepository.findRawBookedSeatStringsByBusId(busId);
        Set<String> bookedSeats = new HashSet<>();
        for (String raw : rawSeatStrings) {
            if (raw != null) {
                String[] parts = raw.split(",");
                for (String p : parts) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty()) {
                        bookedSeats.add(trimmed);
                    }
                }
            }
        }
        return bookedSeats;
    }
}
