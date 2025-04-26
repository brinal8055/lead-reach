package workflow;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;
import java.util.Optional;

@Singleton
public class DynamoCheckpointRepository implements CheckpointRepository {
    private static final String TABLE_NAME = "workflow_checkpoints";
    private final DynamoDbTable<CheckpointItem> checkpointTable;
    
    @Inject
    public DynamoCheckpointRepository(DynamoDbEnhancedClient enhancedClient) {
        this.checkpointTable = enhancedClient.table(TABLE_NAME, 
            TableSchema.fromBean(CheckpointItem.class));
    }
    
    @Override
    public void save(String workflowId, String step, String payloadJson) {
        CheckpointItem item = new CheckpointItem();
        item.setWorkflowId(workflowId);
        item.setStep(step);
        item.setPayload(payloadJson);
        item.setTs(Instant.now());
        
        checkpointTable.putItem(item);
    }
    
    @Override
    public Optional<Checkpoint> last(String workflowId) {
        QueryConditional queryConditional = QueryConditional
            .keyEqualTo(Key.builder().partitionValue(workflowId).build());
        
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
            .queryConditional(queryConditional)
            .scanIndexForward(false) // descending order by sort key
            .limit(1) // get only the latest checkpoint
            .build();
        
        return checkpointTable.query(request)
            .items()
            .stream()
            .findFirst()
            .map(item -> new Checkpoint(
                item.getWorkflowId(),
                item.getStep(),
                item.getPayload(),
                item.getTs()
            ));
    }
    
    @DynamoDbBean
    public static class CheckpointItem {
        private String workflowId;
        private String step;
        private String payload;
        private Instant ts;
        
        @DynamoDbPartitionKey
        public String getWorkflowId() {
            return workflowId;
        }
        
        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }
        
        @DynamoDbSortKey
        public String getStep() {
            return step;
        }
        
        public void setStep(String step) {
            this.step = step;
        }
        
        public String getPayload() {
            return payload;
        }
        
        public void setPayload(String payload) {
            this.payload = payload;
        }
        
        public Instant getTs() {
            return ts;
        }
        
        public void setTs(Instant ts) {
            this.ts = ts;
        }
    }
}
