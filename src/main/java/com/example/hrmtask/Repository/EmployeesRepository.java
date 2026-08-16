package com.example.hrmtask.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrmtask.Model.Employees;


public interface EmployeesRepository extends JpaRepository<Employees,Long>{
    Optional<Employees> findByEmail(String email);

    Optional<Employees> findByUserId(Long userId);
    
    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    List<Employees> findByStatusIgnoreCase(String status);
}
