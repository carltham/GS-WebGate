package com.noprobit.analyzers.analyzers;

import com.noprobit.analyzers.reporters.ClassNameSuggester;
import com.noprobit.analyzers.validators.ClassNameValidator;
import com.noprobit.analyzers.analyzers.model.*;
import com.noprobit.analyzers.analyzers.engine.JsonConfiguredEngine;
import com.noprobit.analyzers.analyzers.remote.RemoteVerificationResult;
import com.noprobit.analyzers.analyzers.config.PurposeMappingLoader;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Purpose Analyzer
 * Analyzes class/function names and purposes using JSON-configured engines
 * Supports local analysis and remote verification via WebGate
 */
public class PurposeAnalyser {
    private final List<JsonConfiguredEngine> configuredEngines = new ArrayList<>();
    private final ClassNameValidator validator = new ClassNameValidator();
    private final ClassNameSuggester suggester = new ClassNameSuggester();
    private final Map<String, PurposeType> learnedPatterns = new HashMap<>();
    private final List<UnknownPattern> unknownPatterns = new ArrayList<>();
    private final Path logDir = Paths.get("logs");
    private final Path purposeLogFile = logDir.resolve("purpose-analysis.log");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // REST Gateway Configuration
    private String webgateUrl = "http://localhost:8080/webgate/api";
    private boolean enableRemoteVerification = true;
    private final Gson gson = new Gson();

    public PurposeAnalyser() {
        initializeLogDirectory();
        loadDefaultConfiguration();
    }

    /**
     * Load default configuration from classpath
     */
    private void loadDefaultConfiguration() {
        try {
            List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");
            loadConfiguration(engines);
            System.out.println("Loaded " + engines.size() + " purpose mapping engines from classpath");
        } catch (Exception e) {
            System.err.println("Failed to load default configuration: " + e.getMessage());
        }
    }

    /**
     * Initialize log directory
     */
    private void initializeLogDirectory() {
        try {
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
        } catch (IOException e) {
            System.err.println("Failed to create logs directory: " + e.getMessage());
        }
    }

    /**
     * Load configuration from JSON-configured engines
     */
    public void loadConfiguration(List<JsonConfiguredEngine> engines) {
        this.configuredEngines.clear();
        this.configuredEngines.addAll(engines);
        this.configuredEngines.sort(Comparator.comparingInt(JsonConfiguredEngine::getPriority).reversed());
    }

    /**
     * Load configuration from file
     */
    public void loadConfigurationFromFile(String filePath) throws Exception {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromFile(filePath);
        loadConfiguration(engines);
    }

    /**
     * Load configuration from JSON string
     */
    public void loadConfigurationFromString(String jsonContent) {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromString(jsonContent);
        loadConfiguration(engines);
    }

    /**
     * Load configuration from classpath resource
     */
    public void loadConfigurationFromClasspath(String resourcePath) throws Exception {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath(resourcePath);
        loadConfiguration(engines);
    }

    /**
     * Reload configuration (useful for hot-reload scenarios)
     */
    public void reloadConfiguration() {
        loadDefaultConfiguration();
    }

    /**
     * Analyze text using JSON-configured engines with learning
     */
    public PurposeMatch analyse(String rawText) {
        if (rawText == null || rawText.strip().isEmpty()) {
            return new PurposeMatch(PurposeType.UNKNOWN, 0.0, "System");
        }

        String lower = rawText.toLowerCase();

        // Check learned patterns first
        if (learnedPatterns.containsKey(lower)) {
            return new PurposeMatch(learnedPatterns.get(lower), 0.95, "Learned");
        }

        // Check JSON-configured engines
        for (JsonConfiguredEngine engine : configuredEngines) {
            Optional<PurposeMatch> match = engine.evaluate(rawText);
            if (match.isPresent()) {
                return match.get();
            }
        }

        // Track unknown
        trackUnknownPattern(rawText);
        return new PurposeMatch(PurposeType.UNKNOWN, 0.10, "Fallback");
    }

    /**
     * Teach the analyzer a new pattern
     */
    public void learnPattern(String pattern, PurposeType purpose) {
        learnedPatterns.put(pattern.toLowerCase(), purpose);
    }

    /**
     * Track unknown patterns for analysis and log to file
     */
    private void trackUnknownPattern(String pattern) {
        String lower = pattern.toLowerCase();
        boolean exists = unknownPatterns.stream()
            .anyMatch(up -> up.pattern.equals(lower));
        if (!exists) {
            unknownPatterns.add(new UnknownPattern(lower, PurposeType.UNKNOWN));
            logUnknownPattern(pattern);
        }
    }

    /**
     * Log comprehensive analysis to file with actual and suggested names
     */
    public void logAnalysis(String actualName, String suggestedName, String purpose, String extendsClass) {
        try {
            String timestamp = LocalDateTime.now().format(dateFormat);
            String logEntry = String.format(
                "[%s] ANALYSIS | Actual: %s | Suggested: %s | Purpose: %s | Extends: %s%n",
                timestamp, actualName, suggestedName, purpose,
                extendsClass != null ? extendsClass : "N/A"
            );
            Files.write(purposeLogFile, logEntry.getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to log analysis: " + e.getMessage());
        }
    }

    /**
     * Get path to purpose analysis log file
     */
    public Path getPurposeLogFile() {
        return purposeLogFile;
    }

    /**
     * Set WebGate URL for remote verification
     */
    public void setWebgateUrl(String url) {
        this.webgateUrl = url;
    }

    /**
     * Enable/disable remote internet verification
     */
    public void setRemoteVerificationEnabled(boolean enabled) {
        this.enableRemoteVerification = enabled;
    }

    /**
     * Verify purpose with remote WebGate
     */
    public RemoteVerificationResult verifyPurposeRemote(String className, String detectedPurpose, String keyword) {
        if (!enableRemoteVerification) {
            return new RemoteVerificationResult(false, "Remote verification disabled", null);
        }

        try {
            JsonObject request = new JsonObject();
            request.addProperty("className", className);
            request.addProperty("detectedPurpose", detectedPurpose);
            request.addProperty("keyword", keyword);
            request.addProperty("timestamp", LocalDateTime.now().format(dateFormat));

            String response = callWebGateAPI("/verify-purpose", request.toString());

            if (response != null) {
                JsonObject result = JsonParser.parseString(response).getAsJsonObject();
                return new RemoteVerificationResult(
                    result.get("verified").getAsBoolean(),
                    result.get("reason").getAsString(),
                    result.get("internetSource").getAsString()
                );
            }
        } catch (Exception e) {
            System.err.println("Remote verification failed: " + e.getMessage());
        }

        return new RemoteVerificationResult(false, "Remote verification failed", null);
    }

    /**
     * Call WebGate REST API
     */
    private String callWebGateAPI(String endpoint, String jsonPayload) {
        try {
            URL url = new URL(webgateUrl + endpoint);
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
            System.err.println("WebGate API call failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all unknown patterns encountered
     */
    public List<UnknownPattern> getUnknownPatterns() {
        return new ArrayList<>(unknownPatterns);
    }

    /**
     * Clear learned patterns
     */
    public void clearLearned() {
        learnedPatterns.clear();
    }

    /**
     * Get all learned patterns
     */
    public Map<String, PurposeType> getLearned() {
        return new HashMap<>(learnedPatterns);
    }

    /**
     * Comprehensive analysis with logging
     */
    public AnalysisResult analyzeAndLog(String className, String extendsClass) {
        String purpose = analyzePurpose(className, extendsClass);
        String suggestedName = suggestName(className, extendsClass);

        logAnalysis(className, suggestedName, purpose, extendsClass);

        return new AnalysisResult(className, suggestedName, purpose, extendsClass);
    }

    /**
     * Analyze class purpose based on name and parent class
     */
    public String analyzePurpose(String className, String extendsClass) {
        String lower = className.toLowerCase();

        if (extendsClass != null) {
            String extendsLower = extendsClass.toLowerCase();

            if (extendsLower.contains("editorpanel"))
                return "Management panel for " + extractEntity(className) + " with editor";
            if (extendsLower.contains("editorrecord"))
                return "Editor for " + extractEntity(className) + " records";
            if (extendsLower.contains("panel"))
                return "UI panel/view component";
            if (extendsLower.contains("dialog"))
                return "Modal dialog window";
            if (extendsLower.contains("editor"))
                return "Editor component";
            if (extendsLower.contains("screen"))
                return "Screen/display component";
        }

        if (lower.contains("panel")) return "Panel/View component";
        if (lower.contains("dialog")) return "Dialog window";
        if (lower.contains("editor")) return "Editor component";
        if (lower.contains("screen")) return "Screen/Display";
        if (lower.contains("controller")) return "Controller component";
        if (lower.contains("worker")) return "Worker component";
        if (lower.contains("validator")) return "Validator component";
        if (lower.contains("exporter")) return "Exporter component";
        if (lower.contains("event")) return "Event component";
        if (lower.contains("listener")) return "Listener component";

        return "Component/Helper";
    }

    /**
     * Suggest improved name
     */
    public String suggestName(String currentName, String extendsClass) {
        return suggester.suggest(currentName, extendsClass).suggestedName;
    }

    /**
     * Validate PascalCase
     */
    public boolean isPascalCase(String className) {
        return validator.isPascalCase(className);
    }

    /**
     * Extract entity name from class name
     */
    private String extractEntity(String className) {
        String lower = className.toLowerCase();

        if (lower.contains("category")) return "Category";
        if (lower.contains("product")) return "Product";
        if (lower.contains("cashier")) return "Cashier";
        if (lower.contains("resource")) return "Resource";
        if (lower.contains("people")) return "Person";
        if (lower.contains("supplier")) return "Supplier";
        if (lower.contains("tax")) return "Tax";
        if (lower.contains("printer")) return "Printer";
        if (lower.contains("layer")) return "Layer Handling";
        if (lower.contains("register")) return "Registration";

        return className;
    }

    /**
     * Log unknown pattern to file
     */
    private void logUnknownPattern(String pattern) {
        try {
            String timestamp = LocalDateTime.now().format(dateFormat);
            String logEntry = String.format("[%s] UNKNOWN: %s%n", timestamp, pattern);
            Files.write(purposeLogFile, logEntry.getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to log unknown pattern: " + e.getMessage());
        }
    }
}
