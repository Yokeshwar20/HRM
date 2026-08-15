package com.example.hrmtask.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrmtask.DTO.EmployeeCreationDTO;
import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Service.EmployeeService;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/hr")
public class HRController {
    private final EmployeeService employeeService;
    public HRController(EmployeeService employeeService){
        this.employeeService=employeeService;
    }

    @PostMapping("/create/employee")
    public ResponseEntity<Employees> createEmployee(@RequestBody EmployeeCreationDTO data) {
        Employees employee=employeeService.createEmployee(data);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/edit/employee/{id}")
    public ResponseEntity<Employees> editEmployee(@PathVariable Long id,@RequestBody Employees data) {
        Employees employee = employeeService.editEmployee(id, data);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Employees> updateEmployeeStatus(@PathVariable Long id,@RequestParam String status,@RequestParam(required = false) LocalDate endingDate) {
        Employees employee=employeeService.updateEmployeeStatus(id, status, endingDate);
        return ResponseEntity.ok(employee);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }
}
