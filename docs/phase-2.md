# Phase 2 monitoring

## Start and sign in

From the repository root, with Docker Desktop running and .env configured:

```powershell
docker compose up -d --build
docker compose ps
```

- App: http://localhost:8080/
- Health: http://localhost:8080/health
- Metrics: http://localhost:8080/metrics
- Prometheus: http://localhost:9090/
- Targets: http://localhost:9090/targets
- Grafana login: http://localhost:3000/login
- Provisioned dashboard: http://localhost:3000/d/gymms-overview/gymms-overview

Grafana username is GRAFANA_ADMIN_USER in .env (default admin); password is GRAFANA_ADMIN_PASSWORD in .env. Display only the Grafana entries locally if needed:

```powershell
Get-Content .env | Select-String '^GRAFANA_ADMIN_(USER|PASSWORD)='
```

Do not capture passwords in screenshots. Grafana uses these variables only when initializing its database. Later edits to .env do not reset an existing Grafana password. MySQL likewise retains its initialized passwords in the named volume. Never remove volumes to resolve a login problem if you need their data.

Prometheus, Grafana, and node-exporter run only as Docker images. No host installation is needed. Prometheus and Grafana bind only to localhost; node-exporter and MySQL have no published host ports. Jenkins remains on 8081.

## Provisioning

Prometheus scrapes app:8080/metrics, node-exporter:9100/metrics, and localhost:9090/metrics every 15 seconds. It retains samples for 15 days in a named volume. Configuration is mounted read-only from monitoring/prometheus/prometheus.yml. After editing it, validate with the command below and restart Prometheus.

```powershell
docker compose exec -T prometheus promtool check config /etc/prometheus/prometheus.yml
docker compose restart prometheus
```

Grafana provisions the Prometheus datasource (UID gymms-prometheus) and GymMS Overview dashboard (UID gymms-overview) from the monitoring/grafana directory. The datasource uses the internal Compose URL http://prometheus:9090, not localhost. Dashboard JSON is the source of truth; edit it in Git. Grafana scans for changes every 30 seconds. Datasource changes require a Grafana restart.

The dashboard has ten panels: app scrape status, API request rate, API p95 latency, Linux VM CPU/memory/disk usage, member records, active members, subscription plans, and staff records. Scrape status means metrics are reachable; /health independently verifies DB readiness. Zero gym counts are valid for a new empty database.

On Windows, node-exporter measures the Linux VM running Docker Desktop, not native Windows CPU, memory, or C: drive space. Disk series are per persistent Linux block device, deduplicated across bind mounts. No payment or attendance metrics are invented.

## Generate traffic and inspect PromQL

Wait at least 30 seconds after startup for two scrapes. API rate and latency need requests; no traffic can legitimately leave latency blank. To generate read-only demo traffic:

```powershell
1..60 | ForEach-Object {
    Invoke-WebRequest http://localhost:8080/api/plans | Out-Null
    Start-Sleep -Seconds 1
}
```

On http://localhost:9090/query use:

```promql
up
sum(rate(http_server_requests_seconds_count{job="gymms",uri=~"/api/.*"}[2m]))
histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job="gymms",uri=~"/api/.*"}[2m])))
gym_members_active{job="gymms"}
100 * (1 - avg(rate(node_cpu_seconds_total{job="node-exporter",mode="idle"}[2m])))
```

## Screenshot checklist for Phase 2

1. Terminal: docker compose ps showing app, DB, Prometheus, Grafana, and node-exporter. Include health states and port mappings.
2. Browser: /health showing UP and /metrics showing gym_* values.
3. Prometheus /targets: all three jobs (gymms, node-exporter, prometheus) UP.
4. Prometheus query view: up showing all three values equal to 1.
5. Prometheus query view: request rate or p95 expression above after demo traffic.
6. Grafana GymMS Overview: top panels showing app UP, request rate, latency, and Linux VM resource charts.
7. Grafana lower panels: all four gym business gauges. Zero is expected until you enter app data.
8. GitHub Phase 2 PR: feature/monitoring targeting master, commits and changed files. Keep it open for review.

For a useful Grafana screenshot, select Last 15 minutes and wait a few minutes for the time series to fill. Do not include .env, Docker inspect environment output, or password screens in your report.

References: https://grafana.com/docs/grafana/latest/administration/provisioning/ and https://prometheus.io/docs/prometheus/latest/getting_started/ .
