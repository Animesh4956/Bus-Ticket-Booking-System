package com.example.demo.repository;

import com.example.demo.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    
    @Query("SELECT b FROM Bus b WHERE LOWER(b.source) LIKE LOWER(CONCAT('%', :source, '%')) " +
           "AND LOWER(b.destination) LIKE LOWER(CONCAT('%', :destination, '%'))")
    List<Bus> findBySourceAndDestination(String source, String destination);

    @Query("SELECT b FROM Bus b WHERE LOWER(b.source) LIKE LOWER(CONCAT('%', :source, '%')) " +
           "AND LOWER(b.destination) LIKE LOWER(CONCAT('%', :destination, '%')) " +
           "AND b.journeyDate = :journeyDate")
    List<Bus> findBySourceAndDestinationAndDate(String source, String destination, LocalDate journeyDate);

    @Query("SELECT DISTINCT b.source FROM Bus b")
    List<String> findDistinctSources();

    @Query("SELECT DISTINCT b.destination FROM Bus b")
    List<String> findDistinctDestinations();
}
