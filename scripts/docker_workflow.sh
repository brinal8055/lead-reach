#!/bin/bash

# This script uses the orchestrator Docker container to send a workflow start request

# Check if a lead ID was provided as an argument
if [ -z "$1" ]; then
  # If no lead ID was provided, use John Doe's ID
  LEAD_ID="70018eab-d0b1-465f-ba9d-635faa7de3cf"
  echo "No lead ID provided, using John Doe's ID: $LEAD_ID"
else
  LEAD_ID="$1"
  echo "Using provided lead ID: $LEAD_ID"
fi

echo "Sending workflow start request for lead ID: $LEAD_ID"

# Create a Java program inside the Docker container to make a gRPC request
JAVA_CODE=$(cat <<EOF
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import leadreach.OrchestratorGrpc;
import leadreach.OrchestratorService.StartWorkflowRequest;
import leadreach.OrchestratorService.StartWorkflowResponse;

public class WorkflowClient {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java WorkflowClient <lead_id>");
            System.exit(1);
        }
        
        String leadId = args[0];
        
        // Create a channel to the orchestrator service
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 50051)
            .usePlaintext()
            .build();
        
        try {
            // Create a blocking stub
            OrchestratorGrpc.OrchestratorBlockingStub stub = OrchestratorGrpc.newBlockingStub(channel);
            
            // Create the request
            StartWorkflowRequest request = StartWorkflowRequest.newBuilder()
                .setLeadId(leadId)
                .build();
            
            System.out.println("Sending StartWorkflow request for lead ID: " + leadId);
            
            // Make the call
            StartWorkflowResponse response = stub.startWorkflow(request);
            
            System.out.println("Workflow started successfully!");
            System.out.println("Workflow ID: " + response.getWorkflowId());
            
        } catch (Exception e) {
            System.err.println("Error calling orchestrator service: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Shutdown the channel
            channel.shutdown();
        }
    }
}
EOF
)

# Use docker exec to run a command in the orchestrator container
echo "Creating Java client in the orchestrator container..."
docker-compose exec orchestrator sh -c "cd /tmp && echo '$JAVA_CODE' > WorkflowClient.java && javac -cp /app/app.jar WorkflowClient.java && java -cp /app/app.jar:/tmp WorkflowClient $LEAD_ID"

echo "Request sent. Check the output above for the response."
