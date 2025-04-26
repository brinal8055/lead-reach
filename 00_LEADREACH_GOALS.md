# LeadReach – Weekend Demo

**Elevator-pitch**  
An AI sales-assistant backend that moves a Lead through three gRPC micro-services—**Lead**, **Enrichment**, **Outreach**—orchestrated by a **Workflow Orchestrator** with DynamoDB-backed checkpoints. 100 % Dockerised, CI-ready, idempotent, and fault-tolerant.

**Service Map**
| Service | Language | Purpose |
|---------|----------|---------|
| lead-service         | Java (Micronaut) | CRUD + status for leads |
| enrichment-service   | Java (Micronaut) | Simulate 3rd-party data pull (50 % failure toggle) |
| outreach-service     | Java (Micronaut) | Simulate sending an email / CRM update |
| orchestrator         | Java (Micronaut) | Drives workflow, handles retries, stores checkpoints |
| local-dynamodb       | Docker image     | Stores `WorkflowCheckpoint` & `Lead` |
| local-sqs (optional) | LocalStack       | Message queue demo |

**Quality Bar**
- gRPC for all internal APIs (proto files committed)
- Idempotent orchestration with **exact-once** semantics
- Dockerfile per service + single `docker-compose.yml`
- GitHub Actions pipeline: build, unit tests, docker push
- ≥80 % line coverage with JUnit 5 + Testcontainers

**Out of scope**
UI, Kubernetes, auth, real e-mail APIs, non-Java languages.
