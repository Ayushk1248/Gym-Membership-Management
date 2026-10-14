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

Open http://localhost:8080/ for the existing frontend. The frontend uses relative /api URLs on the same origin. The final Compose app port is 8080. Health includes the database check. Metrics expose JVM and HTTP metrics plus gym_members_count, gym_members_active, gym_plans_count, and gym_staff_count. The four custom gauges query existing tables at scrape time; they are current record counts, not cumulative counters. No payment or attendance functionality is introduced.

Compose enables idempotent sample data for Monthly, Quarterly, and Yearly plans, four linked active members, and six staff. Missing sample records are added on startup; existing records and user edits are retained. The member form loads its plan choices from the API. CRUD validation errors appear in the frontend instead of failing silently.

The production image runs Java 21 as UID 10001 and checks /health. CI tests run in the Docker test image with an isolated H2 database, so no local MySQL or credentials are needed. The retained context-load test is supplemented by focused member, plan, and staff API tests. `pipelineRevertDemoRejectsMembersWithoutAnExistingPlan` is the named test used to demonstrate a failed build followed by a Git revert.

Phase 2 now enables Prometheus, Grafana, and node-exporter by default with scrape/dashboard provisioning; see phase-2.md. They run exclusively as Docker images. On Windows Docker Desktop, node-exporter observes the Linux Docker VM, not native Windows host metrics.

Actuator path mapping follows https://docs.spring.io/spring-boot/reference/actuator/monitoring.html . Linux node-exporter container setup follows https://github.com/prometheus/node_exporter .

The database password present in the original commit has been removed from both current properties files. Existing Git history still contains that original credential; rotate it if it is used anywhere else. No history rewrite is performed.

## Jenkins CI/CD

The existing Windows Jenkins instance remains outside Compose at http://localhost:8081/. Create a Pipeline item named `GymMS-CI-CD` using this repository, `*/master`, and `Jenkinsfile`; enable SCM polling every two minutes (`*/2 * * * *`). The Jenkinsfile uses the Docker test image for Java 21 and Maven, so the Jenkins host itself does not need a JDK. Its agent does need access to Docker Desktop and the `docker compose` plugin.

Add a Jenkins Secret file credential with ID `gymms-compose-env` containing the local ignored `.env` file. This supplies the existing Compose database and Grafana credentials to deployments without committing secrets or resetting persistent database credentials. Install/enable Pipeline and JUnit plugins if missing so the pipeline and published Surefire reports work.

The pipeline checks out the configured branch, builds/runs the H2 test image, publishes JUnit results, builds `gymms-app:local`, updates the existing `gymms` Compose project, and retries `/health` on port 8080. Do not create a second Compose project or delete its named volumes.
