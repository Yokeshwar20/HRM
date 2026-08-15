package com.example.hrmtask.Service;

import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Model.Payroll;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;

@Service
public class PdfService {

    public String generatePayslip(Payroll payroll, Employees employee) throws Exception {
        File dir = new File("payslips");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = "payslips/payslip_" + payroll.getEmployeeId() + "_" + payroll.getPayMonth() + "_" + payroll.getPayYear() + ".pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        document.add(new Paragraph("EMPLOYEE PAYSLIP"));
        document.add(new Paragraph(" "));

        if (employee != null) {
            String name = (employee.getFirstName() != null ? employee.getFirstName() : "") + " " + (employee.getLastName() != null ? employee.getLastName() : "");
            document.add(new Paragraph("Employee Name: " + name.trim()));
            document.add(new Paragraph("Employee Code: " + (employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "")));
            document.add(new Paragraph("Department: " + (employee.getDepartment() != null ? employee.getDepartment() : "")));
            document.add(new Paragraph("Designation: " + (employee.getDesignation() != null ? employee.getDesignation() : "")));
        } else {
            document.add(new Paragraph("Employee ID: " + payroll.getEmployeeId()));
        }

        document.add(new Paragraph("Pay Month: " + payroll.getPayMonth()));
        document.add(new Paragraph("Pay Year: " + payroll.getPayYear()));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);

        table.addCell("Basic Salary");
        table.addCell(payroll.getBasicSalary() != null ? payroll.getBasicSalary().toString() : "0.00");

        table.addCell("HRA");
        table.addCell(payroll.getHra() != null ? payroll.getHra().toString() : "0.00");

        table.addCell("Allowance");
        table.addCell(payroll.getAllowance() != null ? payroll.getAllowance().toString() : "0.00");

        table.addCell("Gross Salary");
        table.addCell(payroll.getGrossSalary() != null ? payroll.getGrossSalary().toString() : "0.00");

        table.addCell("PF");
        table.addCell(payroll.getPf() != null ? payroll.getPf().toString() : "0.00");

        table.addCell("Other Deduction");
        table.addCell(payroll.getOtherDeduction() != null ? payroll.getOtherDeduction().toString() : "0.00");

        table.addCell("Total Deduction");
        table.addCell(payroll.getTotalDeduction() != null ? payroll.getTotalDeduction().toString() : "0.00");

        table.addCell("Net Salary");
        table.addCell(payroll.getNetSalary() != null ? payroll.getNetSalary().toString() : "0.00");

        document.add(table);
        document.close();

        return filePath;
    }

    public String generatePayslip(Payroll payroll) throws Exception {
        return generatePayslip(payroll, null);
    }
}
