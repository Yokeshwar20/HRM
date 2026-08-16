package com.example.hrmtask.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRequestDto {
    private String employeeCode;
    private Integer payMonth;
    private Integer payYear;
}
