package com.example.hrmtask.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrmtask.Model.SalaryStructure;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure,Long>{
    Optional<SalaryStructure> findTopByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);
    List<SalaryStructure> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);
}
