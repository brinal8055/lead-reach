package com.leadreach;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Factory
public class DynamoDbConfig {
    
    @Bean
    @Singleton
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
            .endpointOverride(URI.create("http://localhost:8000"))
            .build();
    }
    
    @Bean
    @Singleton
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    }
}
