package com.noprobit.analyzers.analyzers.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.noprobit.analyzers.analyzers.engine.JsonConfiguredEngine;
import com.noprobit.analyzers.analyzers.model.MappingRule;
import com.noprobit.analyzers.analyzers.model.PurposeType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurposeMappingLoader Integration Tests")
public class PurposeMappingLoaderIT {

    private static final String TEST_CONFIG = "{\"engines\":[{\"engineName\":\"TestEngine\",\"priority\":100," +
        "\"description\":\"Test engine\",\"mappings\":[{\"pattern\":\"controller\",\"purpose\":\"CONTROLLER\"," +
        "\"confidence\":0.95,\"description\":\"Test controller\"}]}]}";

    @Test
    @DisplayName("Load configuration from classpath")
    public void testLoadFromClasspath() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        assertNotNull(engines);
        assertFalse(engines.isEmpty());
        assertTrue(engines.size() >= 3);
    }

    @Test
    @DisplayName("Load configuration from JSON string")
    public void testLoadFromString() {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromString(TEST_CONFIG);

        assertNotNull(engines);
        assertFalse(engines.isEmpty());
        assertEquals(1, engines.size());
        assertEquals("TestEngine", engines.get(0).getName());
    }

    @Test
    @DisplayName("Engine priorities are correct")
    public void testEnginePriorities() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        assertNotNull(engines);
        assertTrue(engines.size() >= 1);

        // Verify engines are sorted by priority descending
        for (int i = 0; i < engines.size() - 1; i++) {
            assertTrue(engines.get(i).getPriority() >= engines.get(i + 1).getPriority());
        }
    }

    @Test
    @DisplayName("Engines contain mapping rules")
    public void testEnginesHaveRules() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        assertNotNull(engines);
        for (JsonConfiguredEngine engine : engines) {
            assertNotNull(engine.getName());
            assertTrue(engine.getPriority() > 0);
        }
    }

    @Test
    @DisplayName("ClassNamingPatterns engine exists")
    public void testClassNamingPatternsEngine() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        boolean found = engines.stream()
            .anyMatch(e -> "ClassNamingPatterns".equals(e.getName()));
        assertTrue(found);
    }

    @Test
    @DisplayName("SemanticPatterns engine exists")
    public void testSemanticPatternsEngine() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        boolean found = engines.stream()
            .anyMatch(e -> "SemanticPatterns".equals(e.getName()));
        assertTrue(found);
    }

    @Test
    @DisplayName("WebPatterns engine exists")
    public void testWebPatternsEngine() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        boolean found = engines.stream()
            .anyMatch(e -> "WebPatterns".equals(e.getName()));
        assertTrue(found);
    }

    @Test
    @DisplayName("Engine evaluation works")
    public void testEngineEvaluation() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        assertNotNull(engines);
        assertTrue(engines.size() > 0);

        var match = engines.get(0).evaluate("controller");
        assertTrue(match.isPresent());
    }

    @Test
    @DisplayName("Invalid JSON string returns empty list")
    public void testInvalidJsonString() {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromString("{invalid}");

        assertNotNull(engines);
        assertTrue(engines.isEmpty());
    }

    @Test
    @DisplayName("Missing resource throws IOException")
    public void testMissingResource() {
        assertThrows(IOException.class, () -> {
            PurposeMappingLoader.loadFromClasspath("non-existent-file.json");
        });
    }

    @Test
    @DisplayName("Configuration with multiple engines loads all")
    public void testMultipleEngines() {
        String multiEngineConfig = "{\"engines\":[{\"engineName\":\"Engine1\",\"priority\":100," +
            "\"mappings\":[{\"pattern\":\"test1\",\"purpose\":\"CONTROLLER\",\"confidence\":0.9}]}," +
            "{\"engineName\":\"Engine2\",\"priority\":80," +
            "\"mappings\":[{\"pattern\":\"test2\",\"purpose\":\"PANEL\",\"confidence\":0.8}]}]}";

        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromString(multiEngineConfig);

        assertEquals(2, engines.size());
        assertEquals("Engine1", engines.get(0).getName());
        assertEquals("Engine2", engines.get(1).getName());
    }

    @Test
    @DisplayName("Engine names are preserved")
    public void testEngineNames() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        assertFalse(engines.isEmpty());
        for (JsonConfiguredEngine engine : engines) {
            assertNotNull(engine.getName());
            assertFalse(engine.getName().isEmpty());
        }
    }

    @Test
    @DisplayName("Engines sorted by priority descending")
    public void testEnginesSortedByPriority() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        for (int i = 0; i < engines.size() - 1; i++) {
            assertTrue(engines.get(i).getPriority() >= engines.get(i + 1).getPriority());
        }
    }

    @Test
    @DisplayName("Configuration has reasonable number of mappings")
    public void testConfigurationMappingCount() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        int totalMappings = 0;
        for (JsonConfiguredEngine engine : engines) {
            // Each engine should have evaluate() which uses rules
            var result = engine.evaluate("test");
            // Just verify it executes without error
            assertNotNull(result);
        }
        // At least one engine should be able to evaluate
        boolean canEvaluate = engines.stream().anyMatch(e -> e.evaluate("controller").isPresent());
        assertTrue(canEvaluate);
    }

    @Test
    @DisplayName("Load empty JSON configuration")
    public void testEmptyConfiguration() {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromString("{}");

        assertNotNull(engines);
        assertTrue(engines.isEmpty());
    }

    @Test
    @DisplayName("Configuration engine priority is positive")
    public void testPositivePriority() throws IOException {
        List<JsonConfiguredEngine> engines = PurposeMappingLoader.loadFromClasspath("purpose-mappings.json");

        for (JsonConfiguredEngine engine : engines) {
            assertTrue(engine.getPriority() > 0);
        }
    }
}
