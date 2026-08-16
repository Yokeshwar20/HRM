package com.example.hrmtask.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Service.AuthenticatedUserService;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final AuthenticatedUserService authenticatedUserService;

    public EmployeeController(AuthenticatedUserService authenticatedUserService) {
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/profile")
    public ResponseEntity<Employees> getMyProfile() {
        Employees employee = authenticatedUserService.getAuthenticatedEmployee();
        return ResponseEntity.ok(employee);
    }
}
