package com.example.hrmtask.Service;

import java.math.BigDecimal;
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

        BigDecimal basicSalary = dto.getBasicSalary() != null ? dto.getBasicSalary() : BigDecimal.ZERO;
        BigDecimal hra = dto.getHra() != null ? dto.getHra() : BigDecimal.ZERO;
        BigDecimal allowance = dto.getAllowance() != null ? dto.getAllowance() : BigDecimal.ZERO;
        BigDecimal calculatedGross = basicSalary.add(hra).add(allowance);
        BigDecimal grossSalary = dto.getGrossSalary() != null ? dto.getGrossSalary() : calculatedGross;

        SalaryStructure structure = new SalaryStructure();
        structure.setEmployeeId(dto.getEmployeeId());
        structure.setBasicSalary(basicSalary);
        structure.setHra(hra);
        structure.setAllowance(allowance);
        structure.setGrossSalary(grossSalary);
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

        BigDecimal basicSalary = dto.getBasicSalary() != null ? dto.getBasicSalary() : (existing.getBasicSalary() != null ? existing.getBasicSalary() : BigDecimal.ZERO);
        BigDecimal hra = dto.getHra() != null ? dto.getHra() : (existing.getHra() != null ? existing.getHra() : BigDecimal.ZERO);
        BigDecimal allowance = dto.getAllowance() != null ? dto.getAllowance() : (existing.getAllowance() != null ? existing.getAllowance() : BigDecimal.ZERO);
        BigDecimal calculatedGross = basicSalary.add(hra).add(allowance);
        BigDecimal grossSalary = dto.getGrossSalary() != null ? dto.getGrossSalary() : calculatedGross;

        SalaryStructure newStructure = new SalaryStructure();
        newStructure.setEmployeeId(empId);
        newStructure.setBasicSalary(basicSalary);
        newStructure.setHra(hra);
        newStructure.setAllowance(allowance);
        newStructure.setGrossSalary(grossSalary);
        newStructure.setPf(dto.getPf() != null ? dto.getPf() : existing.getPf());
        newStructure.setOtherDeduction(dto.getOtherDeduction() != null ? dto.getOtherDeduction() : existing.getOtherDeduction());
        newStructure.setEffectiveFrom(dto.getEffectiveFrom() != null ? dto.getEffectiveFrom() : LocalDate.now());

        return salaryStructureRepository.save(newStructure);
    }

    public List<SalaryStructure> getSalaryStructureHistory(Long employeeId) {
        return salaryStructureRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);
    }
}
