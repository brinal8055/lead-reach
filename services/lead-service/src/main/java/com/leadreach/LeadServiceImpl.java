package com.leadreach;

import com.leadreach.domain.LeadEntity;
import com.leadreach.repo.LeadRepository;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import leadreach.LeadServiceGrpc;
import leadreach.LeadOuterClass.Lead;
import leadreach.LeadServiceOuterClass.GetLeadRequest;
import leadreach.LeadServiceOuterClass.UpdateStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class LeadServiceImpl extends LeadServiceGrpc.LeadServiceImplBase {
    private static final Logger LOG = LoggerFactory.getLogger(LeadServiceImpl.class);
    
    private final LeadRepository leadRepository;

    @Inject
    public LeadServiceImpl(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }
    
    @PostConstruct
    public void init() {
        LOG.info("Initializing LeadServiceImpl with demo data");
        try {
            // Bootstrap demo data if table is empty
            long count = leadRepository.count();
            LOG.info("Current lead count: {}", count);
            
            if (count == 0) {
                LOG.info("Creating demo leads");
                try {
                    createLead("John Doe", "john@example.com");
                    createLead("Jane Smith", "jane@example.com");
                    createLead("Bob Johnson", "bob@example.com");
                    LOG.info("Demo leads created successfully");
                } catch (Exception e) {
                    LOG.error("Error creating demo leads: {}", e.getMessage(), e);
                }
            } else {
                LOG.info("Demo data already exists, skipping creation");
            }
        } catch (Exception e) {
            LOG.error("Error in init method: {}", e.getMessage(), e);
            // Don't throw the exception, as it would prevent the application from starting
            // Instead, log the error and continue
        }
    }

    @Override
    public void getLead(GetLeadRequest request, StreamObserver<Lead> responseObserver) {
        LOG.debug("Received getLead request for ID: {}", request.getId());
        try {
            Optional<LeadEntity> leadEntityOpt = leadRepository.findById(request.getId());
            
            if (leadEntityOpt.isEmpty()) {
                LOG.warn("Lead not found: {}", request.getId());
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Lead not found: " + request.getId())
                    .asRuntimeException());
                return;
            }
            
            LeadEntity leadEntity = leadEntityOpt.get();
            Lead lead = convertToProto(leadEntity);
            
            LOG.debug("Returning lead: {}", lead);
            responseObserver.onNext(lead);
            responseObserver.onCompleted();
        } catch (Exception e) {
            LOG.error("Error in getLead: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error retrieving lead: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void updateStatus(UpdateStatusRequest request, StreamObserver<Lead> responseObserver) {
        LOG.debug("Received updateStatus request for ID: {} with status: {}", request.getId(), request.getStatus());
        try {
            // Check if lead exists
            Optional<LeadEntity> leadEntityOpt = leadRepository.findById(request.getId());
            if (leadEntityOpt.isEmpty()) {
                LOG.warn("Lead not found for status update: {}", request.getId());
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Lead not found: " + request.getId())
                    .asRuntimeException());
                return;
            }
            
            // Update status using the repository method
            leadRepository.updateStatus(request.getId(), request.getStatus());
            LOG.debug("Status updated successfully for lead: {}", request.getId());
            
            // Fetch the updated entity
            LeadEntity updatedEntity = leadRepository.findById(request.getId()).get();
            Lead updatedLead = convertToProto(updatedEntity);
            
            responseObserver.onNext(updatedLead);
            responseObserver.onCompleted();
        } catch (Exception e) {
            LOG.error("Error in updateStatus: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error updating lead status: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    // Helper method to create a lead (not exposed via gRPC)
    // This would typically be called by tests or other internal services
    public Lead createLead(String name, String email) {
        LOG.debug("Creating lead with name: {} and email: {}", name, email);
        String id = UUID.randomUUID().toString();
        
        LeadEntity leadEntity = new LeadEntity();
        leadEntity.setId(id);
        leadEntity.setName(name);
        leadEntity.setEmail(email);
        leadEntity.setStatus("NEW");
        
        leadRepository.save(leadEntity);
        LOG.debug("Lead created with ID: {}", id);
        
        return convertToProto(leadEntity);
    }
    
    // Helper method to convert LeadEntity to Lead proto
    private Lead convertToProto(LeadEntity entity) {
        return Lead.newBuilder()
            .setId(entity.getId())
            .setName(entity.getName())
            .setEmail(entity.getEmail())
            .setStatus(entity.getStatus())
            .build();
    }
}
