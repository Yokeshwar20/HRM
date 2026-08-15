package com.example.hrmtask.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hrmtask.DTO.BulkPayrollRequestDto;
import com.example.hrmtask.DTO.BulkPayrollResponseDto;
import com.example.hrmtask.DTO.PayrollRequestDto;
import com.example.hrmtask.DTO.PayrollScheduleDto;
import com.example.hrmtask.Model.Payroll;
import com.example.hrmtask.Model.PayrollSchedule;
import com.example.hrmtask.Service.PayrollService;

@RestController
@RequestMapping("/api/hr/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/process")
    public ResponseEntity<BulkPayrollResponseDto> processBulkPayroll(@RequestBody BulkPayrollRequestDto dto) {
        BulkPayrollResponseDto response = payrollService.processAllEmployeesPayroll(dto.getPayMonth(), dto.getPayYear());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process/employee")
    public ResponseEntity<Payroll> processEmployeePayroll(@RequestBody PayrollRequestDto dto) {
        Payroll payroll = payrollService.processEmployeePayroll(dto.getEmployeeId(), dto.getPayMonth(), dto.getPayYear());
        return ResponseEntity.ok(payroll);
    }

    @GetMapping
    public ResponseEntity<List<Payroll>> getAllPayrolls() {
        List<Payroll> payrolls = payrollService.getAllPayrolls();
        return ResponseEntity.ok(payrolls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payroll> getPayrollById(@PathVariable Long id) {
        Payroll payroll = payrollService.getPayrollById(id);
        return ResponseEntity.ok(payroll);
    }

    @PostMapping("/{id}/retry-email")
    public ResponseEntity<Payroll> retryEmailForPayroll(@PathVariable Long id) {
        Payroll payroll = payrollService.retryEmailForPayroll(id);
        return ResponseEntity.ok(payroll);
    }

    @PostMapping("/retry-failed-emails")
    public ResponseEntity<List<Payroll>> retryAllFailedEmails() {
        List<Payroll> payrolls = payrollService.retryAllFailedEmails();
        return ResponseEntity.ok(payrolls);
    }

    @PostMapping("/schedule")
    public ResponseEntity<PayrollSchedule> createPayrollSchedule(@RequestBody PayrollScheduleDto dto) {
        PayrollSchedule schedule = payrollService.createPayrollSchedule(dto);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<PayrollSchedule>> getPayrollSchedules() {
        List<PayrollSchedule> schedules = payrollService.getPayrollSchedules();
        return ResponseEntity.ok(schedules);
    }

    @PutMapping("/schedule/{id}")
    public ResponseEntity<PayrollSchedule> updatePayrollSchedule(@PathVariable Long id, @RequestBody PayrollScheduleDto dto) {
        PayrollSchedule updated = payrollService.updatePayrollSchedule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<PayrollSchedule> cancelPayrollSchedule(@PathVariable Long id) {
        PayrollSchedule cancelled = payrollService.cancelPayrollSchedule(id);
        return ResponseEntity.ok(cancelled);
    }
}
