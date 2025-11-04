package com.decoder.decoder_api.service;   // Defines the folder (package) where this class lives

// Tools and classes we need to make this service work
import com.decoder.decoder_api.dto.ReadingRequest; // A small data package sent from the outside (API request)
import com.decoder.decoder_api.model.Building;
import com.decoder.decoder_api.model.Reading; // The Reading object that will be saved in the database
import com.decoder.decoder_api.repository.BuildingRepository;
import com.decoder.decoder_api.repository.ReadingRepository; // Helps us talk to the database
import org.springframework.beans.factory.annotation.Autowired; // Allows Spring to inject the repository automatically
import org.springframework.stereotype.Service; // Tells Spring this class does important business actions
import java.time.LocalDateTime; // Handles timestamps in a clean Java time format

/**
 * Handles business logic for ingesting sensor readings into the system.
 * converts incoming request data into a Reading entity and saves it.
 */
@Service // Tells Spring Boot this class contains business logic and should be auto-managed
public class IngestService {
    
    @Autowired // Spring will automatically create and inject the ReadingRepository instance
    private ReadingRepository readingRepository;

    @Autowired // Spring will declare and inject the BuildingRepository
    private BuildingRepository buildingRepository;

    /**
     * Saves a new sensor reading to the database
     * - Converts the incoming DTO into a Reading entity
     * - Assigns timestamp, value, building, and sensor info
     * - Persists the entity using the repository
     */
    public void ingestReading(ReadingRequest request) {
        // Validate building exists before accepting the reading
        Building building = buildingRepository.findById(request.getBuildingId())
            .orElseThrow(() -> new RuntimeException("Building not found: " + request.getBuildingId()));

        Reading reading = new Reading(); // Create a new Reading object (empty container for data)
        
        reading.setBuildingId(request.getBuildingId()); // Store which building this reading came from
        reading.setSensorId(request.getSensorId()); // Store the sensor that produced the data
        
        // Convert the timestamp string to a LocalDateTime object before saving it
        reading.setTimestamp(LocalDateTime.parse(request.getTimestamp()));
        
        // Store the actual measured value (temperature, humidity, CO2, etc.)
        reading.setReadingValue(request.getValue());
        
        readingRepository.save(reading); // Save the reading into the database
    }
}
