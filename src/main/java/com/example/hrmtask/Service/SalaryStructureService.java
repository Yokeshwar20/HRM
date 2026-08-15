package com.example.hrmtask.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.hrmtask.DTO.SalaryStructureDto;
import com.example.hrmtask.Model.SalaryStructure;
import com.example.hrmtask.Repository.EmployeesRepository;
import com.example.hrmtask.Repository.SalaryStructureRepository;

@Service
public class SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeesRepository employeesRepository;

    public SalaryStructureService(SalaryStructureRepository salaryStructureRepository, EmployeesRepository employeesRepository) {
        this.salaryStructureRepository = salaryStructureRepository;
        this.employeesRepository = employeesRepository;
    }

    public SalaryStructure createSalaryStructure(SalaryStructureDto dto) {
        employeesRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        SalaryStructure structure = new SalaryStructure();
        structure.setEmployeeId(dto.getEmployeeId());
        structure.setBasicSalary(dto.getBasicSalary());
        structure.setHra(dto.getHra());
        structure.setAllowance(dto.getAllowance());
        structure.setPf(dto.getPf());
        structure.setOtherDeduction(dto.getOtherDeduction());
        structure.setEffectiveFrom(dto.getEffectiveFrom() != null ? dto.getEffectiveFrom() : LocalDate.now());

        return salaryStructureRepository.save(structure);
    }

    public SalaryStructure getEmployeeSalaryStructure(Long employeeId) {
        return salaryStructureRepository.findTopByEmployeeIdOrderByEffectiveFromDesc(employeeId)
                .orElseThrow(() -> new RuntimeException("Salary structure not found for employee id: " + employeeId));
    }

    public SalaryStructure updateSalaryStructure(Long id, SalaryStructureDto dto) {
        SalaryStructure existing = salaryStructureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary structure not found with id: " + id));

        Long empId = dto.getEmployeeId() != null ? dto.getEmployeeId() : existing.getEmployeeId();

        SalaryStructure newStructure = new SalaryStructure();
        newStructure.setEmployeeId(empId);
        newStructure.setBasicSalary(dto.getBasicSalary());
        newStructure.setHra(dto.getHra());
        newStructure.setAllowance(dto.getAllowance());
        newStructure.setPf(dto.getPf());
        newStructure.setOtherDeduction(dto.getOtherDeduction());
        newStructure.setEffectiveFrom(dto.getEffectiveFrom() != null ? dto.getEffectiveFrom() : LocalDate.now());

        return salaryStructureRepository.save(newStructure);
    }

    public List<SalaryStructure> getSalaryStructureHistory(Long employeeId) {
        return salaryStructureRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);
    }
}
