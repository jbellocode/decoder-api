package com.decoder.decoder_api.model; // shows which package folder this class belongs to

import jakarta.persistence.*;   //imports @entity, @Table etc instead of coding them
import lombok.Data;           //generates getters/setters automatically
import java.util.List;        //imports list collection type

/**
 * Represents a building in the system.
 * Each building can have multiple sensor readings over time.
 */

@Entity
@Table(name = "buildings")
@Data
public class Building {
    
    // Using String ID because buildings have codes like "B001" or "MAIN_CAMPUS"
    // rather than auto-generated numbers
    @Id
    private String id;
    
    private String name;
    
    // Stores owner's username - in production this would link to a User table
    private String ownerId;
    
    // Relationship: one building has many readings (temperature, humdity,c02 etc.)
    // Lazy-loaded to avoid fetching thousands of readings unnecessarily
    @OneToMany(mappedBy = "building")
    private List<Reading> readings;
}


