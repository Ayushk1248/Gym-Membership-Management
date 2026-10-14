# Phase 1 verification

The root pom.xml and src/ are canonical: Docker builds from the repository root and packages the root frontend files into Spring Boot static resources. The nested gym-management/ copy is preserved, excluded from the Docker context, and receives only credential externalization. Do not develop both copies independently.

## Local setup

Copy .env.example to .env and replace the password placeholders with unique values. The generated local .env used during verification is ignored by Git and Docker. Never commit it. Existing database volumes retain their original passwords; editing .env does not rotate them.

Run from the repository root:

```powershell
docker compose config --quiet
docker compose up -d --build app db
docker compose ps
curl.exe --fail http://localhost:8080/health
curl.exe --fail http://localhost:8080/metrics
```

Port 8080 must be available first. If your IDE is running the app on 8080, stop that app before launching Compose. Jenkins remains on 8081. The container database is new and separate from your local MySQL data; no host database port is published. Do not use docker compose down -v unless you intend to delete the container database.

Open http://localhost:8080/ for the existing frontend. The frontend intentionally retains its localhost:8080 API URLs, so use it on this computer. Health includes the database check. Metrics expose JVM and HTTP metrics plus gym_members_count, gym_members_active, gym_plans_count, and gym_staff_count. The four custom gauges query existing tables at scrape time; they are current record counts, not cumulative counters. No payment or attendance functionality is introduced.

The production image runs Java 21 as UID 10001 and checks /health. Its package step skips tests; the separate test target runs Maven tests in CI. H2 test configuration and focused tests are scheduled for Phase 3; the test target is not yet DB-independent.

Phase 2 now enables Prometheus, Grafana, and node-exporter by default with scrape/dashboard provisioning; see phase-2.md. They run exclusively as Docker images. On Windows Docker Desktop, node-exporter observes the Linux Docker VM, not native Windows host metrics.

Actuator path mapping follows https://docs.spring.io/spring-boot/reference/actuator/monitoring.html . Linux node-exporter container setup follows https://github.com/prometheus/node_exporter .

The database password present in the original commit has been removed from both current properties files. Existing Git history still contains that original credential; rotate it if it is used anywhere else. No history rewrite is performed.
