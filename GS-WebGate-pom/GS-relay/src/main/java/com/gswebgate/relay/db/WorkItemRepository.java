package com.gswebgate.relay.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for persisting and retrieving work items.
 */
@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, String> {

    /**
     * Find the next pending work item in submission order.
     */
    @Query(value = "SELECT * FROM work_items WHERE state = 'pending' ORDER BY created_at ASC LIMIT 1", 
           nativeQuery = true)
    Optional<WorkItem> findNextPending();
}
