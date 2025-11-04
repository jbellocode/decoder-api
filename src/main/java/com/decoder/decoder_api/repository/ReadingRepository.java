package com.decoder.decoder_api.repository;

import com.decoder.decoder_api.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;  // Provides CRUD methods
import org.springframework.data.jpa.repository.Query;          // Custom JPQL queries
import org.springframework.data.repository.query.Param;        // Named parameters
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Reading entity - handles database queries.
 * Spring Data JPA generates implementation automatically at runtime.
 */
@Repository  // Marks this as a data access component
public interface ReadingRepository extends JpaRepository<Reading, Long> {
    // JpaRepository<Reading, Long>
    //              ^^^^^^^ Entity type
    //                      ^^^^ Primary key type
    
    // Inherited methods we get for free (no code needed):
    // - save(reading)           → INSERT or UPDATE
    // - findById(id)            → SELECT by ID
    // - findAll()               → SELECT all readings
    // - deleteById(id)          → DELETE by ID
    // - count()                 → COUNT(*)
    
    /**
     * Get recent readings for a building within time window.
     * Used for dashboard queries and forecasting.
     * 
     * JPQL (Java Persistence Query Language) - not SQL!
     * Works across different databases (H2, PostgreSQL, MySQL).
     */
    @Query("SELECT r FROM Reading r WHERE r.buildingId = :buildingId " +
           "AND r.timestamp >= :since ORDER BY r.timestamp DESC")
    // Query breakdown:
    // - "Reading" = Entity name (not table name "readings")
    // - "r" = alias
    // - ":buildingId" = named parameter (prevents SQL injection)
    // - ORDER BY DESC = newest first
    List<Reading> findRecentReadings(
        @Param("buildingId") String buildingId,  // Maps to :buildingId in query
        @Param("since") LocalDateTime since      // Maps to :since in query
    );
    
    // Example usage in service:
    // LocalDateTime since = LocalDateTime.now().minusMinutes(60);
    // List<Reading> readings = repository.findRecentReadings("B001", since);
}
