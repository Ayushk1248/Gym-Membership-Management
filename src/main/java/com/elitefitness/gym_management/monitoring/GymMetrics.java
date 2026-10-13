package com.elitefitness.gym_management.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Read-only gauges from existing tables; no changes to gym CRUD behavior. */
@Component
public class GymMetrics {
    public GymMetrics(MeterRegistry registry, JdbcTemplate jdbc) {
        register(registry, jdbc, "gym.members.count", "Current member records",
                "SELECT COUNT(*) FROM members");
        register(registry, jdbc, "gym.members.active", "Members whose status is Active",
                "SELECT COUNT(*) FROM members WHERE LOWER(status) = 'active'");
        register(registry, jdbc, "gym.plans.count", "Current subscription plans",
                "SELECT COUNT(*) FROM plans");
        register(registry, jdbc, "gym.staff.count", "Current staff records",
                "SELECT COUNT(*) FROM staff");
    }

    private static void register(MeterRegistry registry, JdbcTemplate jdbc,
                                 String name, String description, String query) {
        Gauge.builder(name, jdbc, db -> db.queryForObject(query, Long.class).doubleValue())
                .description(description)
                .register(registry);
    }
}

