package com.noprobit.webgate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Generic Query Controller Layer Tests (Mocked Service)")
public class GenericQueryControllerLT {

    private PurposeVerificationController controller;

    @Mock
    private InternetSearchService mockSearchService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PurposeVerificationController();
    }

    @Test
    @DisplayName("Controller handles valid query request")
    public void testValidQueryRequest() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is REST?");

        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setQuestion("What is REST?");
        mockResponse.setAnswer("REST is Representational State Transfer");
        mockResponse.setAnswerFound(true);
        mockResponse.setConfidence(0.85);
        mockResponse.addSource("DuckDuckGo");

        when(mockSearchService.queryGeneric(request)).thenReturn(mockResponse);

        QueryResponse result = mockSearchService.queryGeneric(request);

        assertNotNull(result);
        assertEquals("What is REST?", result.getQuestion());
        assertTrue(result.isAnswerFound());
    }

    @Test
    @DisplayName("Controller processes query with context")
    public void testQueryWithContext() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("Best practices for?");
        request.setContext("Java development");

        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setQuestion("Best practices for?");
        mockResponse.setContext("Java development");
        mockResponse.setAnswerFound(true);
        mockResponse.setConfidence(0.80);

        when(mockSearchService.queryGeneric(request)).thenReturn(mockResponse);

        QueryResponse result = mockSearchService.queryGeneric(request);

        assertNotNull(result);
        assertEquals("Best practices for?", result.getQuestion());
    }

    @Test
    @DisplayName("Controller returns multiple sources")
    public void testMultipleSources() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is cloud?");

        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setQuestion("What is cloud?");
        mockResponse.addSource("DuckDuckGo");
        mockResponse.addSource("Wikipedia");
        mockResponse.addSource("StackOverflow");
        mockResponse.setConfidence(0.75);

        when(mockSearchService.queryGeneric(request)).thenReturn(mockResponse);

        QueryResponse result = mockSearchService.queryGeneric(request);

        assertEquals(3, result.getSources().size());
        assertTrue(result.getSources().contains("DuckDuckGo"));
    }

    @Test
    @DisplayName("Controller sets confidence correctly")
    public void testConfidenceSetCorrectly() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is Python?");

        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setConfidence(0.90);

        when(mockSearchService.queryGeneric(request)).thenReturn(mockResponse);

        QueryResponse result = mockSearchService.queryGeneric(request);

        assertEquals(0.90, result.getConfidence());
    }

    @Test
    @DisplayName("Controller handles no answer found")
    public void testNoAnswerFound() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("Unknown random topic xyz?");

        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setQuestion("Unknown random topic xyz?");
        mockResponse.setAnswerFound(false);
        mockResponse.setConfidence(0.15);
        mockResponse.setSummary("No results found");

        when(mockSearchService.queryGeneric(request)).thenReturn(mockResponse);

        QueryResponse result = mockSearchService.queryGeneric(request);

        assertFalse(result.isAnswerFound());
        assertTrue(result.getConfidence() < 0.5);
    }

    @Test
    @DisplayName("Controller respects max results parameter")
    public void testMaxResultsParameter() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What are patterns?");
        request.setMaxResults(3);

        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setMaxResults(3);

        when(mockSearchService.queryGeneric(request)).thenReturn(mockResponse);

        QueryResponse result = mockSearchService.queryGeneric(request);

        assertEquals(3, result.getMaxResults());
    }

    @Test
    @DisplayName("Controller measures processing time")
    public void testProcessingTimeMeasured() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is agile?");

        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setProcessingTime(250);

        when(mockSearchService.queryGeneric(request)).thenReturn(mockResponse);

        QueryResponse result = mockSearchService.queryGeneric(request);

        assertTrue(result.getProcessingTime() > 0);
    }

    @Test
    @DisplayName("Response has summary text")
    public void testResponseHasSummary() {
        QueryRequest request = new QueryRequest();
        request.setQuestion("What is DevOps?");

        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setSummary("Comprehensive answer about DevOps practices");
        mockResponse.setAnswerFound(true);

        when(mockSearchService.queryGeneric(request)).thenReturn(mockResponse);

        QueryResponse result = mockSearchService.queryGeneric(request);

        assertNotNull(result.getSummary());
        assertFalse(result.getSummary().isEmpty());
    }

    @Test
    @DisplayName("Multiple sequential queries work")
    public void testMultipleSequentialQueries() {
        QueryRequest req1 = new QueryRequest();
        req1.setQuestion("What is AI?");

        QueryRequest req2 = new QueryRequest();
        req2.setQuestion("What is ML?");

        QueryResponse resp1 = new QueryResponse();
        resp1.setQuestion("What is AI?");
        resp1.setConfidence(0.85);

        QueryResponse resp2 = new QueryResponse();
        resp2.setQuestion("What is ML?");
        resp2.setConfidence(0.80);

        when(mockSearchService.queryGeneric(req1)).thenReturn(resp1);
        when(mockSearchService.queryGeneric(req2)).thenReturn(resp2);

        QueryResponse result1 = mockSearchService.queryGeneric(req1);
        QueryResponse result2 = mockSearchService.queryGeneric(req2);

        assertEquals("What is AI?", result1.getQuestion());
        assertEquals("What is ML?", result2.getQuestion());
        verify(mockSearchService, times(2)).queryGeneric(any(QueryRequest.class));
    }
}
