package com.decoder.decoder_api.controller;
// This class handles incoming API requests related to buildings,
// such as fetching readings and forecasts.

/* ---- Imports: what we're bringing in and why ---- */
import com.decoder.decoder_api.dto.ForecastResponse; // The forecast result returned to the client
import com.decoder.decoder_api.model.Reading;        // Represents a single sensor reading
import com.decoder.decoder_api.service.ForecastService; // Makes predictions based on data
import com.decoder.decoder_api.service.QueryService; // Fetches readings + checks who owns a building
import org.springframework.beans.factory.annotation.Autowired; // Spring auto-creates needed services
import org.springframework.security.core.Authentication; // Gives us information about the logged-in user
import org.springframework.web.bind.annotation.*;        // Enables REST API annotations like @GetMapping
import java.util.List;                                 // Used to return multiple readings



/**
 * API controller for reading data and getting forecasts for buildings.
 * It includes access checks so only the building owner or admin can view data.
 */
@RestController // This class returns JSON/text responses for the API
@RequestMapping("/buildings") // All endpoints start with /buildings/...
public class QueryController {
    
    @Autowired // Spring injects the query service
    private QueryService queryService;
    
    @Autowired // Spring injects the forecast service
    private ForecastService forecastService;

    

    /**
     * GET /buildings/{id}/last-readings?minutes=60
     * Returns recent sensor readings for a building.
     * Default time window: 60 minutes.
     * Access rule: Only admins OR the building’s owner can see its data.
     */
    @GetMapping("/{id}/last-readings")
    public List<Reading> getLastReadings(
            @PathVariable String id,                // Building ID from the URL
            @RequestParam(defaultValue = "60") int minutes, // Optional query param: time range to look back
            Authentication auth) {                  // Info about who is currently logged in
        
        // Check if the user has the admin role
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // If not admin → user must own the building, or they're blocked
        if (!isAdmin && !queryService.userOwnsBuilding(auth.getName(), id)) {
            throw new SecurityException("Access denied"); // Stop unauthorized viewing
        }
        
        return queryService.getLastReadings(id, minutes); // Fetch and return the data
    }



    /**
     * GET /buildings/{id}/forecast?minutes=60
     * Returns a prediction and energy recommendation for the building.
     * Same security rule: only admin or the building’s owner can access it.
     */
    @GetMapping("/{id}/forecast")
    public ForecastResponse getForecast(
            @PathVariable String id,
            @RequestParam(defaultValue = "60") int minutes,
            Authentication auth) {
        
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !queryService.userOwnsBuilding(auth.getName(), id)) {
            throw new SecurityException("Access denied");
        }
        
        return forecastService.getForecast(id, minutes);
    }
}
