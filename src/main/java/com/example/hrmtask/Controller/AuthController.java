package com.example.hrmtask.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrmtask.DTO.LoginRequestDto;
import com.example.hrmtask.DTO.LoginResponseDto;
import com.example.hrmtask.DTO.RefreshTokenRequestDto;
import com.example.hrmtask.DTO.RefreshTokenResponseDto;
import com.example.hrmtask.DTO.UsersDto;
import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Service.UsersService;

@RestController
@RequestMapping
public class AuthController {
    private final UsersService usersService;

    public AuthController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping({"/api/auth/register", "/register"})
    public ResponseEntity<Employees> register(@RequestBody UsersDto data) {
        Employees employee = usersService.register(data);
        return ResponseEntity.ok(employee);
    }

    @PostMapping({"/api/auth/login", "/login"})
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto data) {
        LoginResponseDto response = usersService.login(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/api/auth/refresh-token", "/refresh-token"})
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@RequestBody RefreshTokenRequestDto data) {
        RefreshTokenResponseDto response = usersService.refreshToken(data);
        return ResponseEntity.ok(response);
    }
}
