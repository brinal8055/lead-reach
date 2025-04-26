package workflow;

import java.util.Optional;

public interface CheckpointRepository {
  void save(String workflowId, String step, String payloadJson);
  Optional<Checkpoint> last(String workflowId);
}
