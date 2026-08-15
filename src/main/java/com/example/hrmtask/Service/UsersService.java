package com.example.hrmtask.Service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.hrmtask.DTO.LoginRequestDto;
import com.example.hrmtask.DTO.LoginResponseDto;
import com.example.hrmtask.DTO.RefreshTokenRequestDto;
import com.example.hrmtask.DTO.RefreshTokenResponseDto;
import com.example.hrmtask.DTO.UsersDto;
import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Model.Users;
import com.example.hrmtask.Repository.EmployeesRepository;
import com.example.hrmtask.Repository.UsersRepository;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final EmployeesRepository employeesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsersService(UsersRepository usersRepository,
                        EmployeesRepository employeesRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.usersRepository = usersRepository;
        this.employeesRepository = employeesRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Employees register(UsersDto data) {
        Employees employee = employeesRepository.findByEmail(data.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee Not found"));
        if (employee.getUserId() != null) {
            throw new RuntimeException("Employee already has an account");
        }

        Users user = new Users();
        user.setEmail(data.getEmail());
        user.setPassword(passwordEncoder.encode(data.getPassword()));

        if (employee.getDesignation() != null && employee.getDesignation().equalsIgnoreCase("HR")) {
            user.setRole("HR");
        } else {
            user.setRole("EMPLOYEE");
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);
        Users savedUser = usersRepository.save(user);

        employee.setUserId(savedUser.getId());
        return employeesRepository.save(employee);
    }

    public LoginResponseDto login(LoginRequestDto data) {
        Users user = usersRepository.findByEmail(data.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account Locked, Contact HR");
        }

        if (!passwordEncoder.matches(data.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect Password");
        }

        Employees employee = employeesRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee Not found"));

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new LoginResponseDto(accessToken, refreshToken, "Bearer", user.getRole(), employee);
    }

    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto data) {
        if (data.getRefreshToken() == null || data.getRefreshToken().isBlank()) {
            throw new RuntimeException("Refresh token is required");
        }

        String email = jwtService.extractEmail(data.getRefreshToken());
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account Locked, Contact HR");
        }

        if (!jwtService.isTokenValid(data.getRefreshToken(), user.getEmail())) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        return new RefreshTokenResponseDto(newAccessToken, "Bearer");
    }
}
