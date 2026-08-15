package com.example.hrmtask.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hrmtask.Model.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    boolean existsByEmployeeIdAndPayMonthAndPayYear(Long employeeId, Integer payMonth, Integer payYear);
    Optional<Payroll> findByEmployeeIdAndPayMonthAndPayYear(Long employeeId, Integer payMonth, Integer payYear);
    List<Payroll> findByEmployeeIdOrderByPayYearDescPayMonthDesc(Long employeeId);
    List<Payroll> findByPayMonthAndPayYear(Integer payMonth, Integer payYear);
    List<Payroll> findByEmailStatusIgnoreCase(String emailStatus);

    @Query("SELECT p FROM Payroll p WHERE " +
           "(p.payYear > :startYear OR (p.payYear = :startYear AND p.payMonth >= :startMonth)) AND " +
           "(p.payYear < :endYear OR (p.payYear = :endYear AND p.payMonth <= :endMonth)) " +
           "ORDER BY p.payYear ASC, p.payMonth ASC, p.employeeId ASC")
    List<Payroll> findPayrollsForDateRange(
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth
    );

    @Query("SELECT p FROM Payroll p WHERE " +
           "(p.payYear > :startYear OR (p.payYear = :startYear AND p.payMonth >= :startMonth)) AND " +
           "(p.payYear < :endYear OR (p.payYear = :endYear AND p.payMonth <= :endMonth)) AND " +
           "p.employeeId IN :employeeIds " +
           "ORDER BY p.payYear ASC, p.payMonth ASC, p.employeeId ASC")
    List<Payroll> findPayrollsForDateRangeAndEmployees(
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth,
            @Param("employeeIds") List<Long> employeeIds
    );
}