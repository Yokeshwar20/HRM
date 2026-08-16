package com.example.hrmtask.DTO;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollExportRequest {
    private Integer startMonth;
    private Integer startYear;
    private Integer endMonth;
    private Integer endYear;
    private List<String> employeeCodes;
}
