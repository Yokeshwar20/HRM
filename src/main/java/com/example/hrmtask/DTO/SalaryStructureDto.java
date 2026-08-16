package com.example.hrmtask.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructureDto {
    private String employeeCode;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal allowance;
    private BigDecimal grossSalary;
    private BigDecimal pf;
    private BigDecimal otherDeduction;
    private LocalDate effectiveFrom;
}
