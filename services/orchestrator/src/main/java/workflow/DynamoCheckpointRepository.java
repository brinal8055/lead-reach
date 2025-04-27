package workflow;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class DynamoCheckpointRepository implements CheckpointRepository {
    
    private static final String TABLE_NAME = "workflow_checkpoints";
    private static final String PK = "workflowId";
    private static final String SK = "step";
    private static final String PAYLOAD = "payload";
    private static final String TIMESTAMP = "ts";
    
    private final DynamoDbClient dynamoDbClient;
    
    @Inject
    public DynamoCheckpointRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
        createTableIfNotExists();
    }
    
    @Override
    public void save(String workflowId, String step, String payloadJson) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(PK, AttributeValue.builder().s(workflowId).build());
        item.put(SK, AttributeValue.builder().s(step).build());
        
        if (payloadJson != null) {
            item.put(PAYLOAD, AttributeValue.builder().s(payloadJson).build());
        }
        
        String timestamp = Instant.now().toString();
        item.put(TIMESTAMP, AttributeValue.builder().s(timestamp).build());
        
        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
        
        dynamoDbClient.putItem(request);
    }
    
    @Override
    public Optional<Checkpoint> last(String workflowId) {
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("#pk = :pkValue")
                .expressionAttributeNames(Map.of("#pk", PK))
                .expressionAttributeValues(Map.of(":pkValue", AttributeValue.builder().s(workflowId).build()))
                .scanIndexForward(false) // descending order by sort key
                .limit(1) // get only the latest checkpoint
                .build();
        
        QueryResponse response = dynamoDbClient.query(queryRequest);
        
        if (response.items().isEmpty()) {
            return Optional.empty();
        }
        
        Map<String, AttributeValue> item = response.items().get(0);
        String step = item.get(SK).s();
        String payload = item.containsKey(PAYLOAD) ? item.get(PAYLOAD).s() : null;
        Instant ts = Instant.parse(item.get(TIMESTAMP).s());
        
        return Optional.of(new Checkpoint(workflowId, step, payload, ts));
    }
    
    private void createTableIfNotExists() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(TABLE_NAME).build());
            // Table exists, no need to create
        } catch (ResourceNotFoundException e) {
            // Table doesn't exist, create it
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder().attributeName(PK).keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName(SK).keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName(PK).attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName(SK).attributeType(ScalarAttributeType.S).build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();
            
            dynamoDbClient.createTable(createTableRequest);
        }
    }
}
