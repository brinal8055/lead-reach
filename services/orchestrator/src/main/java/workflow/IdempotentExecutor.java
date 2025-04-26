package workflow;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Singleton
public class IdempotentExecutor {
    private final CheckpointRepository repo;
    
    @Inject
    public IdempotentExecutor(CheckpointRepository repo) {
        this.repo = repo;
    }
    
    public <T> T run(
            String workflowId,
            String step,
            Supplier<T> body,
            Duration retryBackoff
    ) {
        // Check if this step was already completed
        Optional<Checkpoint> lastCheckpoint = repo.last(workflowId);
        if (lastCheckpoint.isPresent() && lastCheckpoint.get().step().equals(step)) {
            // Step already completed, return null as we're skipping execution
            return null;
        }
        
        // Execute with retries
        T result = executeWithRetries(body, retryBackoff);
        
        // Save checkpoint after successful execution
        repo.save(workflowId, step, result != null ? result.toString() : null);
        
        return result;
    }
    
    private <T> T executeWithRetries(Supplier<T> body, Duration retryBackoff) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount <= maxRetries) {
            try {
                return body.get();
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                
                if (retryCount > maxRetries) {
                    break;
                }
                
                // Exponential backoff
                long sleepTime = retryBackoff.toMillis() * (long) Math.pow(2, retryCount - 1);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry backoff", ie);
                }
            }
        }
        
        throw new RuntimeException("Failed after " + maxRetries + " retries", lastException);
    }
}
