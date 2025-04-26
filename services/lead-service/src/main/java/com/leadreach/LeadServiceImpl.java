package com.leadreach;

import io.grpc.stub.StreamObserver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import leadreach.LeadServiceGrpc;
import leadreach.LeadOuterClass.Lead;
import leadreach.LeadServiceOuterClass.GetLeadRequest;
import leadreach.LeadServiceOuterClass.UpdateStatusRequest;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class LeadServiceImpl extends LeadServiceGrpc.LeadServiceImplBase {
    private final LeadRepository leadRepository;
    private final WorkflowManager workflowManager;

    @Inject
    public LeadServiceImpl(LeadRepository leadRepository, WorkflowManager workflowManager) {
        this.leadRepository = leadRepository;
        this.workflowManager = workflowManager;
    }

    @Override
    public void getLead(GetLeadRequest request, StreamObserver<Lead> responseObserver) {
        Optional<Lead> leadOpt = leadRepository.findById(request.getId());
        
        if (leadOpt.isEmpty()) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                .withDescription("Lead not found: " + request.getId())
                .asRuntimeException());
            return;
        }
        
        Lead lead = leadOpt.get();
        
        // Save checkpoint that lead was fetched
        workflowManager.saveCheckpoint(lead.getId(), "LEAD_FETCHED");
        
        responseObserver.onNext(lead);
        responseObserver.onCompleted();
    }

    @Override
    public void updateStatus(UpdateStatusRequest request, StreamObserver<Lead> responseObserver) {
        try {
            Lead updatedLead = leadRepository.updateStatus(request.getId(), request.getStatus());
            responseObserver.onNext(updatedLead);
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                .withDescription("Lead not found: " + request.getId())
                .asRuntimeException());
        }
    }
    
    // Helper method to create a lead (not exposed via gRPC)
    // This would typically be called by tests or other internal services
    public Lead createLead(String name, String email) {
        String id = UUID.randomUUID().toString();
        Lead lead = Lead.newBuilder()
            .setId(id)
            .setName(name)
            .setEmail(email)
            .setStatus("NEW")
            .build();
        
        return leadRepository.save(lead);
    }
}
