package com.decoder.decoder_api.service; 
// This tells Java which folder/package this file belongs to. 
// It keeps project files organized and avoids naming conflicts with other classes.

// Below are the tools we need from other parts of the project and Java libraries.
import com.decoder.decoder_api.model.Reading; 
// Reading = the object that represents one sensor measurement in the database

import com.decoder.decoder_api.repository.BuildingRepository; 
// Used to check building information (like who owns it)

import com.decoder.decoder_api.repository.ReadingRepository; 
// Used to fetch saved readings from the database

import org.springframework.beans.factory.annotation.Autowired; 
// Lets Spring automatically give us ready-to-use objects (dependency injection)

import org.springframework.stereotype.Service; 
// Tells Spring this class holds business logic and should be created automatically at startup

import java.time.LocalDateTime; 
// Helps us work with dates + time in a modern and safe way

import java.util.List; 
// Allows us to return multiple readings as a list



/**
 * This service provides features for fetching readings
 * and checking who owns a building.
 * It acts like a middle layer between controllers (API) and the database.
 */
@Service // Marks this class as a Spring service
public class QueryService {

    @Autowired // Spring injects a working reading repository here
    private ReadingRepository readingRepository;

    @Autowired // Spring injects a working building repository here
    private BuildingRepository buildingRepository;

    /**
     * Gets recent readings from the last 'X' minutes for a specific building.
     * Example: "Give me all readings from the last 10 minutes for building B001"
     */
    public List<Reading> getLastReadings(String buildingId, int minutes) {
        // Calculates the time point 'X minutes ago' from right now
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);

        // Asks the database: "Show me readings newer than this time"
        return readingRepository.findRecentReadings(buildingId, since);
    }

    /**
     * Checks whether a user is the owner of the building.
     * Used to decide if the user is allowed to view or manage its data.
     */
    public boolean userOwnsBuilding(String username, String buildingId) {
        // Database call: does a building exist with this ID and this owner?
        return buildingRepository.existsByIdAndOwnerId(buildingId, username);
    }
}
