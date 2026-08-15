package com.example.hrmtask.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payroll{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
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
