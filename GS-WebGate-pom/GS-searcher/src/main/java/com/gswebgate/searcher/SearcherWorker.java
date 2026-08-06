package com.gswebgate.searcher;

import com.gswebgate.searcher.contract.PolledWork;
import com.gswebgate.searcher.contract.SearchResult;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Orchestrates the searcher worker loop.
 * Polls for work, executes searches, and submits results.
 */
@Service
public class SearcherWorker {

    private final RelayClient relayClient;
    private final SearchExecutor searchExecutor;

    public SearcherWorker(RelayClient relayClient, SearchExecutor searchExecutor) {
        this.relayClient = relayClient;
        this.searchExecutor = searchExecutor;
    }

    /**
     * Execute a single work cycle:
     * 1. Poll relay for next pending work
     * 2. Execute search if work found
     * 3. Submit result back to relay
     * 
     * @return True if work was processed, false if no work available
     */
    public boolean processNextWork() {
        // Step 1: Poll for next work
        Optional<PolledWork> polledWork = relayClient.pollNextPending();
        
        if (polledWork.isEmpty()) {
            return false;
        }
        
        PolledWork work = polledWork.get();
        
        // Step 2: Execute search
        SearchResult result = searchExecutor.execute(work);
        
        // Step 3: Submit result
        return relayClient.submitResult(result);
    }
}
