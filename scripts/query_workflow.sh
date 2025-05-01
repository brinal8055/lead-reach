#!/bin/bash

# This script creates and runs a Java program in the orchestrator container to query DynamoDB

# Check if a workflow ID was provided as an argument
if [ -z "$1" ]; then
  # If no workflow ID was provided, list all workflows
  WORKFLOW_ID=""
  echo "No workflow ID provided, listing all checkpoints..."
else
  # If a workflow ID was provided, query for that specific workflow
  WORKFLOW_ID="$1"
  echo "Querying checkpoints for workflow ID: $WORKFLOW_ID"
fi

# Create a Java program to query DynamoDB
cat > /tmp/QueryWorkflow.java << EOF
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class QueryWorkflow {
    public static void main(String[] args) {
        String workflowId = args.length > 0 ? args[0] : null;
        
        try {
            // Configure DynamoDB client
            DynamoDbClient client = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://dynamodb:8000"))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("dummy", "dummy")))
                .build();
            
            if (workflowId != null && !workflowId.isEmpty()) {
                // Query for specific workflow
                Map<String, AttributeValue> expressionValues = new HashMap<>();
                expressionValues.put(":wid", AttributeValue.builder().s(workflowId).build());
                
                QueryRequest request = QueryRequest.builder()
                    .tableName("workflow_checkpoints")
                    .keyConditionExpression("workflowId = :wid")
                    .expressionAttributeValues(expressionValues)
                    .build();
                
                QueryResponse response = client.query(request);
                System.out.println("Found " + response.count() + " checkpoints for workflow: " + workflowId);
                
                for (Map<String, AttributeValue> item : response.items()) {
                    System.out.println("\nCheckpoint:");
                    System.out.println("  Workflow ID: " + item.get("workflowId").s());
                    System.out.println("  Step: " + item.get("step").s());
                    System.out.println("  Timestamp: " + item.get("ts").s());
                    if (item.containsKey("payload")) {
                        System.out.println("  Payload: " + item.get("payload").s());
                    }
                }
            } else {
                // Scan all checkpoints
                ScanRequest request = ScanRequest.builder()
                    .tableName("workflow_checkpoints")
                    .build();
                
                ScanResponse response = client.scan(request);
                System.out.println("Found " + response.count() + " total checkpoints");
                
                for (Map<String, AttributeValue> item : response.items()) {
                    System.out.println("\nCheckpoint:");
                    System.out.println("  Workflow ID: " + item.get("workflowId").s());
                    System.out.println("  Step: " + item.get("step").s());
                    System.out.println("  Timestamp: " + item.get("ts").s());
                    if (item.containsKey("payload")) {
                        System.out.println("  Payload: " + item.get("payload").s());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
EOF

# Copy the Java file to the orchestrator container
docker cp /tmp/QueryWorkflow.java lead-reach-orchestrator-1:/tmp/

# Compile and run the Java program in the orchestrator container
docker-compose exec orchestrator sh -c "cd /tmp && javac -cp /app/app.jar QueryWorkflow.java && java -cp /app/app.jar:/tmp QueryWorkflow \"$WORKFLOW_ID\""

echo ""
echo "Usage:"
echo "  ./query_workflow.sh                  # List all checkpoints"
echo "  ./query_workflow.sh <workflow_id>    # List checkpoints for a specific workflow"
echo ""
echo "Example workflow IDs:"
echo "  16e5a9d3-ecc7-4489-bfed-69e3ba5ed874    # From our previous test"
