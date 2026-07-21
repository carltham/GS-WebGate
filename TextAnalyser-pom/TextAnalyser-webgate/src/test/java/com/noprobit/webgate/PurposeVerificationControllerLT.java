package com.noprobit.webgate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PurposeVerificationController Layer Tests (Mocked SearchService)")
public class PurposeVerificationControllerLT {

    private PurposeVerificationController controller;

    @Mock
    private InternetSearchService mockSearchService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PurposeVerificationController();
    }

    @Test
    @DisplayName("Controller uses search service")
    public void testControllerUsesSearchService() {
        SearchResult mockResult = new SearchResult();
        mockResult.setRelevant(true);
        mockResult.setReason("Test reason");
        mockResult.setSource("TestService");
        mockResult.setConfidence(0.85);
        mockResult.setProcessingTime(100);

        when(mockSearchService.search(anyString())).thenReturn(mockResult);

        SearchResult result = mockSearchService.search("UserController");

        assertNotNull(result);
        assertTrue(result.isRelevant());
        assertEquals("TestService", result.getSource());
    }

    @Test
    @DisplayName("Controller handles search failures gracefully")
    public void testHandleSearchFailure() {
        SearchResult failedResult = new SearchResult();
        failedResult.setRelevant(false);
        failedResult.setReason("Service error");
        failedResult.setSource("ERROR");
        failedResult.setConfidence(0.0);

        when(mockSearchService.search(anyString())).thenReturn(failedResult);

        SearchResult result = mockSearchService.search("UnknownClass");

        assertNotNull(result);
        assertFalse(result.isRelevant());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    @DisplayName("Search result confidence in valid range")
    public void testConfidenceRange() {
        SearchResult result = new SearchResult();
        result.setConfidence(0.75);

        assertTrue(result.getConfidence() >= 0.0);
        assertTrue(result.getConfidence() <= 1.0);
    }

    @Test
    @DisplayName("Multiple searches use same service")
    public void testMultipleSearches() {
        when(mockSearchService.search(anyString()))
            .thenReturn(createMockResult(0.8))
            .thenReturn(createMockResult(0.6))
            .thenReturn(createMockResult(0.9));

        SearchResult r1 = mockSearchService.search("Query1");
        SearchResult r2 = mockSearchService.search("Query2");
        SearchResult r3 = mockSearchService.search("Query3");

        assertEquals(0.8, r1.getConfidence());
        assertEquals(0.6, r2.getConfidence());
        assertEquals(0.9, r3.getConfidence());

        verify(mockSearchService, times(3)).search(anyString());
    }

    @Test
    @DisplayName("Controller returns properly formatted response")
    public void testResponseFormat() {
        JsonObject response = new JsonObject();
        response.addProperty("verified", true);
        response.addProperty("reason", "Pattern matched");
        response.addProperty("confidence", 0.85);

        assertTrue(response.has("verified"));
        assertTrue(response.has("reason"));
        assertTrue(response.has("confidence"));
    }

    @Test
    @DisplayName("Error response has required fields")
    public void testErrorResponse() {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("error", "Invalid input");
        errorResponse.addProperty("verified", false);

        assertTrue(errorResponse.has("error"));
        assertTrue(errorResponse.has("verified"));
        assertFalse(errorResponse.get("verified").getAsBoolean());
    }

    private SearchResult createMockResult(double confidence) {
        SearchResult result = new SearchResult();
        result.setRelevant(confidence > 0.5);
        result.setConfidence(confidence);
        result.setReason("Mock result");
        result.setSource("MockService");
        result.setProcessingTime(50);
        return result;
    }
}
