package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.model.Bus;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserOrderByBookingTimeDesc(User user);
    
    List<Booking> findByBusAndStatus(Bus bus, String status);

    Optional<Booking> findByBookingReference(String bookingReference);

    @Query("SELECT b.seatNumber FROM Booking b WHERE b.bus.id = :busId AND b.status = 'CONFIRMED'")
    List<String> findRawBookedSeatStringsByBusId(Long busId);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(b.totalFare), 0.0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Double calculateTotalRevenue();

    List<Booking> findAllByOrderByBookingTimeDesc();
}
