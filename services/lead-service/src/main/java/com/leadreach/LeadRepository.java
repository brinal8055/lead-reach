package com.leadreach;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import leadreach.LeadOuterClass.Lead;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

import java.util.Optional;

@Singleton
public class LeadRepository {
    private final DynamoDbTable<LeadItem> leadTable;
    
    @Inject
    public LeadRepository(DynamoDbEnhancedClient enhancedClient) {
        // Get or create the Leads table
        this.leadTable = enhancedClient.table("Leads", TableSchema.fromBean(LeadItem.class));
    }
    
    public Optional<Lead> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        LeadItem item = leadTable.getItem(GetItemEnhancedRequest.builder()
            .key(key)
            .build());
        
        if (item == null) {
            return Optional.empty();
        }
        
        return Optional.of(Lead.newBuilder()
            .setId(item.getId())
            .setName(item.getName())
            .setEmail(item.getEmail())
            .setStatus(item.getStatus())
            .build());
    }
    
    public Lead save(Lead lead) {
        LeadItem item = new LeadItem();
        item.setId(lead.getId());
        item.setName(lead.getName());
        item.setEmail(lead.getEmail());
        item.setStatus(lead.getStatus());
        
        leadTable.putItem(PutItemEnhancedRequest.builder()
            .item(item)
            .build());
        
        return lead;
    }
    
    public Lead updateStatus(String id, String status) {
        Optional<Lead> existingLead = findById(id);
        if (existingLead.isEmpty()) {
            throw new RuntimeException("Lead not found: " + id);
        }
        
        Lead updatedLead = existingLead.get().toBuilder()
            .setStatus(status)
            .build();
        
        return save(updatedLead);
    }
    
    @DynamoDbBean
    public static class LeadItem {
        private String id;
        private String name;
        private String email;
        private String status;
        
        @DynamoDbPartitionKey
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
