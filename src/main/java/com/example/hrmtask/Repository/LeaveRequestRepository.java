package com.example.hrmtask.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrmtask.Model.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long>{
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByStatus(String status);
}
