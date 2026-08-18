package com.example.hrmtask.Service;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:forbotlogin3@gmail.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPayslip(String email, String filePath) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("Your Payslip");
        helper.setText("Please find your attached payslip.");
        FileSystemResource file = new FileSystemResource(new File(filePath));
        helper.addAttachment(file.getFilename(), file);
        mailSender.send(message);
    }

    public void sendLeaveNotification(String email, String subject, String body) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(body);
        mailSender.send(message);
    }

    public void sendPayslipWithDetails(String email, String filePath, String employeeName, Integer payMonth, Integer payYear, java.math.BigDecimal netSalary) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("Payslip for " + payMonth + "/" + payYear);
        String body = String.format(
            "Dear %s,\n\nPlease find your attached payslip for %d/%d.\nNet Salary: %s\n\nBest regards,\nHR Department",
            employeeName, payMonth, payYear, netSalary != null ? netSalary.toString() : "0.00"
        );
        helper.setText(body);
        FileSystemResource file = new FileSystemResource(new File(filePath));
        helper.addAttachment(file.getFilename(), file);
        mailSender.send(message);
    }

    public boolean sendPayslipWithRetry(String email, String filePath, String employeeName, Integer payMonth, Integer payYear, java.math.BigDecimal netSalary, int maxRetries, long delayMillis) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                sendPayslipWithDetails(email, filePath, employeeName, payMonth, payYear, netSalary);
                return true;
            } catch (Exception e) {
                attempt++;
                System.err.println("Email send attempt " + attempt + " of " + maxRetries + " failed for " + email + ": " + e.getMessage());
                if (attempt < maxRetries && delayMillis > 0) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return false;
    }
}
