package com.decoder.decoder_api.service;  // Service layer package for business logic

// === SPRING FRAMEWORK IMPORTS ===
import org.springframework.beans.factory.annotation.Autowired;  // Auto-inject dependencies from Spring container
import org.springframework.beans.factory.annotation.Value;      // Read config properties from application.properties file
import org.springframework.stereotype.Service;                   // Mark class as Spring-managed service component

// === PROJECT-SPECIFIC IMPORTS ===
import com.decoder.decoder_api.dto.ForecastResponse;            // DTO for API response (buildingId, prediction, recommendation)
import com.decoder.decoder_api.model.Reading;                   // Entity representing a sensor reading from database
import java.util.List;                                          // Collection interface for storing multiple readings

/**
 * ForecastService: Generate energy forecasts and recommendations
 * Uses ASHRAE 62.1-2016 standard with 70% threshold
 */
@Service  // Spring will create and manage this bean automatically
public class ForecastService {
    
    @Autowired  // Spring injects QueryService instance automatically
    private QueryService queryService;  // Fetch readings from database

    @Value("${forecast.threshold:70.0}")  // Read threshold from config, default to 70.0 if not found
    private double thresholdValue;  // ASHRAE-compliant threshold: 70% is proactive alert point

   

    
    /**
     * Generate forecast by analyzing recent sensor readings
     * @param buildingId which building to forecast
     * @param minutes time window (used in response)
     * @return ForecastResponse with predicted value and recommendation
     */
    public ForecastResponse getForecast(String buildingId, int minutes) {
        // Fetch last 120 minutes of readings (provides stable trend, reduces noise)
        List<Reading> readings = queryService.getLastReadings(buildingId, 120);
        
        // Error handling: cannot forecast without data
        if (readings.isEmpty()) {  // Check if no readings were returned
            throw new RuntimeException("No readings found for building: " + buildingId);  // Fail fast with clear error
        }
        
        // Calculate simple moving average of last 5 readings
        double sum = 0;  // Accumulator for summing reading values
        int count = Math.min(5, readings.size());  // Take up to 5 readings (or fewer if less available)
        for (int i = 0; i < count; i++) {  // Loop through readings
            sum += readings.get(i).getReadingValue();  // Add each reading's numeric value to total
        }
        double predictedValue = sum / count;  // Divide by count to get average

        // Compare predicted average to 70% threshold (ASHRAE standard)
        String recommendation = predictedValue > thresholdValue  // If average exceeds 70%
            ? "Activate energy-saving mode"  // High energy: recommend savings
            : "Normal operation";  // Low energy: continue normal monitoring
        
        // Build and return response object to caller
        return new ForecastResponse(buildingId, minutes, predictedValue, recommendation);
    }
}
