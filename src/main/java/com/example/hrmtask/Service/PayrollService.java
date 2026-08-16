package com.example.hrmtask.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hrmtask.DTO.BulkPayrollResponseDto;
import com.example.hrmtask.DTO.PayrollScheduleDto;
import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Model.Payroll;
import com.example.hrmtask.Model.PayrollSchedule;
import com.example.hrmtask.Model.SalaryStructure;
import com.example.hrmtask.Repository.EmployeesRepository;
import com.example.hrmtask.Repository.PayrollRepository;
import com.example.hrmtask.Repository.PayrollScheduleRepository;
import com.example.hrmtask.Repository.SalaryStructureRepository;
import com.example.hrmtask.Repository.UsersRepository;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeesRepository employeesRepository;
    private final PayrollScheduleRepository payrollScheduleRepository;
    private final UsersRepository usersRepository;
    private final PdfService pdfService;
    private final EmailService emailService;
    private final AuthenticatedUserService authenticatedUserService;

    public PayrollService(PayrollRepository payrollRepository,
                          SalaryStructureRepository salaryStructureRepository,
                          EmployeesRepository employeesRepository,
                          PayrollScheduleRepository payrollScheduleRepository,
                          UsersRepository usersRepository,
                          PdfService pdfService,
                          EmailService emailService,
                          AuthenticatedUserService authenticatedUserService) {
        this.payrollRepository = payrollRepository;
        this.salaryStructureRepository = salaryStructureRepository;
        this.employeesRepository = employeesRepository;
        this.payrollScheduleRepository = payrollScheduleRepository;
        this.usersRepository = usersRepository;
        this.pdfService = pdfService;
        this.emailService = emailService;
        this.authenticatedUserService = authenticatedUserService;
    }

    private String resolveEmployeeEmail(Employees employee) {
        if (employee.getEmail() != null && !employee.getEmail().isBlank()) {
            return employee.getEmail().trim();
        }
        if (employee.getUserId() != null) {
            return usersRepository.findById(employee.getUserId())
                    .map(u -> u.getEmail() != null ? u.getEmail().trim() : null)
                    .orElse(null);
        }
        return null;
    }

    public Payroll processEmployeePayroll(Long employeeId, Integer payMonth, Integer payYear) {
        if (payrollRepository.existsByEmployeeIdAndPayMonthAndPayYear(employeeId, payMonth, payYear)) {
            throw new RuntimeException("Payroll already processed for employee " + employeeId + " for " + payMonth + "/" + payYear);
        }

        Employees employee = employeesRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        SalaryStructure structure = salaryStructureRepository.findTopByEmployeeIdOrderByEffectiveFromDesc(employeeId)
                .orElseThrow(() -> new RuntimeException("Salary structure not found for employee id: " + employeeId));

        BigDecimal basicSalary = structure.getBasicSalary() != null ? structure.getBasicSalary() : BigDecimal.ZERO;
        BigDecimal hra = structure.getHra() != null ? structure.getHra() : BigDecimal.ZERO;
        BigDecimal allowance = structure.getAllowance() != null ? structure.getAllowance() : BigDecimal.ZERO;
        BigDecimal grossSalary = structure.getGrossSalary() != null ? structure.getGrossSalary() : basicSalary.add(hra).add(allowance);

        BigDecimal pf = structure.getPf() != null ? structure.getPf() : BigDecimal.ZERO;
        BigDecimal otherDeduction = structure.getOtherDeduction() != null ? structure.getOtherDeduction() : BigDecimal.ZERO;
        BigDecimal totalDeduction = pf.add(otherDeduction);

        BigDecimal netSalary = grossSalary.subtract(totalDeduction);

        Payroll payroll = new Payroll();
        payroll.setEmployeeId(employeeId);
        payroll.setPayMonth(payMonth);
        payroll.setPayYear(payYear);
        payroll.setBasicSalary(basicSalary);
        payroll.setHra(hra);
        payroll.setAllowance(allowance);
        payroll.setGrossSalary(grossSalary);
        payroll.setPf(pf);
        payroll.setOtherDeduction(otherDeduction);
        payroll.setTotalDeduction(totalDeduction);
        payroll.setNetSalary(netSalary);
        payroll.setProcessedAt(LocalDateTime.now());
        payroll.setEmailStatus("PENDING");

        Payroll savedPayroll = payrollRepository.save(payroll);

        String filePath = null;
        try {
            filePath = pdfService.generatePayslip(savedPayroll, employee);
            savedPayroll.setPayslipPath(filePath);
            payrollRepository.save(savedPayroll);
        } catch (Exception e) {
            System.err.println("Failed to generate PDF payslip for employee " + employeeId + ": " + e.getMessage());
        }

        String recipientEmail = resolveEmployeeEmail(employee);

        if (filePath != null && recipientEmail != null && !recipientEmail.isBlank()) {
            String empName = ((employee.getFirstName() != null ? employee.getFirstName() : "") + " " + (employee.getLastName() != null ? employee.getLastName() : "")).trim();
            boolean success = emailService.sendPayslipWithRetry(recipientEmail, filePath, empName, payMonth, payYear, netSalary, 3, 1000);
            if (success) {
                savedPayroll.setEmailStatus("SENT");
            } else {
                System.err.println("Email delivery failed after retries for employee id " + employeeId + " (" + recipientEmail + ")");
                savedPayroll.setEmailStatus("FAILED");
            }
            savedPayroll = payrollRepository.save(savedPayroll);
        } else {
            if (filePath == null) {
                System.err.println("Payslip email not sent for employee id " + employeeId + ": PDF payslip generation failed.");
            } else {
                System.err.println("Payslip email not sent for employee id " + employeeId + ": No valid email address found.");
            }
            savedPayroll.setEmailStatus("FAILED");
            savedPayroll = payrollRepository.save(savedPayroll);
        }

        return savedPayroll;
    }

    public BulkPayrollResponseDto processAllEmployeesPayroll(Integer payMonth, Integer payYear) {
        List<Employees> activeEmployees = employeesRepository.findByStatusIgnoreCase("ACTIVE");

        int total = activeEmployees.size();
        int successful = 0;
        int failed = 0;
        int alreadyProcessed = 0;

        for (Employees emp : activeEmployees) {
            if (payrollRepository.existsByEmployeeIdAndPayMonthAndPayYear(emp.getId(), payMonth, payYear)) {
                alreadyProcessed++;
                continue;
            }

            try {
                processEmployeePayroll(emp.getId(), payMonth, payYear);
                successful++;
            } catch (Exception e) {
                System.err.println("Failed to process payroll for employee id " + emp.getId() + ": " + e.getMessage());
                failed++;
            }
        }

        return new BulkPayrollResponseDto(total, successful, failed, alreadyProcessed);
    }

    public Payroll processEmployeePayrollByCode(String employeeCode, Integer payMonth, Integer payYear) {
        Employees employee = employeesRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return processEmployeePayroll(employee.getId(), payMonth, payYear);
    }

    public List<Payroll> getMyPayrollHistory() {
        Employees employee = authenticatedUserService.getAuthenticatedEmployee();
        return payrollRepository.findByEmployeeIdOrderByPayYearDescPayMonthDesc(employee.getId());
    }

    public List<Payroll> getEmployeePayrollHistory(Long employeeId) {
        return payrollRepository.findByEmployeeIdOrderByPayYearDescPayMonthDesc(employeeId);
    }

    public List<Payroll> getEmployeePayrollHistoryByCode(String employeeCode) {
        Employees employee = employeesRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return payrollRepository.findByEmployeeIdOrderByPayYearDescPayMonthDesc(employee.getId());
    }

    public Payroll getPayrollById(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll record not found with id: " + id));
    }

    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findAll();
    }

    public Payroll retryEmailForPayroll(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll record not found with id: " + payrollId));

        Employees employee = employeesRepository.findById(payroll.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + payroll.getEmployeeId()));

        String recipientEmail = resolveEmployeeEmail(employee);
        if (recipientEmail == null || recipientEmail.isBlank()) {
            System.err.println("Retry email failed for payroll id " + payrollId + ": No valid email address found for employee id " + payroll.getEmployeeId());
            payroll.setEmailStatus("FAILED");
            return payrollRepository.save(payroll);
        }

        String filePath = payroll.getPayslipPath();
        if (filePath == null || !(new java.io.File(filePath).exists())) {
            try {
                filePath = pdfService.generatePayslip(payroll, employee);
                payroll.setPayslipPath(filePath);
            } catch (Exception e) {
                System.err.println("Failed to generate PDF payslip during email retry for payroll " + payrollId + ": " + e.getMessage());
                payroll.setEmailStatus("FAILED");
                return payrollRepository.save(payroll);
            }
        }

        String empName = ((employee.getFirstName() != null ? employee.getFirstName() : "") + " " + (employee.getLastName() != null ? employee.getLastName() : "")).trim();
        boolean success = emailService.sendPayslipWithRetry(recipientEmail, filePath, empName, payroll.getPayMonth(), payroll.getPayYear(), payroll.getNetSalary(), 3, 1000);

        if (success) {
            payroll.setEmailStatus("SENT");
        } else {
            System.err.println("Retry email delivery failed for payroll id " + payrollId + " (" + recipientEmail + ")");
            payroll.setEmailStatus("FAILED");
        }

        return payrollRepository.save(payroll);
    }

    public List<Payroll> retryAllFailedEmails() {
        List<Payroll> failedPayrolls = payrollRepository.findByEmailStatusIgnoreCase("FAILED");
        List<Payroll> pendingPayrolls = payrollRepository.findByEmailStatusIgnoreCase("PENDING");
        failedPayrolls.addAll(pendingPayrolls);

        for (Payroll p : failedPayrolls) {
            try {
                retryEmailForPayroll(p.getId());
            } catch (Exception e) {
                System.err.println("Error retrying email for payroll id " + p.getId() + ": " + e.getMessage());
            }
        }

        return payrollRepository.findAll();
    }
    public PayrollSchedule createPayrollSchedule(PayrollScheduleDto dto) {
        List<Employees> activeEmployees = employeesRepository.findByStatusIgnoreCase("ACTIVE");
        if (!activeEmployees.isEmpty()) {
            boolean allProcessed = activeEmployees.stream().allMatch(emp -> 
                payrollRepository.existsByEmployeeIdAndPayMonthAndPayYear(emp.getId(), dto.getPayMonth(), dto.getPayYear())
            );
            if (allProcessed) {
                throw new RuntimeException("Payroll for " + dto.getPayMonth() + "/" + dto.getPayYear() + " has already been completely processed");
            }
        }

        PayrollSchedule schedule = new PayrollSchedule();
        schedule.setPayMonth(dto.getPayMonth());
        schedule.setPayYear(dto.getPayYear());
        schedule.setScheduledAt(dto.getScheduledAt());
        schedule.setEnabled(true);
        schedule.setStatus("PENDING");
        schedule.setCreatedAt(LocalDateTime.now());

        return payrollScheduleRepository.save(schedule);
    }

    public List<PayrollSchedule> getPayrollSchedules() {
        return payrollScheduleRepository.findAll();
    }

    public PayrollSchedule updatePayrollSchedule(Long id, PayrollScheduleDto dto) {
        PayrollSchedule schedule = payrollScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll schedule not found with id: " + id));

        if ("COMPLETED".equalsIgnoreCase(schedule.getStatus())) {
            throw new RuntimeException("Cannot update a completed payroll schedule");
        }

        schedule.setPayMonth(dto.getPayMonth());
        schedule.setPayYear(dto.getPayYear());
        schedule.setScheduledAt(dto.getScheduledAt());

        return payrollScheduleRepository.save(schedule);
    }

    public PayrollSchedule cancelPayrollSchedule(Long id) {
        PayrollSchedule schedule = payrollScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll schedule not found with id: " + id));

        schedule.setEnabled(false);
        schedule.setStatus("CANCELLED");

        return payrollScheduleRepository.save(schedule);
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void executeScheduledPayrolls() {
        List<PayrollSchedule> pendingSchedules = payrollScheduleRepository
                .findByEnabledTrueAndScheduledAtLessThanEqualAndStatus(LocalDateTime.now(), "PENDING");

        for (PayrollSchedule schedule : pendingSchedules) {
            schedule.setStatus("IN_PROGRESS");
            payrollScheduleRepository.save(schedule);

            try {
                processAllEmployeesPayroll(schedule.getPayMonth(), schedule.getPayYear());
                schedule.setStatus("COMPLETED");
                schedule.setEnabled(false);
                schedule.setExecutedAt(LocalDateTime.now());
            } catch (Exception e) {
                System.err.println("Error executing scheduled payroll id " + schedule.getId() + ": " + e.getMessage());
                schedule.setStatus("FAILED");
            }
            payrollScheduleRepository.save(schedule);
        }
    }
}
