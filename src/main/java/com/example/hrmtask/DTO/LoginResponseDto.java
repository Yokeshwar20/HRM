package com.example.hrmtask.DTO;

import com.example.hrmtask.Model.Employees;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String role;
    private Employees employee;
}
