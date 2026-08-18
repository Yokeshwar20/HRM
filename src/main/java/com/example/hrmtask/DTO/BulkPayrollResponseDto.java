package com.example.hrmtask.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
