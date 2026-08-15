package com.example.hrmtask.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrmtask.Model.LeaveHistory;

public interface LeaveHistoryRepository extends JpaRepository<LeaveHistory,Long>{
    Optional<LeaveHistory> findTopByEmployeeIdAndLeaveTypeOrderByEndDateDesc(Long employeeId, String leaveType);
    List<LeaveHistory> findByEmployeeId(Long employeeId);
}
