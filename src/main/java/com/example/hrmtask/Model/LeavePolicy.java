package com.example.hrmtask.Model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicy{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String leaveType;
    private BigDecimal totalDays;
}
