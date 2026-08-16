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

    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllLeaveTypes() {
        List<String> types = leaveService.getAllLeaveTypes();
        return ResponseEntity.ok(types);
    }

    @DeleteMapping("/policy/{id}")
    public ResponseEntity<String> deletePolicy(@PathVariable Long id) {
        leaveService.deletePolicy(id);
        return ResponseEntity.ok("Leave policy deleted successfully");
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<com.example.hrmtask.DTO.LeaveRequestResponseDto>> getPendingLeaveRequests() {
        List<com.example.hrmtask.DTO.LeaveRequestResponseDto> pendingRequests = leaveService.getPendingLeaveRequestsForHR();
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<com.example.hrmtask.DTO.LeaveRequestResponseDto>> getAllLeaveRequests() {
        List<com.example.hrmtask.DTO.LeaveRequestResponseDto> requests = leaveService.getAllLeaveRequestsForHR();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/employee/{employeeCode}")
    public ResponseEntity<List<com.example.hrmtask.DTO.LeaveRequestResponseDto>> getEmployeeLeaveRequestsForHR(@PathVariable String employeeCode) {
        List<com.example.hrmtask.DTO.LeaveRequestResponseDto> requests = leaveService.getEmployeeLeaveRequestsForHRByCode(employeeCode);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<com.example.hrmtask.DTO.LeaveRequestResponseDto> approveLeaveRequest(@PathVariable Long id, @RequestBody(required = false) LeaveDecisionDto dto) {
        LeaveRequest approved = leaveService.approveLeaveRequest(id, dto);
        return ResponseEntity.ok(leaveService.mapToLeaveRequestResponseDto(approved));
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<com.example.hrmtask.DTO.LeaveRequestResponseDto> rejectLeaveRequest(@PathVariable Long id, @RequestBody(required = false) LeaveDecisionDto dto) {
        LeaveRequest rejected = leaveService.rejectLeaveRequest(id, dto);
        return ResponseEntity.ok(leaveService.mapToLeaveRequestResponseDto(rejected));
    }

    @GetMapping("/history/{employeeCode}")
    public ResponseEntity<List<LeaveHistory>> getEmployeeLeaveHistoryForHR(@PathVariable String employeeCode) {
        List<LeaveHistory> history = leaveService.getEmployeeLeaveHistoryForHRByCode(employeeCode);
        return ResponseEntity.ok(history);
    }
}
