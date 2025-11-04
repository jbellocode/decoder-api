package com.decoder.decoder_api;  // Integration test package - tests full Spring context

import org.junit.jupiter.api.Test;  // JUnit 5 test annotation
import org.springframework.beans.factory.annotation.Autowired;  // Inject Spring beans into test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;  // Enable MockMvc for HTTP testing
import org.springframework.boot.test.context.SpringBootTest;  // Load full Spring Boot context for testing
import org.springframework.test.web.servlet.MockMvc;  // Mock HTTP servlet for testing without network
import org.springframework.test.web.servlet.MvcResult;  // Capture test response

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  // Import request builders (get, post, etc)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  // Import result matchers (status, content, etc)
import static org.hamcrest.Matchers.*;  // Import Hamcrest matchers for assertions


/**
 * Integration Tests for Reading and Forecast API endpoints
 * Tests the complete request-response cycle with Spring Boot context
 * Verifies:
 * - API endpoints are properly registered
 * - RBAC authorization works
 * - HTTP status codes are correct
 * - JSON responses are properly formatted
 */
@SpringBootTest  // Load full Spring Boot application context for testing
@AutoConfigureMockMvc  // Automatically configure MockMvc for HTTP testing
public class ReadingForecastIntegrationTest {
    
    
    @Autowired  // Spring injects MockMvc instance for HTTP testing
    private MockMvc mockMvc;  // MockMvc simulates HTTP requests without network


    // ===== READING ENDPOINT INTEGRATION TESTS =====
    
    /**
     * Test: Reading API with Admin role should succeed
     * Endpoint: GET /api/buildings/B001/last-readings
     * Expected: HTTP 204 No Content (authorized, but no readings)
     */
    @Test  // JUnit marks this as a test to execute
    public void testReadingEndpoint_AdminRole_ShouldSucceed() throws Exception {  // throws Exception for MockMvc
        // Execute: Send GET request with admin role header
        mockMvc.perform(get("/api/buildings/B001/last-readings")  // Send GET to endpoint
                .header("X-User-Role", "admin"))  // Add admin role header
        
                // Assert: Verify response
                .andExpect(status().isNoContent())  // Expect HTTP 204 No Content
                .andExpect(status().is(204));  // Additional assertion: status code is 204
    }

    /**
     * Test: Reading API with Owner accessing own building should succeed
     * Endpoint: GET /api/buildings/B001/last-readings
     * Expected: HTTP 204 No Content (authorized, but no readings)
     */
    @Test  // JUnit marks this as a test to execute
    public void testReadingEndpoint_OwnerRole_OwnBuilding_ShouldSucceed() throws Exception {
        // Execute: Send GET request with owner role header for B001
        mockMvc.perform(get("/api/buildings/B001/last-readings")  // Send GET to endpoint
                .header("X-User-Role", "owner"))  // Add owner role header
        
                // Assert: Verify response
                .andExpect(status().isNoContent())  // Expect HTTP 204 No Content
                .andExpect(status().is(204));  // Additional assertion: status code is 204
    }

    /**
     * Test: Reading API with Owner accessing different building should fail
     * Endpoint: GET /api/buildings/B002/last-readings
     * Expected: HTTP 403 Forbidden (RBAC blocks access)
     */
    @Test  // JUnit marks this as a test to execute
    public void testReadingEndpoint_OwnerRole_DifferentBuilding_ShouldFail() throws Exception {
        // Execute: Send GET request with owner role header for B002 (not owned by this owner)
        mockMvc.perform(get("/api/buildings/B002/last-readings")  // Send GET to B002 endpoint
                .header("X-User-Role", "owner"))  // Add owner role header
        
                // Assert: Verify response is forbidden
                .andExpect(status().isForbidden())  // Expect HTTP 403 Forbidden
                .andExpect(status().is(403))  // Additional assertion: status code is 403
                .andExpect(jsonPath("$.error", containsString("Forbidden")));  // JSON should contain "Forbidden"
    }

    /**
     * Test: Reading API without role header should fail
     * Endpoint: GET /api/buildings/B001/last-readings
     * Expected: HTTP 403 Forbidden (no authentication)
     */
    @Test  // JUnit marks this as a test to execute
    public void testReadingEndpoint_NoRole_ShouldFail() throws Exception {
        // Execute: Send GET request WITHOUT role header (defaults to "guest")
        mockMvc.perform(get("/api/buildings/B001/last-readings"))  // Send GET without role header
        
                // Assert: Verify response is forbidden
                .andExpect(status().isForbidden())  // Expect HTTP 403 Forbidden
                .andExpect(status().is(403))  // Additional assertion: status code is 403
                .andExpect(jsonPath("$.error", containsString("Unauthorized")));  // JSON should contain "Unauthorized"
    }


    // ===== FORECAST ENDPOINT INTEGRATION TESTS =====
    
    /**
     * Test: Forecast API with Admin role should succeed
     * Endpoint: GET /api/buildings/B001/forecast?minutes=60
     * Expected: HTTP 500 (because no readings in database, but authorization works)
     *           OR HTTP 200 if forecast data exists
     * Key: We're testing authorization, not forecast calculation
     */
    @Test  // JUnit marks this as a test to execute
    public void testForecastEndpoint_AdminRole_ShouldSucceed() throws Exception {
        // Execute: Send GET request with admin role header
        MvcResult result = mockMvc.perform(get("/api/buildings/B001/forecast")  // Send GET to forecast endpoint
                .header("X-User-Role", "admin"))  // Add admin role header
        
                // Assert: Verify authorization passed (could be 200 or 500 depending on data)
                // We're NOT checking 204, because forecast returns JSON response (200 or 500)
                .andReturn();  // Capture result for inspection
        
        // Verify: Response is NOT 403 (authorization passed)
        int statusCode = result.getResponse().getStatus();  // Get HTTP status code
        assert statusCode != 403 : "Admin should be authorized (not 403)";  // Admin must not be forbidden
    }

    /**
     * Test: Forecast API with Owner accessing own building should succeed
     * Endpoint: GET /api/buildings/B001/forecast
     * Expected: HTTP 500 (no readings) or HTTP 200 (with forecast data)
     */
    @Test  // JUnit marks this as a test to execute
    public void testForecastEndpoint_OwnerRole_OwnBuilding_ShouldSucceed() throws Exception {
        // Execute: Send GET request with owner role header for B001
        MvcResult result = mockMvc.perform(get("/api/buildings/B001/forecast")  // Send GET to forecast endpoint
                .header("X-User-Role", "owner"))  // Add owner role header
        
                // Assert: Verify authorization passed
                .andReturn();  // Capture result for inspection
        
        // Verify: Response is NOT 403 (authorization passed)
        int statusCode = result.getResponse().getStatus();  // Get HTTP status code
        assert statusCode != 403 : "Owner of B001 should be authorized (not 403)";  // Owner of B001 must not be forbidden
    }

    /**
     * Test: Forecast API with Owner accessing different building should fail
     * Endpoint: GET /api/buildings/B002/forecast
     * Expected: HTTP 403 Forbidden (RBAC blocks)
     */
    @Test  // JUnit marks this as a test to execute
    public void testForecastEndpoint_OwnerRole_DifferentBuilding_ShouldFail() throws Exception {
        // Execute: Send GET request with owner role header for B002 (not owned by owner)
        mockMvc.perform(get("/api/buildings/B002/forecast")  // Send GET to B002 forecast endpoint
                .header("X-User-Role", "owner"))  // Add owner role header
        
                // Assert: Verify response is forbidden
                .andExpect(status().isForbidden())  // Expect HTTP 403 Forbidden
                .andExpect(status().is(403))  // Additional assertion: status code is 403
                .andExpect(jsonPath("$.error", containsString("Forbidden")));  // JSON should contain "Forbidden"
    }

    /**
     * Test: Forecast API without role header should fail
     * Endpoint: GET /api/buildings/B001/forecast
     * Expected: HTTP 403 Forbidden (no authentication)
     */
    @Test  // JUnit marks this as a test to execute
    public void testForecastEndpoint_NoRole_ShouldFail() throws Exception {
        // Execute: Send GET request WITHOUT role header (defaults to "guest")
        mockMvc.perform(get("/api/buildings/B001/forecast"))  // Send GET without role header
        
                // Assert: Verify response is forbidden
                .andExpect(status().isForbidden())  // Expect HTTP 403 Forbidden
                .andExpect(status().is(403))  // Additional assertion: status code is 403
                .andExpect(jsonPath("$.error", containsString("Unauthorized")));  // JSON should contain "Unauthorized"
    }


    // ===== CROSS-ENDPOINT INTEGRATION TESTS =====
    
    /**
     * Test: Both endpoints respond consistently with same role
     * Verifies: Same RBAC logic applies across all endpoints
     */
    @Test  // JUnit marks this as a test to execute
    public void testBothEndpoints_SameRole_ConsistentBehavior() throws Exception {
        // Test 1: Admin should access both endpoints
        mockMvc.perform(get("/api/buildings/B001/last-readings")  // Test reading endpoint
                .header("X-User-Role", "admin"))
                .andExpect(status().isNoContent());  // Should succeed (204)
        
        // Test 2: Admin should access forecast endpoint
        MvcResult forecastResult = mockMvc.perform(get("/api/buildings/B001/forecast")  // Test forecast endpoint
                .header("X-User-Role", "admin"))
                .andReturn();
        
        // Verify: Forecast response is not 403
        assert forecastResult.getResponse().getStatus() != 403;  // Should NOT be forbidden
    }
}
