package com.example.hrmtask.DTO;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollScheduleDto {
    private Integer payMonth;
    private Integer payYear;
    private LocalDateTime scheduledAt;
}
