package com.leadreach;

import io.grpc.stub.StreamObserver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import leadreach.EnrichmentServiceGrpc;
import leadreach.EnrichmentServiceOuterClass.EnrichRequest;
import leadreach.EnrichmentServiceOuterClass.EnrichResponse;
import leadreach.LeadOuterClass.Lead;
import leadreach.LeadServiceGrpc;
import leadreach.LeadServiceOuterClass.GetLeadRequest;
import leadreach.LeadServiceOuterClass.UpdateStatusRequest;
import leadreach.OrchestratorGrpc;
import leadreach.OrchestratorService.StartWorkflowRequest;
import leadreach.OrchestratorService.StartWorkflowResponse;
import leadreach.OutreachServiceGrpc;
import leadreach.OutreachServiceOuterClass.SendRequest;
import leadreach.OutreachServiceOuterClass.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workflow.Checkpoint;
import workflow.CheckpointRepository;
import workflow.IdempotentExecutor;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class OrchestratorServiceImpl extends OrchestratorGrpc.OrchestratorImplBase {
    
    private static final Logger LOG = LoggerFactory.getLogger(OrchestratorServiceImpl.class);
    
    // Step names for checkpointing
    private static final String STEP_LEAD_FETCHED = "LEAD_FETCHED";
    private static final String STEP_ENRICHMENT_DONE = "ENRICHMENT_DONE";
    private static final String STEP_EMAIL_SENT = "EMAIL_SENT";
    
    private final LeadServiceGrpc.LeadServiceBlockingStub leadService;
    private final EnrichmentServiceGrpc.EnrichmentServiceBlockingStub enrichmentService;
    private final OutreachServiceGrpc.OutreachServiceBlockingStub outreachService;
    private final CheckpointRepository checkpointRepository;
    private final IdempotentExecutor idempotentExecutor;
    
    @Inject
    public OrchestratorServiceImpl(
            LeadServiceGrpc.LeadServiceBlockingStub leadService,
            EnrichmentServiceGrpc.EnrichmentServiceBlockingStub enrichmentService,
            OutreachServiceGrpc.OutreachServiceBlockingStub outreachService,
            CheckpointRepository checkpointRepository,
            IdempotentExecutor idempotentExecutor) {
        this.leadService = leadService;
        this.enrichmentService = enrichmentService;
        this.outreachService = outreachService;
        this.checkpointRepository = checkpointRepository;
        this.idempotentExecutor = idempotentExecutor;
    }
    
    @Override
    public void startWorkflow(StartWorkflowRequest request, StreamObserver<StartWorkflowResponse> responseObserver) {
        String leadId = request.getLeadId();
        // Generate a new workflowId (ULID)
        String workflowId = UUID.randomUUID().toString(); // Using UUID as a placeholder for ULID
        
        LOG.info("Starting workflow {} for lead {}", workflowId, leadId);
        
        try {
            // Check if there's an existing checkpoint for this workflow
            Optional<Checkpoint> lastCheckpoint = checkpointRepository.last(workflowId);
            String lastCompletedStep = lastCheckpoint.map(Checkpoint::step).orElse(null);
            
            // Step 1: Fetch lead from lead-service
            final Lead initialLead;
            if (lastCompletedStep == null || !lastCompletedStep.equals(STEP_LEAD_FETCHED)) {
                LOG.info("Fetching lead {} from lead-service", leadId);
                initialLead = idempotentExecutor.run(
                        workflowId,
                        STEP_LEAD_FETCHED,
                        () -> fetchLead(workflowId, leadId),
                        Duration.ofSeconds(1)
                );
                LOG.info("Lead fetched successfully: {}", initialLead.getId());
            } else {
                LOG.info("Step LEAD_FETCHED already completed, skipping");
                // Retrieve lead from the last checkpoint if needed
                initialLead = fetchLead(workflowId, leadId);
            }
            
            // Step 2: Enrich lead using enrichment-service with retry
            final Lead enrichedLead;
            if (lastCompletedStep == null || !lastCompletedStep.equals(STEP_ENRICHMENT_DONE) && 
                !lastCompletedStep.equals(STEP_EMAIL_SENT)) {
                LOG.info("Enriching lead {}", leadId);
                enrichedLead = idempotentExecutor.run(
                        workflowId,
                        STEP_ENRICHMENT_DONE,
                        () -> enrichLead(workflowId, leadId),
                        Duration.ofSeconds(1) // 1s backoff for retries
                );
                LOG.info("Lead enriched successfully: {}", enrichedLead.getId());
            } else {
                LOG.info("Step ENRICHMENT_DONE already completed, skipping");
                // If needed, retrieve enriched lead data
                enrichedLead = initialLead;
            }
            
            // Step 3: Send lead using outreach-service
            if (lastCompletedStep == null || !lastCompletedStep.equals(STEP_EMAIL_SENT)) {
                LOG.info("Sending outreach for lead {}", leadId);
                final Lead finalLead = enrichedLead;
                boolean success = idempotentExecutor.run(
                        workflowId,
                        STEP_EMAIL_SENT,
                        () -> sendOutreach(workflowId, finalLead),
                        Duration.ofSeconds(1)
                );
                LOG.info("Outreach sent successfully: {}", success);
                
                // Update lead status to CONTACTED
                if (success) {
                    LOG.info("Updating lead status to CONTACTED");
                    updateLeadStatus(workflowId, leadId, "CONTACTED");
                }
            } else {
                LOG.info("Step EMAIL_SENT already completed, skipping");
            }
            
            // Return the workflow ID to the client
            StartWorkflowResponse response = StartWorkflowResponse.newBuilder()
                    .setWorkflowId(workflowId)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            LOG.error("Error in workflow execution for lead {}: {}", leadId, e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
    
    private Lead fetchLead(String workflowId, String leadId) {
        LOG.debug("Executing fetchLead for workflow {} and lead {}", workflowId, leadId);
        GetLeadRequest request = GetLeadRequest.newBuilder()
                .setId(leadId)
                .build();
        return leadService.getLead(request);
    }
    
    private Lead enrichLead(String workflowId, String leadId) {
        LOG.debug("Executing enrichLead for workflow {} and lead {}", workflowId, leadId);
        EnrichRequest request = EnrichRequest.newBuilder()
                .setId(leadId)
                .build();
        EnrichResponse response = enrichmentService.enrich(request);
        return response.getLead();
    }
    
    private boolean sendOutreach(String workflowId, Lead lead) {
        LOG.debug("Executing sendOutreach for workflow {} and lead {}", workflowId, lead.getId());
        SendRequest request = SendRequest.newBuilder()
                .setLead(lead)
                .build();
        SendResponse response = outreachService.send(request);
        return response.getSuccess();
    }
    
    private Lead updateLeadStatus(String workflowId, String leadId, String status) {
        LOG.debug("Executing updateLeadStatus for workflow {} and lead {} to status {}", 
                workflowId, leadId, status);
        UpdateStatusRequest request = UpdateStatusRequest.newBuilder()
                .setId(leadId)
                .setStatus(status)
                .build();
        return leadService.updateStatus(request);
    }
}
