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

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class LeadServiceImpl extends LeadServiceGrpc.LeadServiceImplBase {
    private final LeadRepository leadRepository;

    @Inject
    public LeadServiceImpl(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }
    
    @PostConstruct
    public void init() {
        // Bootstrap demo data if table is empty
        if (leadRepository.count() == 0) {
            createLead("John Doe", "john@example.com");
            createLead("Jane Smith", "jane@example.com");
            createLead("Bob Johnson", "bob@example.com");
        }
    }

    @Override
    public void getLead(GetLeadRequest request, StreamObserver<Lead> responseObserver) {
        Optional<LeadEntity> leadEntityOpt = leadRepository.findById(request.getId());
        
        if (leadEntityOpt.isEmpty()) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                .withDescription("Lead not found: " + request.getId())
                .asRuntimeException());
            return;
        }
        
        LeadEntity leadEntity = leadEntityOpt.get();
        Lead lead = convertToProto(leadEntity);
        
        responseObserver.onNext(lead);
        responseObserver.onCompleted();
    }

    @Override
    public void updateStatus(UpdateStatusRequest request, StreamObserver<Lead> responseObserver) {
        try {
            // Check if lead exists
            Optional<LeadEntity> leadEntityOpt = leadRepository.findById(request.getId());
            if (leadEntityOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Lead not found: " + request.getId())
                    .asRuntimeException());
                return;
            }
            
            // Update status using the repository method
            leadRepository.updateStatus(request.getId(), request.getStatus());
            
            // Fetch the updated entity
            LeadEntity updatedEntity = leadRepository.findById(request.getId()).get();
            Lead updatedLead = convertToProto(updatedEntity);
            
            responseObserver.onNext(updatedLead);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error updating lead status: " + e.getMessage())
                .asRuntimeException());
        }
    }
    
    // Helper method to create a lead (not exposed via gRPC)
    // This would typically be called by tests or other internal services
    public Lead createLead(String name, String email) {
        String id = UUID.randomUUID().toString();
        
        LeadEntity leadEntity = new LeadEntity();
        leadEntity.setId(id);
        leadEntity.setName(name);
        leadEntity.setEmail(email);
        leadEntity.setStatus("NEW");
        
        leadRepository.save(leadEntity);
        
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
