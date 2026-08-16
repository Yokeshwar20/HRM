package com.example.hrmtask.Controller;

import java.io.File;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Model.Payroll;
import com.example.hrmtask.Service.AuthenticatedUserService;
import com.example.hrmtask.Service.PayrollService;

@RestController
@RequestMapping("/api/employee/payroll")
public class EmployeePayrollController {

    private final PayrollService payrollService;
    private final AuthenticatedUserService authenticatedUserService;

    public EmployeePayrollController(PayrollService payrollService, AuthenticatedUserService authenticatedUserService) {
        this.payrollService = payrollService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<Payroll>> getEmployeePayrollHistory() {
        List<Payroll> history = payrollService.getMyPayrollHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/payslip")
    public ResponseEntity<Resource> getPayslip(@PathVariable Long id) {
        Employees authenticatedEmployee = authenticatedUserService.getAuthenticatedEmployee();
        Payroll payroll = payrollService.getPayrollById(id);

        if (!payroll.getEmployeeId().equals(authenticatedEmployee.getId())) {
            throw new RuntimeException("Access denied: You are only authorized to view your own payslip");
        }

        if (payroll.getPayslipPath() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(payroll.getPayslipPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
