package com.example.hrmtask.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkPayrollRequestDto {
    private Integer payMonth;
    private Integer payYear;
}
