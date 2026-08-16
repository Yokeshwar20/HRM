package com.example.hrmtask.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrmtask.DTO.SalaryStructureDto;
import com.example.hrmtask.Model.SalaryStructure;
import com.example.hrmtask.Service.SalaryStructureService;

@RestController
@RequestMapping("/api/hr/salary-structure")
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    public SalaryStructureController(SalaryStructureService salaryStructureService) {
        this.salaryStructureService = salaryStructureService;
    }

    @PostMapping
    public ResponseEntity<SalaryStructure> createSalaryStructure(@RequestBody SalaryStructureDto dto) {
        SalaryStructure created = salaryStructureService.createSalaryStructure(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/employee/{employeeCode}")
    public ResponseEntity<SalaryStructure> getEmployeeSalaryStructureByCode(@PathVariable String employeeCode) {
        SalaryStructure structure = salaryStructureService.getEmployeeSalaryStructureByCode(employeeCode);
        return ResponseEntity.ok(structure);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaryStructure> updateSalaryStructure(@PathVariable Long id, @RequestBody SalaryStructureDto dto) {
        SalaryStructure updated = salaryStructureService.updateSalaryStructure(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/employee/{employeeCode}/history")
    public ResponseEntity<List<SalaryStructure>> getSalaryStructureHistoryByCode(@PathVariable String employeeCode) {
        List<SalaryStructure> history = salaryStructureService.getSalaryStructureHistoryByCode(employeeCode);
        return ResponseEntity.ok(history);
    }
}
