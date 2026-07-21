package com.noprobit.webgate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("InternetSearchService Integration Tests (Mocked Network)")
public class InternetSearchServiceIT {

    @Autowired
    private InternetSearchService searchService;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        assertNotNull(searchService, "SearchService should be injected");
    }

    private String createDuckDuckGoResponse(String abstractText, String answer) {
        return "{\"AbstractText\":\"" + abstractText + "\",\"Answer\":\"" + answer + "\"}";
    }

    @Test
    public void testSearchForControllerPattern() {
        String query = "UserController orchestrating business logic";
        String response = createDuckDuckGoResponse("Controller pattern for orchestration", "");
        when(restTemplate.getForObject(contains("UserController"), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search(query);

        assertNotNull(result);
        assertNotNull(result.getReason());
        assertTrue(result.getConfidence() >= 0.0 && result.getConfidence() <= 1.0);
        assertNotNull(result.getSource());
        assertTrue(result.getProcessingTime() >= 0);
    }

    @Test
    public void testSearchForPanelPattern() {
        String response = createDuckDuckGoResponse("Panel UI component", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("UserPanel UI view component");

        assertNotNull(result);
        assertTrue(result.getConfidence() >= 0.0 && result.getConfidence() <= 1.0);
        assertTrue(result.getSource().contains("DuckDuckGo"));
    }

    @Test
    public void testSearchForWorkerPattern() {
        String response = createDuckDuckGoResponse("Worker thread async processing", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("DataWorker async thread processing");

        assertNotNull(result);
        assertFalse(result.getReason().isEmpty());
        assertTrue(result.getProcessingTime() > 0);
    }

    @Test
    public void testSearchForValidatorPattern() {
        String response = createDuckDuckGoResponse("Validator checks valid data", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("UserValidator checking valid data");

        assertNotNull(result);
        assertTrue(result.getConfidence() >= 0.0 && result.getConfidence() <= 1.0);
    }

    @Test
    public void testSearchForExporterPattern() {
        String response = createDuckDuckGoResponse("Exporter converts saves format", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("DataExporter converting saving format");

        assertNotNull(result);
        assertNotNull(result.getReason());
        assertNotNull(result.getSource());
    }

    @Test
    public void testSearchReturnsValidConfidence() {
        String response = createDuckDuckGoResponse("Controller pattern", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("UserController");

        double confidence = result.getConfidence();
        assertTrue(confidence >= 0.0, "Confidence should be >= 0.0");
        assertTrue(confidence <= 1.0, "Confidence should be <= 1.0");
    }

    @Test
    public void testSearchWithUnknownKeyword() {
        String response = createDuckDuckGoResponse("Some generic result", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("XyzAbc123 UnknownPattern NotAClassType");

        assertNotNull(result);
        assertNotNull(result.getReason());
        assertTrue(result.getConfidence() >= 0.0);
    }

    @Test
    public void testSearchProcessingTimeRecorded() {
        String response = createDuckDuckGoResponse("Controller", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        long startTime = System.currentTimeMillis();
        SearchResult result = searchService.search("UserController");
        long endTime = System.currentTimeMillis();

        assertTrue(result.getProcessingTime() >= 0);
        assertTrue(result.getProcessingTime() <= (endTime - startTime + 100));
    }

    @Test
    public void testSearchReturnsDuckDuckGoSource() {
        String response = createDuckDuckGoResponse("Controller orchestrating", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("UserController orchestrating");

        assertTrue(result.getSource().contains("DuckDuckGo"));
    }

    @Test
    public void testSearchReasonNotEmpty() {
        String response = createDuckDuckGoResponse("Panel UI", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("UserPanel UI");

        assertNotNull(result.getReason());
        assertFalse(result.getReason().isEmpty());
    }

    @Test
    public void testSearchWithComplexQuery() {
        String response = createDuckDuckGoResponse(
            "PaymentProcessor handles transactions validates data exports results",
            ""
        );
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        String complexQuery = "PaymentProcessorController handling transactions " +
                            "validating data and exporting results to database";
        SearchResult result = searchService.search(complexQuery);

        assertNotNull(result);
        assertTrue(result.getConfidence() >= 0.0);
        assertNotNull(result.getSource());
    }

    @Test
    public void testMultipleSearchRequests() {
        String response = createDuckDuckGoResponse("Pattern found", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult r1 = searchService.search("UserController");
        SearchResult r2 = searchService.search("UserPanel");
        SearchResult r3 = searchService.search("DataWorker");

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);

        assertTrue(r1.getConfidence() >= 0.0 && r1.getConfidence() <= 1.0);
        assertTrue(r2.getConfidence() >= 0.0 && r2.getConfidence() <= 1.0);
        assertTrue(r3.getConfidence() >= 0.0 && r3.getConfidence() <= 1.0);
    }

    @Test
    public void testSearchHandlesEmptyQuery() {
        String response = createDuckDuckGoResponse("", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("");

        assertNotNull(result);
        assertTrue(result.getConfidence() >= 0.0);
    }

    @Test
    public void testSearchWithSpecialCharacters() {
        String response = createDuckDuckGoResponse("result found", "");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        SearchResult result = searchService.search("User#$%Controller @#$ !@# logic");

        assertNotNull(result);
        assertNotNull(result.getReason());
    }

    @Test
    @DisplayName("Network unavailable falls back to local analysis")
    public void testNetworkUnavailableFallback() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("Network error"));

        SearchResult result = searchService.search("UserController");

        assertNotNull(result);
        assertTrue(result.getConfidence() >= 0.0);
        assertNotNull(result.getReason());
    }

    @Test
    @DisplayName("Malformed JSON response handled gracefully")
    public void testMalformedJsonHandling() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{invalid json}");

        SearchResult result = searchService.search("UserController");

        assertNotNull(result);
        assertTrue(result.getConfidence() >= 0.0 && result.getConfidence() <= 1.0);
    }
}
