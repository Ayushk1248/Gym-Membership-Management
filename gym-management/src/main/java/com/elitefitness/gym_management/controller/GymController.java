package com.elitefitness.gym_management.controller;

import com.elitefitness.gym_management.model.Member;
import com.elitefitness.gym_management.model.Plan;
import com.elitefitness.gym_management.model.Staff;
import com.elitefitness.gym_management.repository.MemberRepository;
import com.elitefitness.gym_management.repository.PlanRepository;
import com.elitefitness.gym_management.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // IMPORTANT: This allows your HTML frontend to talk to this backend
public class GymController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private StaffRepository staffRepository;

    // Endpoint to get all members
    @GetMapping("/members")
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

 // Endpoint to ADD a new member
    @org.springframework.web.bind.annotation.PostMapping("/members")
    public Member addMember(@org.springframework.web.bind.annotation.RequestBody Member newMember) {
        return memberRepository.save(newMember);
    }
    
 // Endpoint to UPDATE an existing member (HTTP PUT)
    @org.springframework.web.bind.annotation.PutMapping("/members/{id}")
    public Member updateMember(@org.springframework.web.bind.annotation.PathVariable String id, @org.springframework.web.bind.annotation.RequestBody Member updatedDetails) {
        // In Spring Data JPA, the save() method acts as an UPDATE if the ID already exists in the database!
        updatedDetails.setMemberId(id); 
        return memberRepository.save(updatedDetails);
    }
    
    //          PLANS (SUBSCRIPTIONS) ENDPOINTS
    @org.springframework.web.bind.annotation.PostMapping("/plans")
    public Plan addPlan(@org.springframework.web.bind.annotation.RequestBody Plan newPlan) {
        return planRepository.save(newPlan);
    }

    @org.springframework.web.bind.annotation.PutMapping("/plans/{id}")
    public Plan updatePlan(@org.springframework.web.bind.annotation.PathVariable String id, @org.springframework.web.bind.annotation.RequestBody Plan plan) {
        plan.setPlanId(id);
        return planRepository.save(plan);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/plans/{id}")
    public void deletePlan(@org.springframework.web.bind.annotation.PathVariable String id) {
        planRepository.deleteById(id);
    }

    //          STAFF (TRAINERS) ENDPOINTS
    @org.springframework.web.bind.annotation.PostMapping("/staff")
    public Staff addStaff(@org.springframework.web.bind.annotation.RequestBody Staff newStaff) {
        return staffRepository.save(newStaff);
    }

    @org.springframework.web.bind.annotation.PutMapping("/staff/{id}")
    public Staff updateStaff(@org.springframework.web.bind.annotation.PathVariable String id, @org.springframework.web.bind.annotation.RequestBody Staff staff) {
        staff.setStaffId(id);
        return staffRepository.save(staff);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/staff/{id}")
    public void deleteStaff(@org.springframework.web.bind.annotation.PathVariable String id) {
        staffRepository.deleteById(id);
    }
    
    // Endpoint to DELETE a member (HTTP DELETE)
    @org.springframework.web.bind.annotation.DeleteMapping("/members/{id}")
    public void deleteMember(@org.springframework.web.bind.annotation.PathVariable String id) {
        memberRepository.deleteById(id);
    }
    
    // Endpoint to get all plans
    @GetMapping("/plans")
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    // Endpoint to get all staff
    @GetMapping("/staff")
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
}