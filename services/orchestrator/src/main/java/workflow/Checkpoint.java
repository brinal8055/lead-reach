package workflow;

import java.time.Instant;

public record Checkpoint(String workflowId, String step, String payload, Instant ts) {}
