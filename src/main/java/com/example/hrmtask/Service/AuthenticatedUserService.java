package com.example.hrmtask.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Model.Users;
import com.example.hrmtask.Repository.EmployeesRepository;
import com.example.hrmtask.Repository.UsersRepository;

@Service
public class AuthenticatedUserService {

    private final UsersRepository usersRepository;
    private final EmployeesRepository employeesRepository;

    public AuthenticatedUserService(UsersRepository usersRepository, EmployeesRepository employeesRepository) {
        this.usersRepository = usersRepository;
        this.employeesRepository = employeesRepository;
    }

    public String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User is not authenticated");
        }
        return authentication.getName();
    }

    public Users getAuthenticatedUser() {
        String email = getAuthenticatedEmail();
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public Employees getAuthenticatedEmployee() {
        Users user = getAuthenticatedUser();

        if (user.getId() != null) {
            Employees employee = employeesRepository.findByUserId(user.getId()).orElse(null);
            if (employee != null) {
                return employee;
            }
        }

        return employeesRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee record not found for authenticated user: " + user.getEmail()));
    }
}
