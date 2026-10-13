package com.elitefitness.gym_management.repository;

import com.elitefitness.gym_management.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, String> {
    // Just by extending JpaRepository, you automatically get access to 
    // built-in methods like save(), findAll(), findById(), and delete()!
}