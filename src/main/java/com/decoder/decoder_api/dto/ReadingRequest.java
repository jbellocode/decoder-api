package com.decoder.decoder_api.dto;

import jakarta.validation.constraints.NotBlank;  // Validation: field cannot be empty
import jakarta.validation.constraints.NotNull;    // Validation: field cannot be null
import lombok.Data;  // Generates getters, setters, etc.

/**
 * Request body for POST /ingest endpoint.
 * Represents a single sensor reading sent by clients.
 */
@Data  // Lombok generates getters/setters automatically
public class ReadingRequest {
    
    // Building ID - which building this reading belongs to
    @NotBlank(message = "Building ID is required")  // Spring validates this before controller
    private String buildingId;
    
    // Sensor ID - which sensor captured this reading
    @NotBlank(message = "Sensor ID is required")
    private String sensorId;
    
    // Timestamp as string - will be parsed to LocalDateTime in service layer
    @NotBlank(message = "Timestamp is required")
    private String timestamp;  // Example: "2025-10-28T15:30:00"
    
    // Sensor value - the actual measurement
    @NotNull(message = "Value is required")  // Allows 0.0 but not null
    private Double value;  // Example: 22.5 (temperature), 145.8 (energy)
}
