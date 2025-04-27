package com.leadreach.repo;

import com.leadreach.domain.LeadEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface LeadRepository extends CrudRepository<LeadEntity, String> {
    
    @Query("UPDATE lead SET status = :status WHERE id = :id")
    void updateStatus(String id, String status);
    
    Optional<LeadEntity> findByEmail(String email);
}
