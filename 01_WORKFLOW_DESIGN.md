# LeadReach Checkpointing & Workflow Library

**Goal**  
Provide ultra-lean, self-contained utilities so *only the Orchestrator* persists workflow state.  
Other micro-services stay stateless.

---

## DynamoDB “workflow_checkpoints” table (single-table design)

| PK (string) | SK (string) | payload (string) | ts (ISO-8601) |
|-------------|-------------|------------------|---------------|
| workflowId  | stepName    | optional JSON    | updatedAt     |

*   **PK:** `workflowId` (ULID or UUID v7).
*   **SK:** name of completed step (`LEAD_FETCHED`, `ENRICHED`, `EMAILED`).
*   CQRS style: writes only from Orchestrator.

---

## Library API (package `workflow`)

```java
public interface CheckpointRepository {
  void save(String workflowId, String step, String payloadJson);
  Optional<Checkpoint> last(String workflowId);
}

public record Checkpoint(String workflowId, String step, String payload, Instant ts) {}

public class DynamoCheckpointRepository implements CheckpointRepository {
  // implements Table name = workflow_checkpoints
}

public class IdempotentExecutor {
  private final CheckpointRepository repo;
  public <T> T run(
       String workflowId,
       String step,
       Supplier<T> body,
       Duration retryBackoff
  ) { /*  retry + save checkpoint  */ }
}
