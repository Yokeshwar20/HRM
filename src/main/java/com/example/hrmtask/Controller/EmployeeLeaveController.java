package com.example.hrmtask.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrmtask.DTO.LeaveRequestDto;
import com.example.hrmtask.Model.LeaveHistory;
import com.example.hrmtask.Model.LeaveRequest;
import com.example.hrmtask.Service.LeaveService;

@RestController
@RequestMapping("/api/employee/leave")
public class EmployeeLeaveController {

    private final LeaveService leaveService;

    public EmployeeLeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/apply")
    public ResponseEntity<LeaveRequest> applyForLeave(@RequestBody LeaveRequestDto dto) {
        LeaveRequest leaveRequest = leaveService.applyForLeave(dto);
        return ResponseEntity.ok(leaveRequest);
    }

    @GetMapping("/history")
    public ResponseEntity<List<LeaveHistory>> getEmployeeLeaveHistory() {
        List<LeaveHistory> history = leaveService.getEmployeeLeaveHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<LeaveRequest>> getEmployeeLeaveRequests() {
        List<LeaveRequest> requests = leaveService.getEmployeeLeaveRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllLeaveTypes() {
        List<String> types = leaveService.getAllLeaveTypes();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/policies")
    public ResponseEntity<List<com.example.hrmtask.Model.LeavePolicy>> getAllLeavePolicies() {
        List<com.example.hrmtask.Model.LeavePolicy> policies = leaveService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }
}
