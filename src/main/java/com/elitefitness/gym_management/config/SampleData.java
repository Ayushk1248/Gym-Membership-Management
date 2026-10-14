package com.elitefitness.gym_management.config;

import com.elitefitness.gym_management.model.Member;
import com.elitefitness.gym_management.model.Plan;
import com.elitefitness.gym_management.model.Staff;
import com.elitefitness.gym_management.repository.MemberRepository;
import com.elitefitness.gym_management.repository.PlanRepository;
import com.elitefitness.gym_management.repository.StaffRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
@ConditionalOnProperty(name = "gym.seed-data", havingValue = "true")
public class SampleData {
    @Bean
    ApplicationRunner seedSampleData(PlanRepository plans, MemberRepository members, StaffRepository staff) {
        return args -> {
            seedPlan(plans, "PLN-2", "Monthly", 1, 1500, "Monthly Basic");
            seedPlan(plans, "PLN-3", "Quarterly", 3, 4000, "Quarterly Elite");
            seedPlan(plans, "PLN-1", "Yearly", 12, 12000, "Annual Pro VIP");

            seedMember(members, plans, "ELT-SEED-001", "Ayush Kushwaha", "PLN-1");
            seedMember(members, plans, "ELT-SEED-002", "Palak Lakhani", "PLN-2");
            seedMember(members, plans, "ELT-SEED-003", "Vedprakash Mishra", "PLN-3");
            seedMember(members, plans, "ELT-SEED-004", "John Doe", "PLN-2");

            seedStaff(staff, "STF-SEED-001", "Ayush Kushwaha", "Head Trainer", "Morning");
            seedStaff(staff, "STF-SEED-002", "Palak Lakhani", "Trainer", "Morning");
            seedStaff(staff, "STF-SEED-003", "Vedprakash Mishra", "Receptionist", "Afternoon");
            seedStaff(staff, "STF-SEED-004", "John Doe", "Manager", "Full day");
            seedStaff(staff, "STF-SEED-005", "Priya Sharma", "Trainer", "Evening");
            seedStaff(staff, "STF-SEED-006", "Arjun Patel", "Fitness Consultant", "Evening");
        };
    }

    private void seedPlan(PlanRepository plans, String id, String name, int months, double price,
                         String previousSeedName) {
        Plan plan = plans.findById(id).orElseGet(Plan::new);
        if (plan.getPlanId() == null) {
            plan.setPlanId(id);
        } else if (!previousSeedName.equals(plan.getPlanName())) {
            return;
        }
        plan.setPlanName(name);
        plan.setDurationMonths(months);
        plan.setPrice(price);
        plans.save(plan);
    }

    private void seedMember(MemberRepository members, PlanRepository plans, String id, String name, String planId) {
        if (members.existsById(id)) {
            return;
        }
        Member member = new Member();
        member.setMemberId(id);
        member.setFullName(name);
        member.setStatus("Active");
        member.setJoinDate(LocalDate.now());
        member.setPlan(plans.findById(planId).orElseThrow());
        members.save(member);
    }

    private void seedStaff(StaffRepository staff, String id, String name, String role, String shift) {
        if (staff.existsById(id)) {
            return;
        }
        Staff member = new Staff();
        member.setStaffId(id);
        member.setStaffName(name);
        member.setSpecialization(role);
        member.setShiftTiming(shift);
        staff.save(member);
    }
}
