package com.example.hrmtask.Controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrmtask.DTO.BulkPayrollRequestDto;
import com.example.hrmtask.DTO.BulkPayrollResponseDto;
import com.example.hrmtask.DTO.PayrollExportRequest;
import com.example.hrmtask.DTO.PayrollRequestDto;
import com.example.hrmtask.DTO.PayrollResponseDto;
import com.example.hrmtask.DTO.PayrollScheduleDto;
import com.example.hrmtask.Model.Payroll;
import com.example.hrmtask.Model.PayrollSchedule;
import com.example.hrmtask.Service.PayrollExportService;
import com.example.hrmtask.Service.PayrollService;

@RestController
@RequestMapping("/api/hr/payroll")
public class PayrollController {

    private final PayrollService payrollService;
    private final PayrollExportService payrollExportService;

    public PayrollController(PayrollService payrollService, PayrollExportService payrollExportService) {
        this.payrollService = payrollService;
        this.payrollExportService = payrollExportService;
    }

    @PostMapping("/process")
    public ResponseEntity<BulkPayrollResponseDto> processBulkPayroll(@RequestBody BulkPayrollRequestDto dto) {
        BulkPayrollResponseDto response = payrollService.processAllEmployeesPayroll(dto.getPayMonth(), dto.getPayYear());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process/employee")
    public ResponseEntity<PayrollResponseDto> processEmployeePayroll(@RequestBody PayrollRequestDto dto) {
        PayrollResponseDto payroll = payrollService.processEmployeePayrollByCodeDto(dto.getEmployeeCode(), dto.getPayMonth(), dto.getPayYear());
        return ResponseEntity.ok(payroll);
    }

    @GetMapping
    public ResponseEntity<List<PayrollResponseDto>> getAllPayrolls() {
        List<PayrollResponseDto> payrolls = payrollService.getAllPayrollsDto();
        return ResponseEntity.ok(payrolls);
    }

    @GetMapping("/employee/{employeeCode}/history")
    public ResponseEntity<List<PayrollResponseDto>> getEmployeePayrollHistory(@PathVariable String employeeCode) {
        List<PayrollResponseDto> payrolls = payrollService.getEmployeePayrollHistoryByCode(employeeCode);
        return ResponseEntity.ok(payrolls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollResponseDto> getPayrollById(@PathVariable Long id) {
        PayrollResponseDto payroll = payrollService.getPayrollByIdDto(id);
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

    @PostMapping(value = "/payslips/download", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    public ResponseEntity<byte[]> downloadPayslipsAsZip(@RequestBody PayrollExportRequest request) {
        byte[] zipBytes = payrollExportService.downloadPayslipsAsZip(request);
        String filename = payrollExportService.getZipFilename(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipBytes);
    }

    @PostMapping(value = "/report", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    public ResponseEntity<byte[]> generatePayrollExcel(@RequestBody PayrollExportRequest request) {
        byte[] excelBytes = payrollExportService.generatePayrollExcel(request);
        String filename = payrollExportService.getExcelFilename(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
