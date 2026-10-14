package com.elitefitness.gym_management.config;

import com.elitefitness.gym_management.model.Plan;
import com.elitefitness.gym_management.repository.PlanRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(name="gym.seed-plans", havingValue="true")
public class SamplePlans {
    @Bean
    ApplicationRunner seedPlans(PlanRepository plans) {
        return args -> {
            if (plans.count() != 0) return; // Preserve existing data and edits on redeploy.
            add(plans, "PLN-1", "Annual Pro VIP", 12, 12000);
            add(plans, "PLN-2", "Monthly Basic", 1, 1500);
            add(plans, "PLN-3", "Quarterly Elite", 3, 4000);
        };
    }
    private void add(PlanRepository plans, String id, String name, int months, double price) {
        Plan plan = new Plan(); plan.setPlanId(id); plan.setPlanName(name);
        plan.setDurationMonths(months); plan.setPrice(price); plans.save(plan);
    }
}
