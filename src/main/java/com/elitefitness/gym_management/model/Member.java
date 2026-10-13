package com.elitefitness.gym_management.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;

@Entity
@Table(name = "Members")
public class Member {

    @Id
    @Column(name = "member_id")
    private String memberId;

    @Column(name = "full_name")
    private String fullName;

    // This is the Foreign Key connection to the Plans table!
    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private String status = "Active";

    @Column(name = "join_date")
    private LocalDate joinDate;

    // Default Constructor
    public Member() {
    }

    // Getters and Setters
    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }
}