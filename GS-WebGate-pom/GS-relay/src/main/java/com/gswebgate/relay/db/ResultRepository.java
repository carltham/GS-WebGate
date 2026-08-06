package com.gswebgate.relay.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for persisting and retrieving results.
 */
@Repository
public interface ResultRepository extends JpaRepository<Result, String> {
}
