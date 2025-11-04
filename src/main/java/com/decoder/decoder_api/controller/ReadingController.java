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
import com.decoder.decoder_api.service.QueryService;           // Service layer - handles business logic for queries
import com.decoder.decoder_api.model.Reading;                  // Database entity representing sensor reading
import java.util.List;                                         // Collection interface for storing multiple readings
import java.util.HashMap;                                      // Map for error responses
import java.util.Map;                                          // Interface for key-value pairs


/**
 * REST Controller for Reading API endpoints
 * Handles HTTP requests for sensor reading retrieval
 * Implements RBAC: admin sees all data, owner sees only their buildings
 * 
 * Base URL: /api/buildings
 * Endpoints:
 * - GET /api/buildings/{id}/last-readings?minutes=60
 * 
 * Required Header:
 * - X-User-Role: "admin" or "owner" (mock JWT)
 */
@RestController  // Spring annotation: this class handles REST requests and returns JSON
@RequestMapping("/api/buildings")  // Base path for all endpoints in this controller
public class ReadingController {
    
    
    // === DEPENDENCY INJECTION ===
    @Autowired  // Spring automatically injects QueryService instance from container
    private QueryService queryService;  // Service to query readings from database
    
    
    /**
     * Get last readings for a building within time window
     * RBAC Enforcement:
     * - admin role: can query ANY building
     * - owner role: can query ONLY buildings they own (mock: only B001)
     * 
     * HTTP Method: GET
     * Endpoint: /api/buildings/{id}/last-readings?minutes=60
     * Example: http://localhost:8080/api/buildings/B001/last-readings?minutes=60
     * Header: X-User-Role: admin
     * 
     * @param buildingId - the building ID to query (from URL path)
     * @param minutes - time window in minutes (from query parameter, default 60)
     * @param userRole - user's role from X-User-Role header (admin or owner)
     * @return ResponseEntity with list of readings and HTTP 200 OK status
     *         or HTTP 403 FORBIDDEN if unauthorized
     *         or HTTP 404 NOT FOUND if building not found
     */
    @GetMapping("/{id}/last-readings")  // Map HTTP GET requests to /buildings/{id}/last-readings
    public ResponseEntity<?> getLastReadings(
        @PathVariable("id") String buildingId,  // Extract {id} from URL path
        @RequestParam(value = "minutes", defaultValue = "60") int minutes,  // Query param ?minutes=60, default 60
        @RequestHeader(value = "X-User-Role", defaultValue = "guest") String userRole  // Extract X-User-Role header, default guest if missing
    ) {
        
        // Step 1: Validate input
        if (buildingId == null || buildingId.isEmpty()) {  // Check if building ID is empty
            // Return 400 Bad Request
            Map<String, String> error = new HashMap<>();  // Create error response map
            error.put("error", "Building ID cannot be empty");  // Add error message
            error.put("status", "400");  // Add HTTP status code
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);  // Bad request response
        }
        
        // Step 2: RBAC enforcement - check user authorization
        // Business rule: admin can see all buildings, owner can only see their own (B001)
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
            error.put("error", "Forbidden: You can only access your own buildings (B001)");  // Error message
            error.put("buildingId", buildingId);  // Return building ID that was denied
            error.put("userRole", userRole);  // Return user's role
            error.put("status", "403");  // Add HTTP status code
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);  // Forbidden response
        }
        
        // Step 3: Authorization passed, call service to fetch readings
        try {
            List<Reading> readings = queryService.getLastReadings(buildingId, minutes);  // Query service returns list of readings
            
            // Step 4: Check if readings were found
            if (readings == null || readings.isEmpty()) {  // If no readings exist
                // Return 204 No Content - request succeeded but no data
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);  // No content response
            }
            
            // Step 5: Return successful response with readings
            // HTTP 200 OK with JSON body containing readings list
            return ResponseEntity.status(HttpStatus.OK).body(readings);  // Success response with data
            
        } catch (RuntimeException e) {  // Catch service layer exceptions
            // Service threw exception (likely building not found)
            // Return 404 Not Found
            Map<String, String> error = new HashMap<>();  // Create error response map
            error.put("error", e.getMessage());  // Add exception message
            error.put("status", "404");  // Add HTTP status code
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);  // Not found response
        }
    }
}
