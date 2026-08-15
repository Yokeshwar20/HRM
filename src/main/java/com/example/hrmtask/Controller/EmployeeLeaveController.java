package com.example.hrmtask.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<LeaveHistory>> getEmployeeLeaveHistory(@PathVariable Long employeeId) {
        List<LeaveHistory> history = leaveService.getEmployeeLeaveHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/requests/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getEmployeeLeaveRequests(@PathVariable Long employeeId) {
        List<LeaveRequest> requests = leaveService.getEmployeeLeaveRequests(employeeId);
        return ResponseEntity.ok(requests);
    }
}
