package com.leadreach.service;

import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import leadreach.EnrichmentServiceGrpc;
import leadreach.EnrichmentServiceOuterClass.EnrichRequest;
import leadreach.EnrichmentServiceOuterClass.EnrichResponse;
import leadreach.LeadOuterClass.Lead;

@Singleton
public class EnrichmentServiceImpl extends EnrichmentServiceGrpc.EnrichmentServiceImplBase {

    @Override
    public void enrich(EnrichRequest request, StreamObserver<EnrichResponse> responseObserver) {
        // Create a hardcoded enriched lead with the requested ID
        Lead enrichedLead = Lead.newBuilder()
            .setId(request.getId())
            .setName("John Doe")
            .setEmail("john.doe@example.com")
            .setStatus("ENRICHED")
            // Add any additional enriched fields here
            .build();
        
        // Create and send the response
        EnrichResponse response = EnrichResponse.newBuilder()
            .setLead(enrichedLead)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
