package com.example.hrmtask.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrmtask.Model.LeavePolicy;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy,Long>{
    Optional<LeavePolicy> findByLeaveType(String leaveType);
}
