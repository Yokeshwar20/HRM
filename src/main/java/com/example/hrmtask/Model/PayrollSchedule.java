package com.example.hrmtask.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer payMonth;
    private Integer payYear;
    private LocalDateTime scheduledAt;
    private Boolean enabled;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;
}
