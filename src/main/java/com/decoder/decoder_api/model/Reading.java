package com.decoder.decoder_api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A single sensor reading from a building.
 * Stores time-series data like temperature, energy usage, etc.
 */
@Entity
@Table(name = "readings")
@Data
public class Reading {
    
    // Auto-increment ID since we'll have lots of readings
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Both fields map to building_id column, but only buildingId can write to it
    @Column(name = "building_id")
    private String buildingId;

    private String sensorId;
    private LocalDateTime timestamp;

    @Column(name = "reading_value")
    private double readingValue;
    
    // Links back to the building - used for queries
    @JsonIgnore  // This was set to prevent infinite loop
    @ManyToOne
    @JoinColumn(name = "building_id", insertable = false, updatable = false)
    private Building building;
}
