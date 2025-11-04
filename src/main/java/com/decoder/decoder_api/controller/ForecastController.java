package com.decoder.decoder_api.controller;  // Controller layer - handles HTTP requests



// === SPRING WEB FRAMEWORK IMPORTS ===
import org.springframework.beans.factory.annotation.Autowired;  // Auto-inject service dependencies from Spring container
import org.springframework.http.HttpStatus;                     // HTTP status codes (200, 201, 404, etc.)
import org.springframework.http.ResponseEntity;                 // Wrap response with status code and headers
import org.springframework.web.bind.annotation.GetMapping;      // Map HTTP GET requests to handler methods
import org.springframework.web.bind.annotation.PathVariable;    // Extract variable from URL path (e.g., /buildings/B001)
import org.springframework.web.bind.annotation.RequestHeader;   // Extract HTTP header (e.g., X-User-Role)
import org.springframework.web.bind.annotation.RequestMapping;  // Base URL path for this controller
import org.springframework.web.bind.annotation.RequestParam;    // Extract query parameters (e.g., ?minutes=60)
import org.springframework.web.bind.annotation.RestController;  // Mark class as REST controller (returns JSON not HTML)



// === PROJECT-SPECIFIC IMPORTS ===
import com.decoder.decoder_api.service.ForecastService;         // Service layer - handles business logic for forecasting
import com.decoder.decoder_api.dto.ForecastResponse;            // DTO - Data Transfer Object for forecast response
import java.util.HashMap;                                       // Map for building error response objects
import java.util.Map;                                           // Interface for key-value pairs


/**
 * REST Controller for Forecast API endpoints
 * Handles HTTP requests for energy forecasting and recommendations
 * Implements RBAC: admin sees all data, owner sees only their buildings
 * 
 * Base URL: /api/buildings
 * Endpoints:
 * - GET /api/buildings/{id}/forecast?minutes=60
 * 
 * Required Header:
 * - X-User-Role: "admin" or "owner" (mock JWT)
 */
@RestController  // Spring annotation: this class handles REST requests and returns JSON
@RequestMapping("/api/buildings")  // Base path for all endpoints in this controller
public class ForecastController {
    
    
    // === DEPENDENCY INJECTION ===
    @Autowired  // Spring automatically injects ForecastService instance from container
    private ForecastService forecastService;  // Service to generate forecasts with recommendations
    
    
    /**
     * Get energy forecast for a building with recommendation
     * RBAC Enforcement:
     * - admin role: can forecast ANY building
     * - owner role: can forecast ONLY buildings they own (mock: only B001)
     * 
     * HTTP Method: GET
     * Endpoint: /api/buildings/{id}/forecast?minutes=60
     * Example: http://localhost:8080/api/buildings/B001/forecast?minutes=60
     * Header: X-User-Role: admin
     * 
     * @param buildingId - the building ID to forecast (from URL path)
     * @param minutes - time window in minutes (from query parameter, default 60)
     * @param userRole - user's role from X-User-Role header (admin or owner)
     * @return ResponseEntity with ForecastResponse and HTTP 200 OK status
     *         or HTTP 403 FORBIDDEN if unauthorized
     *         or HTTP 404 NOT FOUND if building not found
     */
    @GetMapping("/{id}/forecast")  // Map HTTP GET requests to /buildings/{id}/forecast
    public ResponseEntity<?> getForecast(
        @PathVariable("id") String buildingId,  // Extract {id} from URL path
        @RequestParam(value = "minutes", defaultValue = "60") int minutes,  // Query param ?minutes=60, default 60
        @RequestHeader(value = "X-User-Role", defaultValue = "guest") String userRole  // Extract X-User-Role header, default guest if missing
    ) {
        
        // Step 1: Validate input - check if building ID is provided
        if (buildingId == null || buildingId.isEmpty()) {  // Check if building ID is empty
            // Return 400 Bad Request with error message
            Map<String, String> error = new HashMap<>();  // Create error response map for invalid input
            error.put("error", "Building ID cannot be empty");  // Add descriptive error message
            error.put("status", "400");  // Add HTTP status code for reference
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);  // Return 400 Bad Request response
        }
        
        // Step 2: RBAC enforcement - check user authorization
        // Business rule: admin can forecast all buildings, owner can only forecast their own (B001)
        boolean isAdmin = "admin".equalsIgnoreCase(userRole);  // Check if user is admin
        boolean isOwner = "owner".equalsIgnoreCase(userRole);  // Check if user is owner
        boolean isOwnedByUser = "B001".equals(buildingId);  // Mock: owner only owns B001
        
        // If user is neither admin nor owner, deny access
        if (!isAdmin && !isOwner) {  // User is guest or invalid role
            // Return 403 Forbidden - user not authenticated
            Map<String, String> error = new HashMap<>();  // Create error response map
            error.put("error", "Unauthorized: X-User-Role header required (admin or owner)");  // Error message
            error.put("userRole", userRole);  // Return the role that was rejected
            error.put("status", "403");  // Add HTTP status code
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);  // Forbidden response
        }
        
        // If user is owner but trying to access someone else's building, deny access
        if (isOwner && !isOwnedByUser) {  // Owner role but not their building
            // Return 403 Forbidden - owner can only access their own buildings
            Map<String, String> error = new HashMap<>();  // Create error response map
            error.put("error", "Forbidden: You can only forecast your own buildings (B001)");  // Error message
            error.put("buildingId", buildingId);  // Return building ID that was denied
            error.put("userRole", userRole);  // Return user's role
            error.put("status", "403");  // Add HTTP status code
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);  // Forbidden response
        }
        
        // Step 3: Validate minutes parameter - ensure it's positive
        if (minutes <= 0) {  // Check if minutes is positive
            // Return 400 Bad Request with validation error
            Map<String, String> error = new HashMap<>();  // Create error response map for invalid parameter
            error.put("error", "Minutes must be greater than 0");  // Add descriptive error message
            error.put("status", "400");  // Add HTTP status code for reference
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);  // Return 400 Bad Request response
        }
        
        // Step 4: Authorization passed, call ForecastService to generate forecast
        try {
            ForecastResponse forecast = forecastService.getForecast(buildingId, minutes);  // Generate forecast with ASHRAE logic
            
            // Step 5: Check if forecast was generated successfully
            if (forecast == null) {  // If forecast is null (defensive programming)
                // Return 404 Not Found
                Map<String, String> error = new HashMap<>();  // Create error response map for not found scenario
                error.put("error", "Building not found");  // Add error message
                error.put("buildingId", buildingId);  // Include building ID that was not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);  // Return 404 Not Found response
            }
            
            // Step 6: Return successful response with forecast data to client
            // HTTP 200 OK with JSON body containing ForecastResponse object
            return ResponseEntity.status(HttpStatus.OK).body(forecast);  // Return 200 OK with forecast data
            
        } catch (RuntimeException e) {  // Catch runtime exceptions from service layer
            // Service threw exception - likely no readings available
            // Return 500 Internal Server Error with error details
            Map<String, String> error = new HashMap<>();  // Create error response map for server error
            error.put("error", e.getMessage());  // Add exception message
            error.put("status", "500");  // Add HTTP status code for reference
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);  // Return 500 response
        }
    }
}
