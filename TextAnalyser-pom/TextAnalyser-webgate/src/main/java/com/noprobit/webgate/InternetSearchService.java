package com.noprobit.webgate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

@Service
public class InternetSearchService {
    private static final Logger logger = Logger.getLogger(InternetSearchService.class.getName());
    private static final String DUCKDUCKGO_API = "https://api.duckduckgo.com/";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${webgate.search.enabled:true}")
    private boolean searchEnabled;

    public SearchResult search(String query) {
        long startTime = System.currentTimeMillis();

        try {
            if (!searchEnabled) {
                return fallbackKeywordAnalysis(query, startTime);
            }

            SearchResult result = performDuckDuckGoSearch(query);
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            return result;

        } catch (Exception e) {
            logger.warning("Internet search failed: " + e.getMessage());
            return fallbackKeywordAnalysis(query, startTime);
        }
    }

    private SearchResult performDuckDuckGoSearch(String query) {
        SearchResult result = new SearchResult();

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = DUCKDUCKGO_API + "?q=" + encodedQuery + "&format=json&no_html=1&t=websearcher";

            String response = restTemplate.getForObject(url, String.class);

            if (response != null && !response.isEmpty()) {
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();

                String abstract_ = json.has("AbstractText") ? json.get("AbstractText").getAsString() : "";
                String instantAnswer = json.has("Answer") ? json.get("Answer").getAsString() : "";

                double confidence = calculateConfidence(query, abstract_, instantAnswer);

                result.setRelevant(confidence > 0.5);
                result.setConfidence(confidence);
                result.setReason(buildSearchReason(query, abstract_, instantAnswer, confidence));
                result.setSource("DuckDuckGo");
            } else {
                result.setRelevant(false);
                result.setReason("No search results found");
                result.setSource("DuckDuckGo");
                result.setConfidence(0.0);
            }

        } catch (Exception e) {
            logger.warning("DuckDuckGo search error: " + e.getMessage());
            result.setRelevant(false);
            result.setReason("Search service unavailable: " + e.getMessage());
            result.setSource("DuckDuckGo_Error");
            result.setConfidence(0.0);
        }

        return result;
    }

    private double calculateConfidence(String query, String abstract_, String answer) {
        double confidence = 0.0;
        String lower = query.toLowerCase();

        if (!abstract_.isEmpty()) confidence += 0.4;
        if (!answer.isEmpty()) confidence += 0.3;

        return Math.min(1.0, confidence);
    }

    private String buildSearchReason(String query, String abstract_, String answer, double confidence) {
        if (confidence > 0.7) {
            return "High confidence match found in internet search results";
        } else if (confidence > 0.5) {
            return "Moderate confidence match found in search results";
        } else if (!abstract_.isEmpty() || !answer.isEmpty()) {
            return "Low confidence: Results found but unclear relevance";
        } else {
            return "No relevant information found in search results";
        }
    }

    private SearchResult fallbackKeywordAnalysis(String query, long startTime) {
        SearchResult result = new SearchResult();

        result.setRelevant(false);
        result.setReason("Search service unavailable - fallback mode");
        result.setSource("LocalFallback");
        result.setConfidence(0.0);
        result.setProcessingTime(System.currentTimeMillis() - startTime);

        return result;
    }

    public QueryResponse queryGeneric(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        QueryResponse response = new QueryResponse();
        response.setQuestion(request.getQuestion());

        try {
            if (!searchEnabled) {
                return fallbackGenericResponse(request, startTime);
            }

            String enhancedQuery = request.getQuestion();
            if (request.getContext() != null && !request.getContext().isEmpty()) {
                enhancedQuery += " " + request.getContext();
            }

            String encodedQuery = URLEncoder.encode(enhancedQuery, StandardCharsets.UTF_8);
            String url = DUCKDUCKGO_API + "?q=" + encodedQuery + "&format=json&no_html=1&t=websearcher";

            String apiResponse = restTemplate.getForObject(url, String.class);

            if (apiResponse != null && !apiResponse.isEmpty()) {
                JsonObject json = JsonParser.parseString(apiResponse).getAsJsonObject();

                String abstractText = json.has("AbstractText") ? json.get("AbstractText").getAsString() : "";
                String instantAnswer = json.has("Answer") ? json.get("Answer").getAsString() : "";
                String redirect = json.has("Redirect") ? json.get("Redirect").getAsString() : "";

                if (!instantAnswer.isEmpty()) {
                    response.setAnswer(instantAnswer);
                    response.setAnswerFound(true);
                    response.setConfidence(0.95);
                    response.addSource("DuckDuckGo (Instant Answer)");
                    response.setSummary("Direct answer found");
                } else if (!abstractText.isEmpty()) {
                    response.setAnswer(abstractText);
                    response.setAnswerFound(true);
                    response.setConfidence(0.80);
                    response.addSource("DuckDuckGo (Abstract)");
                    response.setSummary("Summary answer found");
                } else if (!redirect.isEmpty()) {
                    response.setAnswer("Redirect to: " + redirect);
                    response.setAnswerFound(true);
                    response.setConfidence(0.70);
                    response.addSource("DuckDuckGo (Related Topic)");
                    response.setSummary("Related topic found");
                } else {
                    response.setAnswerFound(false);
                    response.setConfidence(0.20);
                    response.setSummary("No direct answer found");
                    response.addSource("DuckDuckGo");
                }
            } else {
                response.setAnswerFound(false);
                response.setConfidence(0.0);
                response.setSummary("No search results");
                response.addSource("DuckDuckGo");
            }

        } catch (Exception e) {
            logger.warning("Generic query failed: " + e.getMessage());
            return fallbackGenericResponse(request, startTime);
        }

        response.setProcessingTime(System.currentTimeMillis() - startTime);
        return response;
    }

    private QueryResponse fallbackGenericResponse(QueryRequest request, long startTime) {
        QueryResponse response = new QueryResponse();
        response.setQuestion(request.getQuestion());
        response.setAnswerFound(false);
        response.setConfidence(0.0);
        response.setSummary("Service temporarily unavailable - using fallback");
        response.addSource("LocalFallback");
        response.setProcessingTime(System.currentTimeMillis() - startTime);
        return response;
    }
}
