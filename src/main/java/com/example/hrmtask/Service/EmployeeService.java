package com.example.hrmtask.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.hrmtask.DTO.EmployeeCreationDTO;
import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Model.Users;
import com.example.hrmtask.Repository.EmployeesRepository;
import com.example.hrmtask.Repository.UsersRepository;

@Service
public class EmployeeService {
    private final EmployeesRepository employeesRepository;
    private final UsersRepository usersRepository;
    public EmployeeService(EmployeesRepository employeesRepository, UsersRepository usersRepository) {
        this.employeesRepository = employeesRepository;
        this.usersRepository = usersRepository;
    }

    public List<Employees> getAllEmployees() {
        return employeesRepository.findAll();
    }

    public Employees getEmployeeById(Long id) {
        return employeesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Employees getEmployeeByCode(String employeeCode) {
        return employeesRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
    }

    public Employees getLastCreatedEmployee() {
        return employeesRepository.findTopByOrderByIdDesc().orElse(null);
    }

    public Employees createEmployee(EmployeeCreationDTO data) {
        if (employeesRepository.existsByEmail(data.getEmail())) {
            throw new RuntimeException("Employee email already exist");
        }
        if (data.getEmployeeCode() != null && employeesRepository.existsByEmployeeCode(data.getEmployeeCode())) {
            throw new RuntimeException("Employee code already exists");
        }
        Employees employee = new Employees();
        employee.setEmployeeCode(data.getEmployeeCode());
        employee.setEmail(data.getEmail());
        employee.setFirstName(data.getFirstName());
        employee.setLastName(data.getLastName());
        employee.setPhone(data.getPhone());
        employee.setDesignation(data.getDesignation());
        employee.setDepartment(data.getDepartment());
        employee.setJoiningDate(data.getJoiningDate());
        employee.setStatus("active");
        employeesRepository.save(employee);
        return employee;
    }

    public Employees editEmployee(Long id, Employees data) {
        Employees employee = employeesRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        return updateEmployeeRecord(employee, data);
    }

    public Employees editEmployeeByCode(String employeeCode, Employees data) {
        Employees employee = employeesRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return updateEmployeeRecord(employee, data);
    }

    private Employees updateEmployeeRecord(Employees employee, Employees data) {
        if (data.getEmail() != null && !data.getEmail().equalsIgnoreCase(employee.getEmail()) && employeesRepository.existsByEmail(data.getEmail())) {
            throw new RuntimeException("Employee email already exist");
        }

        employee.setFirstName(data.getFirstName());
        employee.setLastName(data.getLastName());
        employee.setEmail(data.getEmail());
        employee.setPhone(data.getPhone());
        employee.setDepartment(data.getDepartment());
        employee.setDesignation(data.getDesignation());
        employee.setJoiningDate(data.getJoiningDate());
        employee.setEndingDate(data.getEndingDate());
        employee.setStatus(data.getStatus());

        if (employee.getUserId() != null && data.getEmail() != null) {
            usersRepository.findById(employee.getUserId()).ifPresent(user -> {
                user.setEmail(data.getEmail());
                usersRepository.save(user);
            });
        }

        return employeesRepository.save(employee);
    }

    public Employees updateEmployeeStatus(Long id, String status, LocalDate endingDate) {
        Employees employee = employeesRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        return applyStatusChange(employee, status, endingDate);
    }

    public Employees updateEmployeeStatusByCode(String employeeCode, String status, LocalDate endingDate) {
        Employees employee = employeesRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return applyStatusChange(employee, status, endingDate);
    }

    private Employees applyStatusChange(Employees employee, String status, LocalDate endingDate) {
        employee.setStatus(status);
        if (status.equalsIgnoreCase("RESIGNED") || status.equalsIgnoreCase("TERMINATED") || status.equalsIgnoreCase("INACTIVE")) {
            employee.setEndingDate(endingDate);
            if (employee.getUserId() != null) {
                Users user = usersRepository.findById(employee.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                user.setEnabled(false);
                usersRepository.save(user);
            }
        }
        if (status.equalsIgnoreCase("ACTIVE")) {
            employee.setEndingDate(null);
            if (employee.getUserId() != null) {
                Users user = usersRepository.findById(employee.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                user.setEnabled(true);
                usersRepository.save(user);
            }
        }
        return employeesRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employees employee = employeesRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        performEmployeeDeletion(employee);
    }

    public void deleteEmployeeByCode(String employeeCode) {
        Employees employee = employeesRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        performEmployeeDeletion(employee);
    }

    private void performEmployeeDeletion(Employees employee) {
        if (employee.getUserId() != null) {
            usersRepository.deleteById(employee.getUserId());
        }
        employeesRepository.delete(employee);
    }
}
