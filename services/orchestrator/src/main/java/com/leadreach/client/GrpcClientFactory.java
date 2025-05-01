package com.leadreach.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import leadreach.EnrichmentServiceGrpc;
import leadreach.LeadServiceGrpc;
import leadreach.OutreachServiceGrpc;

@Factory
public class GrpcClientFactory {
    
    @Value("${lead.service.host:lead-service}")
    private String leadServiceHost;
    
    @Value("${lead.service.port:50051}")
    private int leadServicePort;
    
    @Value("${enrichment.service.host:enrichment-service}")
    private String enrichmentServiceHost;
    
    @Value("${enrichment.service.port:50052}")
    private int enrichmentServicePort;
    
    @Value("${outreach.service.host:outreach-service}")
    private String outreachServiceHost;
    
    @Value("${outreach.service.port:50053}")
    private int outreachServicePort;
    
    @Bean
    @Singleton
    public LeadServiceGrpc.LeadServiceBlockingStub leadServiceStub() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(leadServiceHost, leadServicePort)
                .usePlaintext()
                .build();
        return LeadServiceGrpc.newBlockingStub(channel);
    }
    
    @Bean
    @Singleton
    public EnrichmentServiceGrpc.EnrichmentServiceBlockingStub enrichmentServiceStub() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(enrichmentServiceHost, enrichmentServicePort)
                .usePlaintext()
                .build();
        return EnrichmentServiceGrpc.newBlockingStub(channel);
    }
    
    @Bean
    @Singleton
    public OutreachServiceGrpc.OutreachServiceBlockingStub outreachServiceStub() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(outreachServiceHost, outreachServicePort)
                .usePlaintext()
                .build();
        return OutreachServiceGrpc.newBlockingStub(channel);
    }
}
