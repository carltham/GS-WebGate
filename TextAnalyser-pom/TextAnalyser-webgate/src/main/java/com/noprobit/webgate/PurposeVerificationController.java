package com.noprobit.webgate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

@RestController
@RequestMapping("/api")
public class PurposeVerificationController {

    private final Gson gson = new Gson();

    @Autowired
    private InternetSearchService searchService;

    @PostMapping("/verify-purpose")
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

    @PostMapping("/query")
    public ResponseEntity<String> queryGeneric(@RequestBody QueryRequest request) {
        try {
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(createErrorResponse("Question cannot be empty")));
            }

            QueryResponse queryResponse = searchService.queryGeneric(request);

            JsonObject response = new JsonObject();
            response.addProperty("question", queryResponse.getQuestion());
            response.addProperty("answerFound", queryResponse.isAnswerFound());
            response.addProperty("answer", queryResponse.getAnswer());
            response.addProperty("confidence", queryResponse.getConfidence());
            response.addProperty("summary", queryResponse.getSummary());
            response.addProperty("processingTime", queryResponse.getProcessingTime());

            JsonArray sourcesArray = new JsonArray();
            for (String source : queryResponse.getSources()) {
                sourcesArray.add(source);
            }
            response.add("sources", sourcesArray);

            return ResponseEntity.ok(response.toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(gson.toJson(createErrorResponse("Query failed: " + e.getMessage())));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        JsonObject response = new JsonObject();
        response.addProperty("status", "UP");
        response.addProperty("service", "WebGate");
        response.addProperty("version", "2.0");
        response.addProperty("features", "Purpose Verification + Generic Queries");

        return ResponseEntity.ok(response.toString());
    }

    private JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        error.addProperty("success", false);
        return error;
    }
}
