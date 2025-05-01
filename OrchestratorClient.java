import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import leadreach.OrchestratorGrpc;
import leadreach.OrchestratorService.StartWorkflowRequest;
import leadreach.OrchestratorService.StartWorkflowResponse;

/**
 * Simple client to test the Orchestrator service.
 * 
 * Usage: java OrchestratorClient <lead_id>
 * 
 * Example: java OrchestratorClient 70018eab-d0b1-465f-ba9d-635faa7de3cf
 */
public class OrchestratorClient {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java OrchestratorClient <lead_id>");
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
