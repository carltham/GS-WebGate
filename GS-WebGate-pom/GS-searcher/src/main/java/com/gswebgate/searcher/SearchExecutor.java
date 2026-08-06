package com.gswebgate.searcher;

import com.gswebgate.searcher.contract.PolledWork;
import com.gswebgate.searcher.contract.SearchResult;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Executes search operations against external search providers.
 * For Phase 0, provides a simple mock implementation.
 */
@Service
public class SearchExecutor {

    /**
     * Execute a search for the given polled work item.
     * Returns a mock result based on question content.
     * 
     * @param polledWork The polled work item containing the question
     * @return A search result with answer and confidence
     */
    public SearchResult execute(PolledWork polledWork) {
        long startTime = System.currentTimeMillis();
        
        // Simple mock search implementation
        String question = polledWork.getQuestion();
        SearchResult result = new SearchResult();
        result.setMessageId(polledWork.getMessageId());
        
        if (question.toLowerCase().contains("capital") && question.toLowerCase().contains("france")) {
            result.setAnswerFound(true);
            result.setAnswer("Paris is the capital of France");
            result.setConfidence(0.99);
            result.setSources(List.of(
                "https://en.wikipedia.org/wiki/Paris",
                "https://en.wikipedia.org/wiki/Capital_of_France"
            ));
        } else if (question.toLowerCase().contains("largest") && question.toLowerCase().contains("planet")) {
            result.setAnswerFound(true);
            result.setAnswer("Jupiter is the largest planet in our solar system");
            result.setConfidence(0.99);
            result.setSources(List.of(
                "https://nasa.gov/planets/jupiter",
                "https://en.wikipedia.org/wiki/Jupiter"
            ));
        } else {
            result.setAnswerFound(false);
            result.setAnswer("Unable to find a definitive answer");
            result.setConfidence(0.3);
            result.setSources(List.of());
        }
        
        long endTime = System.currentTimeMillis();
        result.setProcessingTimeMs(endTime - startTime);
        
        return result;
    }
}
