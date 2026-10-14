package com.elitefitness.gym_management;

import com.elitefitness.gym_management.model.Member;
import com.elitefitness.gym_management.model.Plan;
import com.elitefitness.gym_management.repository.MemberRepository;
import com.elitefitness.gym_management.repository.PlanRepository;
import com.elitefitness.gym_management.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GymControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private StaffRepository staffRepository;

    @BeforeEach
    void resetData() {
        memberRepository.deleteAll();
        staffRepository.deleteAll();
        planRepository.deleteAll();
        planRepository.save(plan("PLN-MONTHLY", "Monthly", 1, 1500));
        planRepository.save(plan("PLN-YEARLY", "Yearly", 12, 12000));
    }

    @Test
    void plansSupportCreateListUpdateAndDelete() throws Exception {
        mockMvc.perform(post("/api/plans").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"planId":"PLN-QUARTERLY","planName":"Quarterly","durationMonths":3,"price":4000}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planName").value("Quarterly"));

        mockMvc.perform(get("/api/plans"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].planId", hasItem("PLN-QUARTERLY")));

        mockMvc.perform(put("/api/plans/PLN-QUARTERLY").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"planName":"Quarterly Plus","durationMonths":3,"price":4500}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planId").value("PLN-QUARTERLY"))
            .andExpect(jsonPath("$.planName").value("Quarterly Plus"));

        mockMvc.perform(delete("/api/plans/PLN-QUARTERLY"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/plans"))
            .andExpect(jsonPath("$[*].planId", not(hasItem("PLN-QUARTERLY"))));
    }

    @Test
    void membersSupportCreateListUpdateAndDeleteWithoutChangingJoinDate() throws Exception {
        mockMvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"memberId":"ELT-TEST","fullName":"Taylor Member","status":"Active",
                     "joinDate":"2024-02-03","plan":{"planId":"PLN-MONTHLY"}}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.plan.planId").value("PLN-MONTHLY"));

        mockMvc.perform(get("/api/members"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].memberId", hasItem("ELT-TEST")));

        mockMvc.perform(put("/api/members/ELT-TEST").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fullName":"Taylor Updated","status":"Expired","plan":{"planId":"PLN-YEARLY"}}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Taylor Updated"))
            .andExpect(jsonPath("$.joinDate").value("2024-02-03"))
            .andExpect(jsonPath("$.plan.planId").value("PLN-YEARLY"));

        mockMvc.perform(delete("/api/members/ELT-TEST"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/members"))
            .andExpect(jsonPath("$[*].memberId", not(hasItem("ELT-TEST"))));
    }

    @Test
    void pipelineRevertDemoRejectsMembersWithoutAnExistingPlan() throws Exception {
        mockMvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"memberId":"ELT-NO-PLAN","fullName":"Invalid Member","status":"Active",
                     "plan":{"planId":"PLN-MISSING"}}
                    """))
            .andExpect(status().isBadRequest());
        assertFalse(memberRepository.existsById("ELT-NO-PLAN"));
    }

    @Test
    void staffSupportCreateListUpdateAndDelete() throws Exception {
        mockMvc.perform(post("/api/staff").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"staffId":"STF-TEST","staffName":"Jordan Trainer",
                     "specialization":"Strength","shiftTiming":"Morning"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.staffName").value("Jordan Trainer"));

        mockMvc.perform(get("/api/staff"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].staffId", hasItem("STF-TEST")));

        mockMvc.perform(put("/api/staff/STF-TEST").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"staffName":"Jordan Updated",
                     "specialization":"Mobility","shiftTiming":"Evening"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.staffId").value("STF-TEST"))
            .andExpect(jsonPath("$.staffName").value("Jordan Updated"));

        mockMvc.perform(delete("/api/staff/STF-TEST"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/staff"))
            .andExpect(jsonPath("$[*].staffId", not(hasItem("STF-TEST"))));
    }

    @Test
    void plansInUseCannotBeDeleted() throws Exception {
        Member member = new Member();
        member.setMemberId("ELT-REFERENCED");
        member.setFullName("Plan User");
        member.setStatus("Active");
        member.setJoinDate(LocalDate.of(2024, 1, 1));
        member.setPlan(planRepository.findById("PLN-MONTHLY").orElseThrow());
        memberRepository.save(member);

        mockMvc.perform(delete("/api/plans/PLN-MONTHLY"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(
                "This record already exists or is referenced by another record."));
    }

    private Plan plan(String id, String name, int months, double price) {
        Plan plan = new Plan();
        plan.setPlanId(id);
        plan.setPlanName(name);
        plan.setDurationMonths(months);
        plan.setPrice(price);
        return plan;
    }
}
