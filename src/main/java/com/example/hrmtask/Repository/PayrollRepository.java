package com.example.hrmtask.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrmtask.Model.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll,Long>{
    boolean existsByEmployeeIdAndPayMonthAndPayYear(Long employeeId, Integer payMonth, Integer payYear);
    Optional<Payroll> findByEmployeeIdAndPayMonthAndPayYear(Long employeeId, Integer payMonth, Integer payYear);
    List<Payroll> findByEmployeeIdOrderByPayYearDescPayMonthDesc(Long employeeId);
    List<Payroll> findByPayMonthAndPayYear(Integer payMonth, Integer payYear);
    List<Payroll> findByEmailStatusIgnoreCase(String emailStatus);
}