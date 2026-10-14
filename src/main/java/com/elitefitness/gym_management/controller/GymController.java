package com.elitefitness.gym_management.controller;

import com.elitefitness.gym_management.model.Member;
import com.elitefitness.gym_management.model.Plan;
import com.elitefitness.gym_management.model.Staff;
import com.elitefitness.gym_management.repository.MemberRepository;
import com.elitefitness.gym_management.repository.PlanRepository;
import com.elitefitness.gym_management.repository.StaffRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GymController {
    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;
    private final StaffRepository staffRepository;

    public GymController(MemberRepository memberRepository, PlanRepository planRepository,
                         StaffRepository staffRepository) {
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping("/members")
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @PostMapping("/members")
    public Member addMember(@RequestBody Member newMember) {
        requireText(newMember.getMemberId(), "Member ID");
        requireText(newMember.getFullName(), "Member name");
        validateMemberStatus(newMember.getStatus());
        if (memberRepository.existsById(newMember.getMemberId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member ID already exists");
        }
        newMember.setPlan(resolvePlan(newMember));
        if (newMember.getJoinDate() == null) {
            newMember.setJoinDate(LocalDate.now());
        }
        return memberRepository.save(newMember);
    }

    @PutMapping("/members/{id}")
    public Member updateMember(@PathVariable String id, @RequestBody Member updatedDetails) {
        Member existing = memberRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        requireText(updatedDetails.getFullName(), "Member name");
        validateMemberStatus(updatedDetails.getStatus());
        existing.setFullName(updatedDetails.getFullName());
        existing.setStatus(updatedDetails.getStatus());
        existing.setPlan(resolvePlan(updatedDetails));
        return memberRepository.save(existing);
    }

    @DeleteMapping("/members/{id}")
    public void deleteMember(@PathVariable String id) {
        if (!memberRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
        memberRepository.deleteById(id);
    }

    @GetMapping("/plans")
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @PostMapping("/plans")
    public Plan addPlan(@RequestBody Plan newPlan) {
        requireText(newPlan.getPlanId(), "Plan ID");
        validatePlanDetails(newPlan);
        if (planRepository.existsById(newPlan.getPlanId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plan ID already exists");
        }
        return planRepository.save(newPlan);
    }

    @PutMapping("/plans/{id}")
    public Plan updatePlan(@PathVariable String id, @RequestBody Plan updatedPlan) {
        Plan existing = planRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        validatePlanDetails(updatedPlan);
        existing.setPlanName(updatedPlan.getPlanName());
        existing.setDurationMonths(updatedPlan.getDurationMonths());
        existing.setPrice(updatedPlan.getPrice());
        return planRepository.save(existing);
    }

    @DeleteMapping("/plans/{id}")
    public void deletePlan(@PathVariable String id) {
        if (!planRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found");
        }
        planRepository.deleteById(id);
    }

    @GetMapping("/staff")
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    @PostMapping("/staff")
    public Staff addStaff(@RequestBody Staff newStaff) {
        requireText(newStaff.getStaffId(), "Staff ID");
        validateStaff(newStaff);
        if (staffRepository.existsById(newStaff.getStaffId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Staff ID already exists");
        }
        return staffRepository.save(newStaff);
    }

    @PutMapping("/staff/{id}")
    public Staff updateStaff(@PathVariable String id, @RequestBody Staff updatedStaff) {
        Staff existing = staffRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));
        validateStaff(updatedStaff);
        existing.setStaffName(updatedStaff.getStaffName());
        existing.setSpecialization(updatedStaff.getSpecialization());
        existing.setShiftTiming(updatedStaff.getShiftTiming());
        return staffRepository.save(existing);
    }

    @DeleteMapping("/staff/{id}")
    public void deleteStaff(@PathVariable String id) {
        if (!staffRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found");
        }
        staffRepository.deleteById(id);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> conflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "message", "This record already exists or is referenced by another record."));
    }

    private Plan resolvePlan(Member member) {
        if (member.getPlan() == null || member.getPlan().getPlanId() == null
                || member.getPlan().getPlanId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select an existing subscription plan");
        }
        return planRepository.findById(member.getPlan().getPlanId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription plan does not exist"));
    }

    private void validateMemberStatus(String status) {
        if (!"Active".equals(status) && !"Expired".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be Active or Expired");
        }
    }

    private void validatePlanDetails(Plan plan) {
        requireText(plan.getPlanName(), "Plan name");
        if (plan.getDurationMonths() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan duration must be greater than zero");
        }
        if (!Double.isFinite(plan.getPrice()) || plan.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan price cannot be negative");
        }
    }

    private void validateStaff(Staff staff) {
        requireText(staff.getStaffName(), "Staff name");
        requireText(staff.getSpecialization(), "Staff specialization");
        requireText(staff.getShiftTiming(), "Staff shift");
    }

    private void requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is required");
        }
    }
}
