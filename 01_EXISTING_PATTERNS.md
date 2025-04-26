# CollateralStack Patterns Re-used in LeadReach

> We will reuse a subset of the production-grade workflow helpers that already live in **collateralstack-commons**.  
> Only the interfaces below are exposed to Cline; the concrete impls remain unchanged and are added as a Gradle dependency.

```java
package commons.workflow;

public interface WorkflowManager {
    // Persist <workflowId, stepName, payload, timestamp>
    void saveCheckpoint(String workflowId, String stepName, String payloadJson);

    // Return the last successfully completed step (null if none)
    Checkpoint getLastCheckpoint(String workflowId);

    // Wrapper to run a step idempotently
    <T> T runStep(
        String workflowId,
        String stepName,
        Supplier<T> body,
        Duration retryBackoff
    ) throws WorkflowException;
}
