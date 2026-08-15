package com.example.hrmtask.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<SalaryStructure> getEmployeeSalaryStructure(@PathVariable Long employeeId) {
        SalaryStructure structure = salaryStructureService.getEmployeeSalaryStructure(employeeId);
        return ResponseEntity.ok(structure);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaryStructure> updateSalaryStructure(@PathVariable Long id, @RequestBody SalaryStructureDto dto) {
        SalaryStructure updated = salaryStructureService.updateSalaryStructure(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<List<SalaryStructure>> getSalaryStructureHistory(@PathVariable Long employeeId) {
        List<SalaryStructure> history = salaryStructureService.getSalaryStructureHistory(employeeId);
        return ResponseEntity.ok(history);
    }
}
