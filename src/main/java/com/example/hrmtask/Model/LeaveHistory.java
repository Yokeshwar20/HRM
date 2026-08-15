package com.example.hrmtask.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "leave_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveHistory{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private String leaveType;
    private BigDecimal totalDays;
    private BigDecimal usedDays;
    private BigDecimal remainingDays;
    private LocalDate startDate;
    private LocalDate endDate;
}
