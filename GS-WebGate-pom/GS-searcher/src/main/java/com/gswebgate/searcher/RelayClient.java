package com.gswebgate.searcher;

import com.gswebgate.searcher.contract.PolledWork;
import com.gswebgate.searcher.contract.SearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

/**
 * Client for communicating with the relay service.
 * Handles polling for work and submitting results.
 */
@Service
public class RelayClient {

    private final RestTemplate restTemplate;
    private final String relayBaseUrl;

    public RelayClient(RestTemplate restTemplate, @Value("${relay.base.url:http://localhost:8080}") String relayBaseUrl) {
        this.restTemplate = restTemplate;
        this.relayBaseUrl = relayBaseUrl;
    }

    /**
     * Get the relay base URL for validation purposes.
     */
    public String getRelayBaseUrl() {
        return relayBaseUrl;
    }

    /**
     * Poll the relay for the next pending work item.
     * 
     * @return The next pending work item with message ID, or empty if none exist
     */
    public Optional<PolledWork> pollNextPending() {
        try {
            ResponseEntity<PolledWork> response = restTemplate.postForEntity(
                    relayBaseUrl + "/messages/next-pending",
                    null,
                    PolledWork.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Submit a search result back to the relay.
     * 
     * @param result The search result to submit
     */
    public boolean submitResult(SearchResult result) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    relayBaseUrl + "/messages/results",
                    result,
                    Void.class
            );
            return response.getStatusCode() == HttpStatus.CREATED;
        } catch (Exception e) {
            return false;
        }
    }
}
