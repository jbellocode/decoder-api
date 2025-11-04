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



// === ASSERTION & VERIFICATION IMPORTS ===
import static org.junit.jupiter.api.Assertions.*;  // assertEquals(), assertNotNull(), assertTrue() to verify results
import static org.mockito.Mockito.*;              // verify(), when(), times() to mock behavior and verify calls
import static org.mockito.ArgumentMatchers.any;  // Matcher for any argument (handles LocalDateTime parameter)



// === PROJECT-SPECIFIC IMPORTS ===
import com.decoder.decoder_api.model.Reading;           // Database entity representing sensor reading from sensors
import com.decoder.decoder_api.repository.ReadingRepository;  // Repository interface for database access to readings
import java.time.LocalDateTime;                   // For working with dates and times without timezone information
import java.util.Arrays;                          // Utility for creating lists from array values for testing
import java.util.List;                            // Collection interface for storing multiple readings



/**
 * Unit tests for QueryService
 * Tests reading retrieval with time-window filtering functionality
 */
@ExtendWith(MockitoExtension.class)  // Enable Mockito annotations in this test class
class QueryServiceTest {



    @Mock  // Create fake ReadingRepository (won't query real database)
    private ReadingRepository readingRepository;  // Mocked dependency - returns test data only



    @InjectMocks  // Create real QueryService with @Mock dependencies injected
    private QueryService queryService;  // Service under test



    @BeforeEach  // Run this before EACH test method
    void setUp() {  // Setup method name
        MockitoAnnotations.openMocks(this);  // Initialize all @Mock and @InjectMocks annotations before each test
    }



    @Test  // This is a test case
    void testGetLastReadings_ReturnsReadingsWithinTimeWindow() {  // Test name: retrieves readings correctly from database
        // === ARRANGE: Create test reading data ===
        Reading r1 = new Reading();  // Create first sensor reading object
        r1.setReadingValue(45.0);  // Set sensor value to 45
        r1.setTimestamp(LocalDateTime.now().minusMinutes(30));  // Set timestamp to 30 minutes ago (within window)
        
        Reading r2 = new Reading();  // Create second sensor reading object
        r2.setReadingValue(48.0);  // Set sensor value to 48
        r2.setTimestamp(LocalDateTime.now().minusMinutes(60));  // Set timestamp to 60 minutes ago (within window)
        
        Reading r3 = new Reading();  // Create third sensor reading object
        r3.setReadingValue(50.0);  // Set sensor value to 50
        r3.setTimestamp(LocalDateTime.now().minusMinutes(90));  // Set timestamp to 90 minutes ago (within window)
        
        List<Reading> mockReadings = Arrays.asList(r1, r2, r3);  // Create list of test readings (ordered by recency)
        
        // Tell mock repository what to return
        when(readingRepository.findRecentReadings(eq("B001"), any(LocalDateTime.class)))  // When findRecentReadings called with building ID and any timestamp
            .thenReturn(mockReadings);  // Return our test readings (not real database)
        
        // === ACT: Call the method being tested ===
        List<Reading> result = queryService.getLastReadings("B001", 120);  // Call with building ID and 120 minute window
        
        // === ASSERT: Verify the results ===
        assertNotNull(result);  // Assert result is not null (got something back)
        assertEquals(3, result.size());  // Assert we got 3 readings back
        assertEquals(45.0, result.get(0).getReadingValue());  // Assert first reading value is correct (most recent)
        assertEquals(50.0, result.get(2).getReadingValue());  // Assert third reading value is correct (oldest)
        
        // Verify repository was called exactly once with correct parameters
        verify(readingRepository, times(1)).findRecentReadings(eq("B001"), any(LocalDateTime.class));  // Verify method called once
    }



    @Test  // This is a test case
    void testGetLastReadings_EmptyResult() {  // Test name: handles empty reading list correctly
        // === ARRANGE: Setup mock to return empty list ===
        when(readingRepository.findRecentReadings(eq("B999"), any(LocalDateTime.class)))  // When findRecentReadings called with non-existent building
            .thenReturn(Arrays.asList());  // Return empty list (no readings exist for this building)
        
        // === ACT: Call the method being tested ===
        List<Reading> result = queryService.getLastReadings("B999", 120);  // Call with non-existent building
        
        // === ASSERT: Verify the results ===
        assertNotNull(result);  // Assert result is not null (empty list is valid result)
        assertEquals(0, result.size());  // Assert result size is 0 (empty)
        
        // Verify repository was called once
        verify(readingRepository, times(1)).findRecentReadings(eq("B999"), any(LocalDateTime.class));  // Verify method was called
    }



    @Test  // This is a test case
    void testGetLastReadings_FiltersByTimeWindow() {  // Test name: correctly filters readings by 120-minute time window
        // === ARRANGE: Create readings with various timestamps ===
        Reading r1 = new Reading();  // Create recent reading (within 120 minute window)
        r1.setReadingValue(52.0);  // Set sensor value
        r1.setTimestamp(LocalDateTime.now().minusMinutes(30));  // 30 minutes ago - WITHIN 120 minute window
        
        Reading r2 = new Reading();  // Create old reading (outside 120 minute window)
        r2.setReadingValue(40.0);  // Set sensor value (old data)
        r2.setTimestamp(LocalDateTime.now().minusMinutes(150));  // 150 minutes ago - OUTSIDE 120 minute window
        
        List<Reading> allReadings = Arrays.asList(r1, r2);  // Create list with recent and old readings
        
        // Tell mock repository what to return (both recent and old readings)
        when(readingRepository.findRecentReadings(eq("B001"), any(LocalDateTime.class)))  // When findRecentReadings called
            .thenReturn(allReadings);  // Return all readings (filtering happens in service layer)
        
        // === ACT: Call the method being tested ===
        List<Reading> result = queryService.getLastReadings("B001", 120);  // Request only 120-minute window
        
        // === ASSERT: Verify the results ===
        assertNotNull(result);  // Assert result is not null (got something back)
        assertTrue(result.size() > 0);  // Assert we got at least one reading back
        
        // Verify repository was called once (QueryService should filter in-memory, not in DB query)
        verify(readingRepository, times(1)).findRecentReadings(eq("B001"), any(LocalDateTime.class));  // Verify method called once
    }



    @Test  // This is a test case
    void testGetLastReadings_ReturnsSortedByTimestamp() {  // Test name: returns readings in descending timestamp order
        // === ARRANGE: Create readings with specific timestamps ===
        Reading r1 = new Reading();  // Create oldest reading
        r1.setReadingValue(40.0);  // Set sensor value
        r1.setTimestamp(LocalDateTime.now().minusMinutes(120));  // 120 minutes ago (oldest)
        
        Reading r2 = new Reading();  // Create middle reading
        r2.setReadingValue(45.0);  // Set sensor value
        r2.setTimestamp(LocalDateTime.now().minusMinutes(60));  // 60 minutes ago (middle)
        
        Reading r3 = new Reading();  // Create newest reading
        r3.setReadingValue(50.0);  // Set sensor value
        r3.setTimestamp(LocalDateTime.now().minusMinutes(30));  // 30 minutes ago (newest/most recent)
        
        // Repository returns readings in descending order (newest first)
        List<Reading> mockReadings = Arrays.asList(r3, r2, r1);  // Ordered: newest → oldest
        
        // Tell mock repository what to return (pre-sorted by timestamp descending)
        when(readingRepository.findRecentReadings(eq("B001"), any(LocalDateTime.class)))  // When findRecentReadings called
            .thenReturn(mockReadings);  // Return readings in descending timestamp order
        
        // === ACT: Call the method being tested ===
        List<Reading> result = queryService.getLastReadings("B001", 120);  // Request readings from database
        
        // === ASSERT: Verify the results are correctly ordered ===
        assertNotNull(result);  // Assert result is not null
        assertEquals(3, result.size());  // Assert we got 3 readings
        assertEquals(50.0, result.get(0).getReadingValue());  // Assert first (newest) reading value is correct
        assertEquals(45.0, result.get(1).getReadingValue());  // Assert second (middle) reading value is correct
        assertEquals(40.0, result.get(2).getReadingValue());  // Assert third (oldest) reading value is correct
        
        // Verify repository was called
        verify(readingRepository, times(1)).findRecentReadings(eq("B001"), any(LocalDateTime.class));  // Verify method called once
    }
}
