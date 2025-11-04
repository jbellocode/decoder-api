package com.decoder.decoder_api.service;

// === PROJECT-SPECIFIC IMPORTS ===
import com.decoder.decoder_api.dto.ReadingRequest;             // DTO containing incoming sensor reading data from API requests
import com.decoder.decoder_api.model.Building;                 // Entity representing buildings in the system
import com.decoder.decoder_api.model.Reading;                  // Entity representing sensor readings
import com.decoder.decoder_api.repository.BuildingRepository;  // Repository for building database operations
import com.decoder.decoder_api.repository.ReadingRepository;   // Repository for reading database operations

// === JUNIT 5 TESTING FRAMEWORK IMPORTS ===
import org.junit.jupiter.api.BeforeEach;                       // Setup method annotation
import org.junit.jupiter.api.Test;                             // Test method annotation

// === MOCKITO MOCKING FRAMEWORK IMPORTS ===
import org.mockito.InjectMocks;                                // Creates real service with mocked dependencies
import org.mockito.Mock;                                       // Creates mock objects
import org.mockito.MockitoAnnotations;                         // Initializes mocks

// === JAVA STANDARD LIBRARY IMPORTS ===
import java.time.LocalDateTime;                                // Date-time handling
import java.util.Optional;                                     // Container that may or may not contain a value (used by JPA)

// === JUNIT 5 ASSERTION IMPORTS ===
import static org.junit.jupiter.api.Assertions.*;              // Assertion methods

// === MOCKITO IMPORTS ===
import static org.mockito.ArgumentMatchers.any;                // Argument matcher for any value
import static org.mockito.Mockito.*;                           // All Mockito methods: when(), verify(), times()

/**
 * Unit tests for IngestService
 * 
 * PURPOSE: Validates data ingestion logic and error handling
 * 
 * testing strategy:
 * - Tests successful reading ingestion
 * - Tests error handling for invalid building IDs
 * - Uses Mockito verify() to ensure repository methods are called
 * 
 * Main purpose:
 * - "Tests both happy path and error scenarios"
 * - "Uses verify() to confirm side effects (database saves)"
 * - "Validates business rule: building must exist before accepting readings"
 */
class IngestServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @InjectMocks
    private IngestService ingestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * TEST: Successful reading ingestion
     * 
     * SCENARIO: Valid building exists, reading is saved
     * OUTCOME: Repository save() method is called exactly once
     */
    @Test
    void testIngestReading_Success() {
        // ARRANGE
        ReadingRequest request = new ReadingRequest();
        request.setBuildingId("B001");
        request.setSensorId("TEMP_01");
         request.setTimestamp("2025-10-30T11:30:00"); 
        request.setValue(22.5);
        
        // Mock building exists in database
        Building mockBuilding = new Building();
        mockBuilding.setId("B001");
        mockBuilding.setName("Main Campus");
        
        // When service looks up building, return the mock
        when(buildingRepository.findById("B001")).thenReturn(Optional.of(mockBuilding));
        
        // When service saves reading, return the same reading (simulates database behavior)
        when(readingRepository.save(any(Reading.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // ACT
        ingestService.ingestReading(request);
        
        // ASSERT
        // Verify that save() was called exactly once with any Reading object
        verify(readingRepository, times(1)).save(any(Reading.class));
    }

    /**
     * TEST: Error handling for non-existent building
     * 
     * SCENARIO: Request references building that doesn't exist in database
     * OUTCOME: RuntimeException thrown with appropriate message
     */
    @Test
    void testIngestReading_BuildingNotFound() {
        // ARRANGE
        ReadingRequest request = new ReadingRequest();
        request.setBuildingId("INVALID");
        request.setSensorId("TEMP_01");
         request.setTimestamp("2025-10-30T11:30:00"); 
        request.setValue(22.5);
        
        // Mock: building does NOT exist (repository returns empty Optional)
        when(buildingRepository.findById("INVALID")).thenReturn(Optional.empty());
        
        // ACT & ASSERT
        // assertThrows verifies that the code throws expected exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            ingestService.ingestReading(request);
        });
        
        // Verify exception message contains "not found"
        assertTrue(exception.getMessage().contains("not found"),
                   "Exception message should indicate building was not found");
    }
}
