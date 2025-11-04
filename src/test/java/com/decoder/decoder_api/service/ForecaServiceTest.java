package com.decoder.decoder_api.service;  // Test package matches production package structure



// === TESTING FRAMEWORK IMPORTS ===
import org.junit.jupiter.api.BeforeEach;   // Run method before each test (setup)
import org.junit.jupiter.api.Test;         // Mark method as test case to execute
import org.junit.jupiter.api.extension.ExtendWith;  // Enable extensions (like MockitoExtension)


// === MOCKITO MOCKING FRAMEWORK IMPORTS ===
import org.mockito.InjectMocks;            // Create real service with mock dependencies injected
import org.mockito.Mock;                   // Create fake object that records all method calls
import org.mockito.MockitoAnnotations;     // Initialize all @Mock and @InjectMocks annotations
import org.mockito.junit.jupiter.MockitoExtension;  // Extension to enable Mockito in JUnit 5 tests


// === SPRING TESTING IMPORTS ===
import org.springframework.test.context.TestPropertySource;  // Provide test properties without full Spring context


// === ASSERTION & VERIFICATION IMPORTS ===
import static org.junit.jupiter.api.Assertions.*;  // assertEquals(), assertThrows(), etc. to verify test results
import static org.mockito.Mockito.*;              // verify(), when(), times() to mock behavior and verify calls



// === PROJECT-SPECIFIC IMPORTS ===
import com.decoder.decoder_api.dto.ForecastResponse;  // Response object returned by service method
import com.decoder.decoder_api.model.Reading;         // Sensor reading entity for test data
import java.util.Arrays;                             // Create lists from array values for testing
import java.util.List;                               // Collection interface for test data



/**
 * Unit tests for ForecastService
 * Tests threshold logic: 70% threshold (ASHRAE standard)
 */
@ExtendWith(MockitoExtension.class)  // Enable Mockito annotations in this test class
@TestPropertySource(properties = "forecast.threshold=70.0")  // Provide test property - @Value will read this
class ForecastServiceTest {



    @Mock  // Create fake QueryService (won't query real database)
    private QueryService queryService;  // Mocked dependency



    @InjectMocks  // Create real ForecastService with mock injected
    private ForecastService forecastService;  // Service under test



    @BeforeEach  // Run this before EACH test method
    void setUp() {  // Setup method name
        MockitoAnnotations.openMocks(this);  // Initialize all @Mock and @InjectMocks annotations
        
        // CRITICAL FIX: Manually set threshold value using reflection (required for @InjectMocks with @Value)
        try {
            // Use reflection to access the private thresholdValue field
            java.lang.reflect.Field thresholdField = ForecastService.class.getDeclaredField("thresholdValue");  // Get the field
            thresholdField.setAccessible(true);  // Allow access to private field
            thresholdField.set(forecastService, 70.0);  // Manually set threshold to 70.0 for testing
        } catch (NoSuchFieldException | IllegalAccessException e) {  // Catch reflection errors
            throw new RuntimeException("Failed to set threshold value", e);  // Fail with clear error message
        }
    }



    @Test  // This is a test case
    void testGetForecast_BelowThreshold() {  // Test name describes what's being tested
        // === ARRANGE: Create test data ===
        Reading r1 = new Reading();  // Create first reading object
        r1.setReadingValue(50.0);  // Set value to 50 (below 70 threshold)
        
        Reading r2 = new Reading();  // Create second reading object
        r2.setReadingValue(55.0);  // Set value to 55 (below threshold)
        
        Reading r3 = new Reading();  // Create third reading object
        r3.setReadingValue(52.0);  // Set value to 52 (below threshold)
        // Average: (50+55+52)/3 = 52.33 → below 70 threshold
        
        // Tell mock QueryService what to return
        when(queryService.getLastReadings("B001", 120))  // When this method is called
            .thenReturn(Arrays.asList(r1, r2, r3));  // Return our test readings (not real database)
        
        // === ACT: Call the method being tested ===
        ForecastResponse response = forecastService.getForecast("B001", 120);  // Call method with test data
        
        // === ASSERT: Verify the results ===
        assertEquals("Normal operation", response.getRecommendation());  // Assert recommendation is correct (52.33 < 70)
        verify(queryService, times(1)).getLastReadings("B001", 120);  // Verify QueryService was called exactly once
    }



    @Test  // This is a test case
    void testGetForecast_AboveThreshold() {  // Test name: above threshold triggers energy-saving
        // === ARRANGE: Create test data with HIGH values ===
        Reading r1 = new Reading();  // Create first reading
        r1.setReadingValue(80.0);  // Set value to 80 (ABOVE 70 threshold)
        
        Reading r2 = new Reading();  // Create second reading
        r2.setReadingValue(85.0);  // Set value to 85 (ABOVE threshold)
        
        Reading r3 = new Reading();  // Create third reading
        r3.setReadingValue(82.0);  // Set value to 82 (ABOVE threshold)
        // Average: (80+85+82)/3 = 82.33 → above 70 threshold
        
        // Tell mock QueryService what to return
        when(queryService.getLastReadings("B001", 120))  // When method called with these params
            .thenReturn(Arrays.asList(r1, r2, r3));  // Return high-value test readings
        
        // === ACT: Call the method being tested ===
        ForecastResponse response = forecastService.getForecast("B001", 120);  // Call with test data
        
        // === ASSERT: Verify the results ===
        assertEquals("Activate energy-saving mode", response.getRecommendation());  // Assert savings triggered (82.33 > 70)
        verify(queryService, times(1)).getLastReadings("B001", 120);  // Verify QueryService called once
    }



    @Test  // This is a test case
    void testGetForecast_NoReadings() {  // Test name: error handling when no data available
        // === ARRANGE: Setup mock to return empty list ===
        when(queryService.getLastReadings("B999", 120))  // When method called with building B999
            .thenReturn(Arrays.asList());  // Return empty list (no readings available)
        
        // === ACT & ASSERT: Verify exception is thrown ===
        assertThrows(RuntimeException.class, () ->   // Expect RuntimeException to be thrown
            forecastService.getForecast("B999", 120));  // When calling method with no data
        
        // Verify QueryService was called (even though exception occurred after)
        verify(queryService, times(1)).getLastReadings("B999", 120);  // Verify method was attempted
    }
}
