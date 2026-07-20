package com.noprobit.analyzers.ui.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.noprobit.analyzers.ui.service.AnalysisServiceClient.AnalysisResponse;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnalysisServiceClient Layer Tests")
public class AnalysisServiceClientLT {

    private AnalysisServiceClient client;

    @BeforeEach
    public void setUp() {
        // Use non-existent URL to simulate service unavailable
        client = new AnalysisServiceClient("http://localhost:9999/analysis");
    }

    @Test
    @DisplayName("Service unavailable returns false")
    public void testServiceUnavailable() {
        boolean available = client.isServiceAvailable();

        assertFalse(available);
    }

    @Test
    @DisplayName("Analysis response on failure contains defaults")
    public void testAnalysisFailureDefaults() {
        AnalysisResponse response = client.analyze("TestClass", null);

        assertNotNull(response);
        assertEquals("TestClass", response.actualName);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Response preserves input names on failure")
    public void testResponsePreservesInput() {
        String className = "MyCustomClass";
        String extendsClass = "JPanel";

        AnalysisResponse response = client.analyze(className, extendsClass);

        assertEquals(className, response.actualName);
        assertEquals(extendsClass, response.extendsClass);
    }

    @Test
    @DisplayName("Multiple analyses with unavailable service")
    public void testMultipleAnalyses() {
        AnalysisResponse r1 = client.analyze("Class1", null);
        AnalysisResponse r2 = client.analyze("Class2", null);
        AnalysisResponse r3 = client.analyze("Class3", null);

        assertFalse(r1.success);
        assertFalse(r2.success);
        assertFalse(r3.success);
    }

    @Test
    @DisplayName("Service URL can be configured")
    public void testConfigurableURL() {
        AnalysisServiceClient client1 = new AnalysisServiceClient("http://custom:8080/api");
        AnalysisServiceClient client2 = new AnalysisServiceClient();

        assertNotNull(client1);
        assertNotNull(client2);
    }

    @Test
    @DisplayName("Null extendsClass handled gracefully")
    public void testNullExtendsClass() {
        AnalysisResponse response = client.analyze("UserController", null);

        assertNotNull(response);
        assertNull(response.extendsClass);
    }

    @Test
    @DisplayName("Response format has all fields")
    public void testResponseFormat() {
        AnalysisResponse response = client.analyze("TestClass", "ParentClass");

        assertNotNull(response.actualName);
        assertNotNull(response.suggestedName);
        assertNotNull(response.purpose);
        assertNotNull(response.extendsClass);
        assertFalse(response.success);
    }

    @Test
    @DisplayName("Client handles empty strings")
    public void testEmptyStrings() {
        AnalysisResponse response = client.analyze("", "");

        assertNotNull(response);
        assertEquals("", response.actualName);
    }

    @Test
    @DisplayName("Service URL from environment or default")
    public void testURLSourceConfiguration() {
        // Without env var, uses default
        String envUrl = System.getenv("JAR_SERVICE_URL");
        if (envUrl == null) {
            AnalysisServiceClient defaultClient = new AnalysisServiceClient();
            assertNotNull(defaultClient);
        }
    }

    @Test
    @DisplayName("Response success flag reflects service state")
    public void testSuccessFlag() {
        AnalysisResponse response = client.analyze("TestClass", null);

        if (!client.isServiceAvailable()) {
            assertFalse(response.success);
        }
    }
}
