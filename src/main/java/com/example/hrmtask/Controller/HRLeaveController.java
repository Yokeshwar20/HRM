package com.example.hrmtask.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hrmtask.DTO.LeaveDecisionDto;
import com.example.hrmtask.Model.LeaveHistory;
import com.example.hrmtask.Model.LeavePolicy;
import com.example.hrmtask.Model.LeaveRequest;
import com.example.hrmtask.Service.LeaveService;

@RestController
@RequestMapping("/api/hr/leave")
public class HRLeaveController {

    private final LeaveService leaveService;

    public HRLeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/policy")
    public ResponseEntity<LeavePolicy> createPolicy(@RequestBody LeavePolicy policy) {
        LeavePolicy created = leaveService.createPolicy(policy);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/policy/{id}")
    public ResponseEntity<LeavePolicy> updatePolicy(@PathVariable Long id, @RequestBody LeavePolicy policy) {
        LeavePolicy updated = leaveService.updatePolicy(id, policy);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/policy")
    public ResponseEntity<List<LeavePolicy>> getAllPolicies() {
        List<LeavePolicy> policies = leaveService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }

    @DeleteMapping("/policy/{id}")
    public ResponseEntity<String> deletePolicy(@PathVariable Long id) {
        leaveService.deletePolicy(id);
        return ResponseEntity.ok("Leave policy deleted successfully");
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaveRequests() {
        List<LeaveRequest> pendingRequests = leaveService.getPendingLeaveRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeaveRequest(@PathVariable Long id, @RequestBody(required = false) LeaveDecisionDto dto) {
        LeaveRequest approved = leaveService.approveLeaveRequest(id, dto);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeaveRequest(@PathVariable Long id, @RequestBody(required = false) LeaveDecisionDto dto) {
        LeaveRequest rejected = leaveService.rejectLeaveRequest(id, dto);
        return ResponseEntity.ok(rejected);
    }

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<LeaveHistory>> getEmployeeLeaveHistoryForHR(@PathVariable Long employeeId) {
        List<LeaveHistory> history = leaveService.getEmployeeLeaveHistoryForHR(employeeId);
        return ResponseEntity.ok(history);
    }
}
