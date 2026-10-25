package com.example.demo.config;

import com.example.demo.model.Booking;
import com.example.demo.model.Bus;
import com.example.demo.model.Passenger;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BusRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepo, BusRepository busRepo, BookingRepository bookingRepo) {
        return args -> {
            // Seed Admin User
            User admin = new User("admin", "admin123", "System Administrator", "admin@busbooking.local", "9876543210", "ROLE_ADMIN");
            userRepo.save(admin);

            // Seed Dummy User 1
            User user1 = new User("john", "user123", "John Doe", "john.doe@email.com", "9876500001", "ROLE_USER");
            userRepo.save(user1);

            // Seed Dummy User 2
            User user2 = new User("sarah", "user123", "Sarah Connor", "sarah.c@email.com", "9876500002", "ROLE_USER");
            userRepo.save(user2);

            LocalDate today = LocalDate.now();

            // Seed Buses
            Bus bus1 = new Bus("DL-01-AB-1234", "Royal Travels Volvo A/C", "New Delhi", "Jaipur",
                    today.plusDays(1), LocalTime.of(6, 0), LocalTime.of(11, 30), 650.0, 32, "AC Sleeper (2+1)");

            Bus bus2 = new Bus("MH-02-CD-5678", "Express Liner Luxury", "Mumbai", "Pune",
                    today.plusDays(1), LocalTime.of(8, 30), LocalTime.of(12, 45), 450.0, 32, "Super Luxury AC");

            Bus bus3 = new Bus("KA-03-EF-9012", "Greenline Multi-Axle", "Bengaluru", "Hyderabad",
                    today.plusDays(2), LocalTime.of(21, 0), LocalTime.of(6, 30), 1100.0, 32, "AC Multi-Axle Sleeper");

            Bus bus4 = new Bus("TN-04-GH-3456", "Southern Star Express", "Chennai", "Bengaluru",
                    today.plusDays(2), LocalTime.of(14, 0), LocalTime.of(20, 0), 550.0, 32, "AC Semi-Sleeper");

            Bus bus5 = new Bus("WB-05-IJ-7890", "Eastern Gateway Coaches", "Kolkata", "Siliguri",
                    today.plusDays(3), LocalTime.of(19, 0), LocalTime.of(7, 30), 950.0, 32, "AC Sleeper");

            busRepo.save(bus1);
            busRepo.save(bus2);
            busRepo.save(bus3);
            busRepo.save(bus4);
            busRepo.save(bus5);

            // Seed Initial Bookings
            Booking b1 = new Booking("TKT-100234", user1, bus1, "John Doe", 28, "Male", "S5", 1, 650.0, "UPI", 
                    LocalDateTime.now().minusDays(1), "CONFIRMED");
            b1.addPassenger(new Passenger("S5", "John Doe", 28, "Male"));

            Booking b2 = new Booking("TKT-100235", user2, bus1, "Sarah Connor", 26, "Female", "S6", 1, 650.0, "CARD", 
                    LocalDateTime.now().minusHours(12), "CONFIRMED");
            b2.addPassenger(new Passenger("S6", "Sarah Connor", 26, "Female"));

            Booking b3 = new Booking("TKT-100236", user1, bus2, "John Doe", 28, "Male", "S12, S13", 2, 900.0, "UPI", 
                    LocalDateTime.now().minusHours(4), "CONFIRMED");
            b3.addPassenger(new Passenger("S12", "John Doe", 28, "Male"));
            b3.addPassenger(new Passenger("S13", "Mark Doe", 24, "Male"));

            bookingRepo.save(b1);
            bookingRepo.save(b2);
            bookingRepo.save(b3);
        };
    }
}
