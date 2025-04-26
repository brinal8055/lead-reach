package com.leadreach;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

import java.time.Instant;

@Singleton
public class WorkflowManager {
    private final DynamoDbTable<WorkflowCheckpoint> checkpointTable;
    
    @Inject
    public WorkflowManager(DynamoDbEnhancedClient enhancedClient) {
        // Get or create the WorkflowCheckpoint table
        this.checkpointTable = enhancedClient.table("WorkflowCheckpoint", 
            TableSchema.fromBean(WorkflowCheckpoint.class));
    }
    
    public void saveCheckpoint(String leadId, String checkpoint) {
        WorkflowCheckpoint checkpointItem = new WorkflowCheckpoint();
        checkpointItem.setLeadId(leadId);
        checkpointItem.setCheckpoint(checkpoint);
        checkpointItem.setTimestamp(Instant.now().toString());
        
        checkpointTable.putItem(PutItemEnhancedRequest.builder()
            .item(checkpointItem)
            .build());
    }
    
    @DynamoDbBean
    public static class WorkflowCheckpoint {
        private String leadId;
        private String checkpoint;
        private String timestamp;
        
        @DynamoDbPartitionKey
        public String getLeadId() {
            return leadId;
        }
        
        public void setLeadId(String leadId) {
            this.leadId = leadId;
        }
        
        public String getCheckpoint() {
            return checkpoint;
        }
        
        public void setCheckpoint(String checkpoint) {
            this.checkpoint = checkpoint;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
