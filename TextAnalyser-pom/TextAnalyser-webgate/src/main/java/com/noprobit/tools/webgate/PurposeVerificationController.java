package com.noprobit.tools.webgate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * REST Controller for Purpose Verification
 * Handles requests from PurposeAnalyser to verify purposes via internet search
 */
@Path("/verify-purpose")
public class PurposeVerificationController {

    private final Gson gson = new Gson();
    private final InternetSearchService searchService = new InternetSearchService();

    /**
     * Verify purpose via internet search
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyPurpose(String payload) {
        try {
            JsonObject request = JsonParser.parseString(payload).getAsJsonObject();

            String className = request.get("className").getAsString();
            String detectedPurpose = request.get("detectedPurpose").getAsString();
            String keyword = request.get("keyword").getAsString();
            String timestamp = request.get("timestamp").getAsString();

            // Search internet for keywords related to purpose
            String searchQuery = className + " " + keyword + " " + detectedPurpose;
            SearchResult result = searchService.search(searchQuery);

            // Build response
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

            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            error.addProperty("verified", Boolean.FALSE);
            error.addProperty("reason", "Verification failed: " + e.getMessage());
            error.addProperty("internetSource", (String) null);

            return Response.status(Response.Status.BAD_REQUEST)
                .entity(error.toString())
                .build();
        }
    }

    /**
     * Health check endpoint
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        JsonObject response = new JsonObject();
        response.addProperty("status", "UP");
        response.addProperty("service", "PurposeVerification");
        response.addProperty("version", "1.0");

        return Response.ok(response.toString()).build();
    }
}
