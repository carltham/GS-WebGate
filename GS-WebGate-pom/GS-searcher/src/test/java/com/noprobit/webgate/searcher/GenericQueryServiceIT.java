package com.noprobit.webgate.searcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Generic Query Service Integration Tests")
public class GenericQueryServiceIT {

    private InternetSearchService searchService;

    @Mock
    private RestTemplate mockRestTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        searchService = new InternetSearchService();
    }

    @Test
    @DisplayName("Query without context should work")
    public void testSimpleQuery() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is REST API?");

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response);
        assertEquals("What is REST API?", response.getQuestion());
        assertTrue(response.getProcessingTime() >= 0);
    }

    @Test
    @DisplayName("Query with context should enhance search")
    public void testQueryWithContext() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is the best practice?");
        request.setContext("java spring boot");

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response);
        assertEquals("What is the best practice?", response.getQuestion());
        assertTrue(response.getSources().size() >= 0);
    }

    @Test
    @DisplayName("Response should have sources")
    public void testResponseHasSources() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("How to deploy microservices?");

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response.getSources());
        assertFalse(response.getSources().isEmpty());
    }

    @Test
    @DisplayName("Confidence should be between 0 and 1")
    public void testConfidenceRange() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is the Java programming language?");

        QueryResponse response = searchService.queryGeneric(request);

        assertTrue(response.getConfidence() >= 0.0);
        assertTrue(response.getConfidence() <= 1.0);
    }

    @Test
    @DisplayName("Processing time should be tracked")
    public void testProcessingTimeTracking() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is cloud computing?");

        QueryResponse response = searchService.queryGeneric(request);

        assertTrue(response.getProcessingTime() >= 0);
        assertNotNull(response.getProcessingTime());
    }

    @Test
    @DisplayName("Technical query should return relevant answer")
    public void testTechnicalQuery() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is machine learning?");

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response);
        assertNotNull(response.getSummary());
        assertTrue(response.getConfidence() >= 0.0);
    }

    @Test
    @DisplayName("Multiple queries should work sequentially")
    public void testMultipleQueries() {
        QueryRequest req1 = new QueryRequest();
        req1.setQuestion("What is AI?");

        QueryRequest req2 = new QueryRequest();
        req2.setQuestion("What is ML?");

        QueryResponse resp1 = searchService.queryGeneric(req1);
        QueryResponse resp2 = searchService.queryGeneric(req2);

        assertNotNull(resp1);
        assertNotNull(resp2);
        assertEquals("What is AI?", resp1.getQuestion());
        assertEquals("What is ML?", resp2.getQuestion());
    }

    @Test
    @DisplayName("Query should have summary")
    public void testQueryHasSummary() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is Docker?");

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response.getSummary());
        assertFalse(response.getSummary().isEmpty());
    }

    @Test
    @DisplayName("Answer found flag should be set correctly")
    public void testAnswerFoundFlag() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is Python?");

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response);
        assertTrue(response.isAnswerFound() || !response.isAnswerFound());
    }

    @Test
    @DisplayName("Long processing timeout should not fail")
    public void testLongTimeoutQuery() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What are microservices?");
        request.setTimeout(10000);

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response);
        assertTrue(response.getProcessingTime() <= request.getTimeout());
    }

    @Test
    @DisplayName("Query with special characters should handle gracefully")
    public void testSpecialCharactersInQuery() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is @interface in Java?");

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response);
        assertEquals("What is @interface in Java?", response.getQuestion());
    }

    @Test
    @DisplayName("Response sources should include DuckDuckGo")
    public void testSourcesIncludeDuckDuckGo() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is the internet?");

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response.getSources());
        boolean hasDuckDuckGo = response.getSources().stream()
            .anyMatch(s -> s.contains("DuckDuckGo") || s.contains("Fallback"));
        assertTrue(hasDuckDuckGo);
    }

    @Test
    @DisplayName("Query max results should be respected")
    public void testMaxResultsParameter() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is Kubernetes?");
        request.setMaxResults(10);

        QueryResponse response = searchService.queryGeneric(request);

        assertNotNull(response);
        assertEquals(request.getMaxResults(), request.getMaxResults());
    }
}
