package com.noprobit.analyzers.analyzers.config;

import com.noprobit.analyzers.analyzers.model.MappingRule;
import com.noprobit.analyzers.analyzers.model.PurposeType;
import com.noprobit.analyzers.analyzers.engine.JsonConfiguredEngine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Purpose Mapping Loader
 * Loads purpose mappings from JSON configuration files
 */
public class PurposeMappingLoader {

    /**
     * Load engines from JSON configuration file
     */
    public static List<JsonConfiguredEngine> loadFromFile(String configPath) throws IOException {
        List<JsonConfiguredEngine> engines = new ArrayList<>();

        Path path = Paths.get(configPath);
        if (!Files.exists(path)) {
            System.err.println("Configuration file not found: " + configPath);
            return engines;
        }

        String jsonContent = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        JsonObject config = JsonParser.parseString(jsonContent).getAsJsonObject();

        if (config.has("engines")) {
            JsonArray enginesArray = config.getAsJsonArray("engines");
            for (JsonElement element : enginesArray) {
                JsonObject engineObj = element.getAsJsonObject();
                JsonConfiguredEngine engine = parseEngine(engineObj);
                engines.add(engine);
            }
        }

        return engines;
    }

    /**
     * Load engines from JSON string
     */
    public static List<JsonConfiguredEngine> loadFromString(String jsonContent) {
        List<JsonConfiguredEngine> engines = new ArrayList<>();

        try {
            JsonObject config = JsonParser.parseString(jsonContent).getAsJsonObject();

            if (config.has("engines")) {
                JsonArray enginesArray = config.getAsJsonArray("engines");
                for (JsonElement element : enginesArray) {
                    JsonObject engineObj = element.getAsJsonObject();
                    JsonConfiguredEngine engine = parseEngine(engineObj);
                    engines.add(engine);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse JSON configuration: " + e.getMessage());
        }

        return engines;
    }

    /**
     * Load engines from classpath resource
     */
    public static List<JsonConfiguredEngine> loadFromClasspath(String resourcePath) throws IOException {
        InputStream inputStream = PurposeMappingLoader.class.getClassLoader()
            .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        return loadFromString(jsonContent);
    }

    /**
     * Parse a single engine from JSON object
     */
    private static JsonConfiguredEngine parseEngine(JsonObject engineObj) {
        String name = engineObj.get("engineName").getAsString();
        int priority = engineObj.get("priority").getAsInt();

        JsonConfiguredEngine engine = new JsonConfiguredEngine(name, priority);

        if (engineObj.has("mappings")) {
            JsonArray mappingsArray = engineObj.getAsJsonArray("mappings");
            for (JsonElement element : mappingsArray) {
                JsonObject mappingObj = element.getAsJsonObject();
                MappingRule rule = parseRule(mappingObj);
                engine.addRule(rule);
            }
        }

        return engine;
    }

    /**
     * Parse a single mapping rule from JSON object
     */
    private static MappingRule parseRule(JsonObject ruleObj) {
        String pattern = ruleObj.get("pattern").getAsString();
        PurposeType purpose = PurposeType.valueOf(ruleObj.get("purpose").getAsString());
        double confidence = ruleObj.get("confidence").getAsDouble();

        return new MappingRule(pattern, purpose, confidence);
    }

    /**
     * Merge multiple configuration files
     */
    public static List<JsonConfiguredEngine> mergeConfigurations(String... configPaths) throws IOException {
        Map<String, JsonConfiguredEngine> engineMap = new LinkedHashMap<>();

        for (String configPath : configPaths) {
            List<JsonConfiguredEngine> engines = loadFromFile(configPath);
            for (JsonConfiguredEngine engine : engines) {
                // Merge engines with same name
                if (engineMap.containsKey(engine.getName())) {
                    // Update priority to highest
                    JsonConfiguredEngine existing = engineMap.get(engine.getName());
                    if (engine.getPriority() > existing.getPriority()) {
                        engineMap.put(engine.getName(), engine);
                    }
                } else {
                    engineMap.put(engine.getName(), engine);
                }
            }
        }

        List<JsonConfiguredEngine> result = new ArrayList<>(engineMap.values());
        result.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        return result;
    }
}
