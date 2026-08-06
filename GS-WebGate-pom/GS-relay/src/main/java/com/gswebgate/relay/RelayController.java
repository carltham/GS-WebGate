package com.gswebgate.relay;

import com.gswebgate.relay.contract.PolledWork;
import com.gswebgate.relay.contract.SearchResult;
import com.gswebgate.relay.contract.WorkItemRequest;
import com.gswebgate.relay.contract.WorkItemResponse;
import com.gswebgate.relay.db.WorkItem;
import com.gswebgate.relay.service.ResultService;
import com.gswebgate.relay.service.WorkItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * REST controller for the relay message queue.
 * Handles work item submission, polling, and result retrieval.
 */
@RestController
@RequestMapping("/messages")
public class RelayController {

    private final WorkItemService workItemService;
    private final ResultService resultService;

    public RelayController(WorkItemService workItemService, ResultService resultService) {
        this.workItemService = workItemService;
        this.resultService = resultService;
    }

    /**
     * Submit a work item for processing.
     * 
     * @param request The work item request
     * @return 201 Created with the assigned message ID
     */
    @PostMapping
    public ResponseEntity<WorkItemResponse> submitWork(@RequestBody WorkItemRequest request) {
        WorkItemResponse response = workItemService.submitWork(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Poll for the next pending work item.
     * 
     * @return 200 OK with the next pending work item and message ID, or 204 No Content if none exist
     */
    @PostMapping("/next-pending")
    public ResponseEntity<PolledWork> fetchNextPending() {
        Optional<WorkItem> workItem = workItemService.fetchNextPending();
        
        if (workItem.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        WorkItem item = workItem.get();
        workItemService.claimWork(item.getMessageId());
        
        PolledWork polledWork = new PolledWork(item.getMessageId(), item.getQuestion());
        polledWork.setContext(item.getContext());
        polledWork.setTarget(item.getTarget());
        polledWork.setMode(item.getMode());
        
        return ResponseEntity.ok(polledWork);
    }

    /**
     * Store a search result.
     * 
     * @param result The search result
     * @return 201 Created
     */
    @PostMapping("/results")
    public ResponseEntity<Void> storeResult(@RequestBody SearchResult result) {
        resultService.storeResult(result);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Retrieve a stored result by message ID.
     * 
     * @param messageId The message ID
     * @return 200 OK with the result, or 404 Not Found if not exist
     */
    @GetMapping("/results/{messageId}")
    public ResponseEntity<SearchResult> retrieveResult(@PathVariable String messageId) {
        Optional<SearchResult> result = resultService.retrieveResult(messageId);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
