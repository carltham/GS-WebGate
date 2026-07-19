package com.noprobit.analyzers.webgate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/api/verify-purpose")
public class PurposeVerificationController {

    private final Gson gson = new Gson();
    private final InternetSearchService searchService = new InternetSearchService();

    @PostMapping
    public ResponseEntity<String> verifyPurpose(@RequestBody String payload) {
        try {
            JsonObject request = JsonParser.parseString(payload).getAsJsonObject();

            String className = request.get("className").getAsString();
            String detectedPurpose = request.get("detectedPurpose").getAsString();
            String keyword = request.get("keyword").getAsString();
            String timestamp = request.get("timestamp").getAsString();

            String searchQuery = className + " " + keyword + " " + detectedPurpose;
            SearchResult result = searchService.search(searchQuery);

            JsonObject response = new JsonObject();
            response.addProperty("className", className);
            response.addProperty("detectedPurpose", detectedPurpose);
            response.addProperty("keyword", keyword);
            response.addProperty("verified", Boolean.valueOf(result.isRelevant()));
            response.addProperty("reason", result.getReason());
            response.addProperty("internetSource", result.getSource());
            response.addProperty("confidence", Double.valueOf(result.getConfidence()));
            response.addProperty("processingTime", Long.valueOf(result.getProcessingTime()));
            response.addProperty("timestamp", timestamp);

            return ResponseEntity.ok(response.toString());

        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            error.addProperty("verified", Boolean.FALSE);
            error.addProperty("reason", "Verification failed: " + e.getMessage());
            error.addProperty("internetSource", (String) null);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        JsonObject response = new JsonObject();
        response.addProperty("status", "UP");
        response.addProperty("service", "PurposeVerification");
        response.addProperty("version", "1.0");

        return ResponseEntity.ok(response.toString());
    }
}
