package com.example.hrmtask.Service;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.hrmtask.DTO.BrevoEmailRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    public EmailService(
            @Value("${brevo.api.key:}") String apiKey,
            @Value("${brevo.from.email:forbotlogin3@gmail.com}") String fromEmail,
            @Value("${brevo.from.name:HR Management System}") String fromName,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<String> sendLeaveNotification(String recipientEmail, String subject, String bodyText) {
        BrevoEmailRequestDto request = BrevoEmailRequestDto.builder()
                .sender(new BrevoEmailRequestDto.Sender(fromName, fromEmail))
                .to(List.of(new BrevoEmailRequestDto.Recipient(recipientEmail, recipientEmail)))
                .subject(subject)
                .textContent(bodyText)
                .build();

        return sendBrevoEmailAsync(request, recipientEmail);
    }

    public CompletableFuture<String> sendPayslip(String recipientEmail, String filePath) {
        return sendPayslipWithDetails(recipientEmail, filePath, "Employee", null, null, null);
    }

    public CompletableFuture<String> sendPayslipWithDetails(String recipientEmail, String filePath, String employeeName, Integer payMonth, Integer payYear, java.math.BigDecimal netSalary) {
        String subject = (payMonth != null && payYear != null) ? "Payslip for " + payMonth + "/" + payYear : "Your Payslip";
        String body = String.format(
            "Dear %s,\n\nPlease find your attached payslip for %s.\nNet Salary: %s\n\nBest regards,\nHR Department",
            employeeName != null ? employeeName : "Employee",
            (payMonth != null && payYear != null) ? payMonth + "/" + payYear : "the period",
            netSalary != null ? netSalary.toString() : "0.00"
        );

        List<BrevoEmailRequestDto.Attachment> attachments = null;
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    String base64Content = Base64.getEncoder().encodeToString(fileBytes);
                    attachments = List.of(new BrevoEmailRequestDto.Attachment(base64Content, file.getName()));
                } catch (Exception e) {
                    logger.error("Failed to read payslip PDF attachment at path {}: {}", filePath, e.getMessage());
                }
            } else {
                logger.warn("Payslip file not found at path: {}", filePath);
            }
        }

        BrevoEmailRequestDto request = BrevoEmailRequestDto.builder()
                .sender(new BrevoEmailRequestDto.Sender(fromName, fromEmail))
                .to(List.of(new BrevoEmailRequestDto.Recipient(recipientEmail, employeeName)))
                .subject(subject)
                .textContent(body)
                .attachment(attachments)
                .build();

        return sendBrevoEmailAsync(request, recipientEmail);
    }

    public boolean sendPayslipWithRetry(String email, String filePath, String employeeName, Integer payMonth, Integer payYear, java.math.BigDecimal netSalary, int maxRetries, long delayMillis) {
        try {
            sendPayslipWithDetails(email, filePath, employeeName, payMonth, payYear, netSalary)
                .whenComplete((response, error) -> {
                    if (error != null) {
                        logger.error("Failed to send payslip email to {}: {}", email, error.getMessage());
                    } else {
                        logger.info("Successfully sent payslip email to {}", email);
                    }
                });
            return true;
        } catch (Exception e) {
            logger.error("Error initiating payslip email to {}: {}", email, e.getMessage());
            return false;
        }
    }

    private CompletableFuture<String> sendBrevoEmailAsync(BrevoEmailRequestDto requestDto, String recipientEmail) {
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("Brevo API key is not configured. Skipping email to {}", recipientEmail);
            return CompletableFuture.completedFuture("Brevo API key missing");
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(requestDto);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            logger.info("Successfully sent email via Brevo HTTP API to {}", recipientEmail);
                            return response.body();
                        } else {
                            logger.error("Failed to send email via Brevo HTTP API to {}. HTTP Status: {}, Body: {}",
                                    recipientEmail, response.statusCode(), response.body());
                            throw new RuntimeException("Brevo API returned HTTP status: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        logger.error("Failed to send email via Brevo HTTP API to {}: {}", recipientEmail, ex.getMessage());
                        return "Failed to send email";
                    });
        } catch (Exception e) {
            logger.error("Failed to serialize Brevo email payload for {}: {}", recipientEmail, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
