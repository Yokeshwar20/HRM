package com.example.hrmtask.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "salary_structures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal allowance;
    private BigDecimal grossSalary;
    private BigDecimal pf;
    private BigDecimal otherDeduction;
    private LocalDate effectiveFrom;
}