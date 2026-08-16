package com.example.hrmtask.Controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrmtask.DTO.EmployeeCreationDTO;
import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Service.EmployeeService;

@RestController
@RequestMapping("/api/hr")
public class HRController {
    private final EmployeeService employeeService;

    public HRController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employees>> getAllEmployees() {
        List<Employees> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/employee/latest")
    public ResponseEntity<Employees> getLastCreatedEmployee() {
        Employees employee = employeeService.getLastCreatedEmployee();
        if (employee == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/employee/{employeeCode}")
    public ResponseEntity<Employees> getEmployeeByCode(@PathVariable String employeeCode) {
        Employees employee = employeeService.getEmployeeByCode(employeeCode);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/create/employee")
    public ResponseEntity<Employees> createEmployee(@RequestBody EmployeeCreationDTO data) {
        Employees employee = employeeService.createEmployee(data);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/edit/employee/{employeeCode}")
    public ResponseEntity<Employees> editEmployeeByCode(@PathVariable String employeeCode, @RequestBody Employees data) {
        Employees employee = employeeService.editEmployeeByCode(employeeCode, data);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{employeeCode}/status")
    public ResponseEntity<Employees> updateEmployeeStatusByCode(
            @PathVariable String employeeCode,
            @RequestParam String status,
            @RequestParam(required = false) LocalDate endingDate) {
        Employees employee = employeeService.updateEmployeeStatusByCode(employeeCode, status, endingDate);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{employeeCode}")
    public ResponseEntity<String> deleteEmployeeByCode(@PathVariable String employeeCode) {
        employeeService.deleteEmployeeByCode(employeeCode);
        return ResponseEntity.ok("Employee deleted successfully");
    }
}
