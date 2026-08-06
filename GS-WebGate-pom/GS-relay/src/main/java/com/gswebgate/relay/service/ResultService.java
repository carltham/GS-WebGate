package com.gswebgate.relay.service;

import com.gswebgate.relay.contract.SearchResult;
import com.gswebgate.relay.db.Result;
import com.gswebgate.relay.db.ResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for result operations.
 * Coordinates persistence and business logic for results.
 */
@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final ObjectMapper objectMapper;

    public ResultService(ResultRepository resultRepository, ObjectMapper objectMapper) {
        this.resultRepository = resultRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Store a search result.
     * 
     * @param result The search result to store
     */
    public void storeResult(SearchResult result) {
        Result entity = new Result(
                result.getMessageId(),
                result.isAnswerFound(),
                result.getAnswer(),
                result.getConfidence()
        );
        
        entity.setProcessingTimeMs(result.getProcessingTimeMs());
        
        if (result.getSources() != null && !result.getSources().isEmpty()) {
            try {
                entity.setSources(objectMapper.writeValueAsString(result.getSources()));
            } catch (Exception e) {
                // If serialization fails, store empty sources
                entity.setSources("[]");
            }
        }
        
        resultRepository.save(entity);
    }

    /**
     * Retrieve a stored result by message ID.
     * 
     * @param messageId The message ID
     * @return The result, or empty if not found
     */
    public Optional<SearchResult> retrieveResult(String messageId) {
        Optional<Result> entity = resultRepository.findById(messageId);
        return entity.map(this::mapToSearchResult);
    }

    private SearchResult mapToSearchResult(Result entity) {
        SearchResult result = new SearchResult(
                entity.getMessageId(),
                entity.isAnswerFound(),
                entity.getAnswer(),
                entity.getConfidence()
        );
        result.setProcessingTimeMs(entity.getProcessingTimeMs());
        
        if (entity.getSources() != null && !entity.getSources().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                List<String> sources = objectMapper.readValue(entity.getSources(), List.class);
                result.setSources(sources);
            } catch (Exception e) {
                // If deserialization fails, set empty sources
                result.setSources(List.of());
            }
        }
        
        return result;
    }
}
