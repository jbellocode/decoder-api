package com.decoder.decoder_api.controller; 
// This class is part of the API controller layer — handles HTTP requests.

// tool Imports
import com.decoder.decoder_api.dto.ReadingRequest; // The shape of the data the client must send us
import com.decoder.decoder_api.service.IngestService; // The service that saves readings to the database
import jakarta.validation.Valid; // Makes sure the incoming data follows validation rules
import org.springframework.beans.factory.annotation.Autowired; // Lets Spring give us a ready IngestService
import org.springframework.http.ResponseEntity; // Used to send proper HTTP responses back to the client
import org.springframework.web.bind.annotation.*; // Enables Spring Web annotations like @RestController, @PostMapping

/**
 * API endpoint for inserting new sensor readings into the system.
 * Receives data → checks it's valid → passes it to the service.
 */
@RestController // Tells Spring that this class handles web requests and returns JSON/text responses
@RequestMapping("/ingest") // All endpoints here will start with "/ingest"
public class IngestController {

    @Autowired // Spring injects the IngestService instance so we can use it here
    private IngestService ingestService;

    /**
     * POST /ingest
     * Takes a reading sent in the body of the request,
     * validates it, and saves it to the database via the service.
     */
    @PostMapping // Defines this method as the POST handler for "/ingest"
    public ResponseEntity<String> ingestReading(@Valid @RequestBody ReadingRequest request) {
        // Pass the validated data to the service to save it
        ingestService.ingestReading(request);


        // Return success message with HTTP 200
        return ResponseEntity.ok("Reading ingested successfully");
    }
}
