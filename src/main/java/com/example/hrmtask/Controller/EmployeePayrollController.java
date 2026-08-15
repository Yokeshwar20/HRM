package com.example.hrmtask.Controller;

import java.io.File;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hrmtask.Model.Payroll;
import com.example.hrmtask.Service.PayrollService;

@RestController
@RequestMapping("/api/employee/payroll")
public class EmployeePayrollController {

    private final PayrollService payrollService;

    public EmployeePayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<Payroll>> getEmployeePayrollHistory(@PathVariable Long employeeId) {
        List<Payroll> history = payrollService.getEmployeePayrollHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/payslip")
    public ResponseEntity<Resource> getPayslip(@PathVariable Long id) {
        Payroll payroll = payrollService.getPayrollById(id);
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
