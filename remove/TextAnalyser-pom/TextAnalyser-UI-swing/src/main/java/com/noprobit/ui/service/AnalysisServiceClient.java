package com.noprobit.analyzers.ui.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AnalysisServiceClient {
    private static final String DEFAULT_JAR_SERVICE_URL = "http://localhost:8081/analysis";
    private final String jarServiceUrl;
    private final Gson gson = new Gson();

    public AnalysisServiceClient() {
        this(System.getenv("JAR_SERVICE_URL") != null ?
            System.getenv("JAR_SERVICE_URL") : DEFAULT_JAR_SERVICE_URL);
    }

    public AnalysisServiceClient(String jarServiceUrl) {
        this.jarServiceUrl = jarServiceUrl;
    }

    public AnalysisResponse analyze(String className, String extendsClass) {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("className", className);
            request.addProperty("extendsClass", extendsClass != null ? extendsClass : "");

            String response = callJarService("/analyze", request.toString());

            if (response != null) {
                JsonObject result = JsonParser.parseString(response).getAsJsonObject();
                return new AnalysisResponse(
                    result.get("actualName").getAsString(),
                    result.get("suggestedName").getAsString(),
                    result.get("purpose").getAsString(),
                    result.get("extendsClass").getAsString(),
                    true
                );
            }
        } catch (Exception e) {
            System.err.println("Analysis service call failed: " + e.getMessage());
        }

        return new AnalysisResponse(className, className, "UNKNOWN", extendsClass, false);
    }

    public boolean isServiceAvailable() {
        try {
            URL url = new URL(jarServiceUrl + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }

    private String callJarService(String endpoint, String jsonPayload) {
        try {
            URL url = new URL(jarServiceUrl + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            }
        } catch (Exception e) {
            System.err.println("Jar service call failed: " + e.getMessage());
        }
        return null;
    }

    public static class AnalysisResponse {
        public final String actualName;
        public final String suggestedName;
        public final String purpose;
        public final String extendsClass;
        public final boolean success;

        public AnalysisResponse(String actualName, String suggestedName, String purpose,
                               String extendsClass, boolean success) {
            this.actualName = actualName;
            this.suggestedName = suggestedName;
            this.purpose = purpose;
            this.extendsClass = extendsClass;
            this.success = success;
        }
    }
}
