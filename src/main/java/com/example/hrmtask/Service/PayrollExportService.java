package com.example.hrmtask.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.Month;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.hrmtask.DTO.PayrollExportRequest;
import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Model.Payroll;
import com.example.hrmtask.Repository.EmployeesRepository;
import com.example.hrmtask.Repository.PayrollRepository;

@Service
public class PayrollExportService {

    private final PayrollRepository payrollRepository;
    private final EmployeesRepository employeesRepository;

    public PayrollExportService(PayrollRepository payrollRepository, EmployeesRepository employeesRepository) {
        this.payrollRepository = payrollRepository;
        this.employeesRepository = employeesRepository;
    }

    public void validateRequest(PayrollExportRequest request) {
        if (request == null) {
            throw new RuntimeException("Export request cannot be null");
        }
        if (request.getStartMonth() == null || request.getStartMonth() < 1 || request.getStartMonth() > 12) {
            throw new RuntimeException("Invalid start month: must be between 1 and 12");
        }
        if (request.getEndMonth() == null || request.getEndMonth() < 1 || request.getEndMonth() > 12) {
            throw new RuntimeException("Invalid end month: must be between 1 and 12");
        }
        if (request.getStartYear() == null || request.getStartYear() < 1900) {
            throw new RuntimeException("Invalid start year");
        }
        if (request.getEndYear() == null || request.getEndYear() < 1900) {
            throw new RuntimeException("Invalid end year");
        }

        YearMonth startPeriod = YearMonth.of(request.getStartYear(), request.getStartMonth());
        YearMonth endPeriod = YearMonth.of(request.getEndYear(), request.getEndMonth());

        if (startPeriod.isAfter(endPeriod)) {
            throw new RuntimeException("Invalid date range: start period cannot be after end period");
        }

        if (request.getEmployeeCodes() != null && !request.getEmployeeCodes().isEmpty()) {
            for (String code : request.getEmployeeCodes()) {
                if (code == null || !employeesRepository.existsByEmployeeCode(code)) {
                    throw new RuntimeException("Employee with code " + code + " not found");
                }
            }
        }
    }

    private List<Payroll> getPayrollRecords(PayrollExportRequest request) {
        validateRequest(request);

        List<Payroll> payrolls;
        if (request.getEmployeeCodes() == null || request.getEmployeeCodes().isEmpty()) {
            payrolls = payrollRepository.findPayrollsForDateRange(
                    request.getStartYear(),
                    request.getStartMonth(),
                    request.getEndYear(),
                    request.getEndMonth()
            );
        } else {
            List<Long> employeeIds = request.getEmployeeCodes().stream()
                    .map(code -> employeesRepository.findByEmployeeCode(code)
                            .orElseThrow(() -> new RuntimeException("Employee not found with code: " + code))
                            .getId())
                    .toList();

            payrolls = payrollRepository.findPayrollsForDateRangeAndEmployees(
                    request.getStartYear(),
                    request.getStartMonth(),
                    request.getEndYear(),
                    request.getEndMonth(),
                    employeeIds
            );
        }

        if (payrolls.isEmpty()) {
            throw new RuntimeException("No payroll records found for the selected criteria");
        }

        return payrolls;
    }

    public byte[] downloadPayslipsAsZip(PayrollExportRequest request) {
        List<Payroll> payrolls = getPayrollRecords(request);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            Set<String> addedFileNames = new HashSet<>();

            for (Payroll p : payrolls) {
                if (p.getPayslipPath() == null || p.getPayslipPath().isBlank()) {
                    continue;
                }

                File file = new File(p.getPayslipPath());
                if (!file.exists()) {
                    continue;
                }

                Employees emp = employeesRepository.findById(p.getEmployeeId()).orElse(null);
                String empCode = emp != null && emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "EMP" + p.getEmployeeId();
                String empName = emp != null ? (emp.getFirstName() + "_" + emp.getLastName()).replaceAll("\\s+", "_") : "Employee_" + p.getEmployeeId();
                String monthName = capitalize(Month.of(p.getPayMonth()).name().toLowerCase());

                String baseName = String.format("%s_%s_%s_%d.pdf", empCode, empName, monthName, p.getPayYear());
                String entryName = baseName;
                int counter = 1;
                while (addedFileNames.contains(entryName)) {
                    entryName = baseName.substring(0, baseName.length() - 4) + "_" + counter + ".pdf";
                    counter++;
                }
                addedFileNames.add(entryName);

                byte[] fileBytes = Files.readAllBytes(file.toPath());
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                zos.write(fileBytes);
                zos.closeEntry();
            }

            if (addedFileNames.isEmpty()) {
                throw new RuntimeException("No payslip PDF files found for the selected payroll records");
            }

            zos.finish();
            return baos.toByteArray();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Error generating payslips ZIP archive: " + e.getMessage());
        }
    }

    public byte[] generatePayrollExcel(PayrollExportRequest request) {
        List<Payroll> payrolls = getPayrollRecords(request);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Payroll Report");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {
                "Employee Code", "Employee Name", "Department", "Designation",
                "Pay Month", "Pay Year", "Basic Salary", "HRA", "Allowance",
                "Gross Salary", "PF", "Other Deduction", "Total Deduction",
                "Net Salary", "Email Status", "Processed At"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Payroll p : payrolls) {
                Row row = sheet.createRow(rowIdx++);
                Employees emp = employeesRepository.findById(p.getEmployeeId()).orElse(null);

                row.createCell(0).setCellValue(emp != null && emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "");
                row.createCell(1).setCellValue(emp != null ? (emp.getFirstName() + " " + emp.getLastName()) : "");
                row.createCell(2).setCellValue(emp != null && emp.getDepartment() != null ? emp.getDepartment() : "");
                row.createCell(3).setCellValue(emp != null && emp.getDesignation() != null ? emp.getDesignation() : "");
                row.createCell(4).setCellValue(p.getPayMonth() != null ? p.getPayMonth() : 0);
                row.createCell(5).setCellValue(p.getPayYear() != null ? p.getPayYear() : 0);

                row.createCell(6).setCellValue(p.getBasicSalary() != null ? p.getBasicSalary().doubleValue() : 0.0);
                row.createCell(7).setCellValue(p.getHra() != null ? p.getHra().doubleValue() : 0.0);
                row.createCell(8).setCellValue(p.getAllowance() != null ? p.getAllowance().doubleValue() : 0.0);
                row.createCell(9).setCellValue(p.getGrossSalary() != null ? p.getGrossSalary().doubleValue() : 0.0);
                row.createCell(10).setCellValue(p.getPf() != null ? p.getPf().doubleValue() : 0.0);
                row.createCell(11).setCellValue(p.getOtherDeduction() != null ? p.getOtherDeduction().doubleValue() : 0.0);
                row.createCell(12).setCellValue(p.getTotalDeduction() != null ? p.getTotalDeduction().doubleValue() : 0.0);
                row.createCell(13).setCellValue(p.getNetSalary() != null ? p.getNetSalary().doubleValue() : 0.0);

                row.createCell(14).setCellValue(p.getEmailStatus() != null ? p.getEmailStatus() : "");
                row.createCell(15).setCellValue(p.getProcessedAt() != null ? p.getProcessedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Error generating payroll Excel report: " + e.getMessage());
        }
    }

    public String getZipFilename(PayrollExportRequest req) {
        return String.format("payslips-%02d-%d-to-%02d-%d.zip", req.getStartMonth(), req.getStartYear(), req.getEndMonth(), req.getEndYear());
    }

    public String getExcelFilename(PayrollExportRequest req) {
        return String.format("payroll-report-%02d-%d-to-%02d-%d.xlsx", req.getStartMonth(), req.getStartYear(), req.getEndMonth(), req.getEndYear());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
