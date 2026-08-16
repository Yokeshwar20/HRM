package com.example.hrmtask.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long leaveHistoryId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String hrComment;
    private LocalDateTime appliedAt;
    private LocalDateTime decidedAt;
}
