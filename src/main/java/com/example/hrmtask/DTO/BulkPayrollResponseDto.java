package com.example.hrmtask.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkPayrollResponseDto {
    private int totalEmployees;
    private int successfullyProcessed;
    private int failed;
    private int alreadyProcessed;
}
