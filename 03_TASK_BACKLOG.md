# Task Backlog ✅ / 🔜

## GENERIC
- [x] Repo scaffolding, prep files, docker-compose
- [ ] GitHub Actions CI (gradle build / test / docker)

---

## lead-service
- [ ] Proto impl skeleton
- [ ] Business logic (CRUD in DynamoDB)
- [ ] Unit tests

## enrichment-service
- [ ] Proto impl skeleton
- [ ] Business logic (+ random failure mode)
- [ ] Unit tests

## outreach-service
- [ ] Proto impl skeleton
- [ ] Business logic (log “email sent”)
- [ ] Unit tests

## orchestrator
- [ ] Proto impl skeleton
- [ ] Idempotent workflow logic using `WorkflowManager`
- [ ] Failure & retry handling
- [ ] Integration tests (happy + flaky path)
