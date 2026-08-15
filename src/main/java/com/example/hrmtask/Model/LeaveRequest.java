package com.example.hrmtask.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private Long leaveHistoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String hrComment;
    private LocalDateTime appliedAt;
    private LocalDateTime decidedAt;
}
