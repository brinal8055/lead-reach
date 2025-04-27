package com.leadreach;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import leadreach.EnrichmentServiceGrpc;
import leadreach.LeadServiceGrpc;
import leadreach.OrchestratorGrpc;
import leadreach.OutreachServiceGrpc;

@Factory
public class GrpcClientFactory {
    
    @Bean
    @Singleton
    public LeadServiceGrpc.LeadServiceBlockingStub leadServiceStub() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("lead-service", 50051)
                .usePlaintext()
                .build();
        return LeadServiceGrpc.newBlockingStub(channel);
    }
    
    @Bean
    @Singleton
    public EnrichmentServiceGrpc.EnrichmentServiceBlockingStub enrichmentServiceStub() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("enrichment-service", 50052)
                .usePlaintext()
                .build();
        return EnrichmentServiceGrpc.newBlockingStub(channel);
    }
    
    @Bean
    @Singleton
    public OutreachServiceGrpc.OutreachServiceBlockingStub outreachServiceStub() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("outreach-service", 50053)
                .usePlaintext()
                .build();
        return OutreachServiceGrpc.newBlockingStub(channel);
    }
}
