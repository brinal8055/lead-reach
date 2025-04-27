package com.leadreach;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

@Factory
public class DynamoDbConfig {
    
    @Bean
    @Singleton
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(Region.US_EAST_1);
        
        // For local development, connect to local DynamoDB
        String dynamoDbEndpoint = System.getenv("DYNAMO_ENDPOINT");
        if (dynamoDbEndpoint != null && !dynamoDbEndpoint.isEmpty()) {
            // For local DynamoDB, we need to provide credentials
            String accessKey = System.getenv("AWS_ACCESS_KEY_ID") != null ? 
                System.getenv("AWS_ACCESS_KEY_ID") : "dummy";
            String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY") != null ? 
                System.getenv("AWS_SECRET_ACCESS_KEY") : "dummy";
                
            builder.endpointOverride(URI.create(dynamoDbEndpoint))
                   .credentialsProvider(StaticCredentialsProvider.create(
                       AwsBasicCredentials.create(accessKey, secretKey)
                   ));
        }

        return builder.build();
    }
    
}
