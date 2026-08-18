package com.example.hrmtask.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Integer payMonth;
    private Integer payYear;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal allowance;
    private BigDecimal grossSalary;
    private BigDecimal pf;
    private BigDecimal otherDeduction;
    private BigDecimal totalDeduction;
    private BigDecimal netSalary;
    private String payslipPath;
    private String emailStatus;
    private LocalDateTime processedAt;
}
