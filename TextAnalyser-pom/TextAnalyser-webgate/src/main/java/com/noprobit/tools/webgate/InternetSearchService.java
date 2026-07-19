package com.noprobit.tools.webgate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

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
            String url = DUCKDUCKGO_API + "?q=" + encodedQuery + "&format=json&no_html=1&t=textanalyser";

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

        String resultsText = (abstract_ + " " + answer).toLowerCase();

        if (lower.contains("controller") &&
            (resultsText.contains("controller") || resultsText.contains("orchestrat"))) {
            confidence += 0.25;
        } else if (lower.contains("panel") &&
                   (resultsText.contains("panel") || resultsText.contains("ui"))) {
            confidence += 0.25;
        } else if (lower.contains("worker") &&
                   (resultsText.contains("worker") || resultsText.contains("thread"))) {
            confidence += 0.25;
        } else if (lower.contains("validator") &&
                   (resultsText.contains("validator") || resultsText.contains("valid"))) {
            confidence += 0.25;
        } else if (lower.contains("exporter") &&
                   (resultsText.contains("export") || resultsText.contains("convert"))) {
            confidence += 0.25;
        }

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
        String queryLower = query.toLowerCase();

        boolean isController = queryLower.contains("controller") &&
            (queryLower.contains("orchestrat") || queryLower.contains("logic"));
        boolean isPanel = queryLower.contains("panel") &&
            (queryLower.contains("ui") || queryLower.contains("view"));
        boolean isWorker = queryLower.contains("worker") &&
            (queryLower.contains("thread") || queryLower.contains("async"));
        boolean isValidator = queryLower.contains("validator") &&
            (queryLower.contains("valid") || queryLower.contains("check"));
        boolean isExporter = queryLower.contains("export") &&
            (queryLower.contains("save") || queryLower.contains("write"));

        boolean isRelevant = isController || isPanel || isWorker || isValidator || isExporter;
        double confidence = isRelevant ? 0.65 : 0.20;

        result.setRelevant(isRelevant);
        result.setReason("Local pattern matching (fallback)");
        result.setSource("LocalAnalysis");
        result.setConfidence(confidence);
        result.setProcessingTime(System.currentTimeMillis() - startTime);

        return result;
    }
}
