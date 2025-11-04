package com.decoder.decoder_api.dto;

import lombok.AllArgsConstructor;  // Generates constructor with all fields
import lombok.Data;

/**
 * Response for GET /buildings/{id}/forecast endpoint.
 * Contains prediction and recommendation.
 */
@Data  // Lombok: getters, setters, toString, equals, hashCode
@AllArgsConstructor  // Lombok: constructor like new ForecastResponse(id, minutes, value, rec)
public class ForecastResponse {
    
    private String buildingId;         // Which building this forecast is for
    private int forecastMinutes;       // Forecast horizon (e.g., 60 minutes ahead)
    private double predictedValue;     // Predicted sensor value based on moving average
    private String recommendation;     // Action to take if threshold exceeded
    
    // Example response:
    // {
    //   "buildingId": "B001",
    //   "forecastMinutes": 60,
    //   "predictedValue": 78.5,
    //   "recommendation": "Activate energy-saving mode"
    // }
}
