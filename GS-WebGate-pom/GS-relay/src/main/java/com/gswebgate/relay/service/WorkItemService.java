package com.gswebgate.relay.service;

import com.gswebgate.relay.contract.WorkItemRequest;
import com.gswebgate.relay.contract.WorkItemResponse;
import com.gswebgate.relay.db.WorkItem;
import com.gswebgate.relay.db.WorkItemRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for work item operations.
 * Coordinates persistence and business logic.
 */
@Service
public class WorkItemService {

    private final WorkItemRepository workItemRepository;

    public WorkItemService(WorkItemRepository workItemRepository) {
        this.workItemRepository = workItemRepository;
    }

    /**
     * Submit a new work item.
     * Generates a message ID, creates a work item, and stores it.
     * 
     * @param request The work item request
     * @return Response with assigned message ID
     */
    public WorkItemResponse submitWork(WorkItemRequest request) {
        String messageId = generateMessageId();
        
        WorkItem workItem = new WorkItem(messageId, request.getQuestion());
        workItem.setContext(request.getContext());
        workItem.setTarget(request.getTarget());
        workItem.setMode(request.getMode());
        workItem.setState("pending");  // Submitted items go directly to pending state
        
        workItemRepository.save(workItem);
        
        return new WorkItemResponse(messageId);
    }

    /**
     * Fetch the next pending work item for processing.
     * 
     * @return The next pending work item, or empty if none exist
     */
    public Optional<WorkItem> fetchNextPending() {
        return workItemRepository.findNextPending();
    }

    /**
     * Claim a work item by transitioning it from pending to claimed state.
     * 
     * @param messageId The message ID to claim
     * @return True if successfully claimed, false if not found
     */
    public boolean claimWork(String messageId) {
        Optional<WorkItem> workItem = workItemRepository.findById(messageId);
        if (workItem.isPresent() && "pending".equals(workItem.get().getState())) {
            WorkItem item = workItem.get();
            item.setState("claimed");
            workItemRepository.save(item);
            return true;
        }
        return false;
    }

    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
}
