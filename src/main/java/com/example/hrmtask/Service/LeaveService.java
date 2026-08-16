package com.example.hrmtask.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hrmtask.DTO.LeaveDecisionDto;
import com.example.hrmtask.DTO.LeaveRequestDto;
import com.example.hrmtask.Model.Employees;
import com.example.hrmtask.Model.LeaveHistory;
import com.example.hrmtask.Model.LeavePolicy;
import com.example.hrmtask.Model.LeaveRequest;
import com.example.hrmtask.Repository.EmployeesRepository;
import com.example.hrmtask.Repository.LeaveHistoryRepository;
import com.example.hrmtask.Repository.LeavePolicyRepository;
import com.example.hrmtask.Repository.LeaveRequestRepository;

@Service
public class LeaveService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveHistoryRepository leaveHistoryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeesRepository employeesRepository;
    private final EmailService emailService;
    private final AuthenticatedUserService authenticatedUserService;

    public LeaveService(LeavePolicyRepository leavePolicyRepository,
                        LeaveHistoryRepository leaveHistoryRepository,
                        LeaveRequestRepository leaveRequestRepository,
                        EmployeesRepository employeesRepository,
                        EmailService emailService,
                        AuthenticatedUserService authenticatedUserService) {
        this.leavePolicyRepository = leavePolicyRepository;
        this.leaveHistoryRepository = leaveHistoryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeesRepository = employeesRepository;
        this.emailService = emailService;
        this.authenticatedUserService = authenticatedUserService;
    }


    public LeavePolicy createPolicy(LeavePolicy policy) {
        if (leavePolicyRepository.findByLeaveType(policy.getLeaveType()).isPresent()) {
            throw new RuntimeException("Leave policy for type '" + policy.getLeaveType() + "' already exists");
        }
        return leavePolicyRepository.save(policy);
    }

    public LeavePolicy updatePolicy(Long id, LeavePolicy policyDetails) {
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave policy not found"));
        policy.setLeaveType(policyDetails.getLeaveType());
        policy.setTotalDays(policyDetails.getTotalDays());
        return leavePolicyRepository.save(policy);
    }

    public List<LeavePolicy> getAllPolicies() {
        return leavePolicyRepository.findAll();
    }

    public void deletePolicy(Long id) {
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave policy not found"));
        leavePolicyRepository.delete(policy);
    }


    public LeaveRequest applyForLeave(LeaveRequestDto dto) {
        Employees employee = authenticatedUserService.getAuthenticatedEmployee();

        LeavePolicy policy = leavePolicyRepository.findByLeaveType(dto.getLeaveType())
                .orElseThrow(() -> new RuntimeException("Leave policy not found for type: " + dto.getLeaveType()));

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        long requestedDaysCount = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        BigDecimal requestedDays = BigDecimal.valueOf(requestedDaysCount);

        Optional<LeaveHistory> leaveHistoryOpt = leaveHistoryRepository
                .findTopByEmployeeIdAndLeaveTypeOrderByEndDateDesc(employee.getId(), dto.getLeaveType());

        BigDecimal availableDays;

        if (leaveHistoryOpt.isPresent()) {
            LeaveHistory existingHistory = leaveHistoryOpt.get();
            availableDays = existingHistory.getRemainingDays() != null ? existingHistory.getRemainingDays() : BigDecimal.ZERO;
        } else {
            availableDays = policy.getTotalDays() != null ? policy.getTotalDays() : BigDecimal.ZERO;
        }

        if (availableDays.compareTo(requestedDays) < 0) {
            throw new RuntimeException("Insufficient leave balance. Remaining: " + availableDays + ", Requested: " + requestedDays);
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(employee.getId());
        leaveRequest.setLeaveHistoryId(null);
        leaveRequest.setLeaveType(dto.getLeaveType());
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setStatus("PENDING");
        leaveRequest.setAppliedAt(LocalDateTime.now());
        leaveRequest.setHrComment(null);
        leaveRequest.setDecidedAt(null);

        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequest> getEmployeeLeaveRequests() {
        Employees employee = authenticatedUserService.getAuthenticatedEmployee();
        return leaveRequestRepository.findByEmployeeId(employee.getId());
    }

    public List<LeaveHistory> getEmployeeLeaveHistory() {
        Employees employee = authenticatedUserService.getAuthenticatedEmployee();
        return leaveHistoryRepository.findByEmployeeId(employee.getId());
    }

    public List<LeaveRequest> getEmployeeLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveHistory> getEmployeeLeaveHistory(Long employeeId) {
        return leaveHistoryRepository.findByEmployeeId(employeeId);
    }


    @Transactional
    public LeaveRequest approveLeaveRequest(Long id, LeaveDecisionDto dto) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!"PENDING".equalsIgnoreCase(leaveRequest.getStatus())) {
            throw new RuntimeException("Leave request is not in PENDING status");
        }

        long requestedDaysCount = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        BigDecimal requestedDays = BigDecimal.valueOf(requestedDaysCount);

        LeaveHistory leaveHistory = null;
        if (leaveRequest.getLeaveHistoryId() != null) {
            leaveHistory = leaveHistoryRepository.findById(leaveRequest.getLeaveHistoryId()).orElse(null);
        }

        String leaveType = leaveRequest.getLeaveType();
        if (leaveHistory == null && leaveType != null) {
            leaveHistory = leaveHistoryRepository
                    .findTopByEmployeeIdAndLeaveTypeOrderByEndDateDesc(leaveRequest.getEmployeeId(), leaveType)
                    .orElse(null);
        }

        if (leaveHistory == null) {
            if (leaveType == null) {
                throw new RuntimeException("Cannot identify leave type for request id: " + id);
            }
            LeavePolicy policy = leavePolicyRepository.findByLeaveType(leaveType)
                    .orElseThrow(() -> new RuntimeException("Leave policy not found for type: " + leaveType));

            leaveHistory = new LeaveHistory();
            leaveHistory.setEmployeeId(leaveRequest.getEmployeeId());
            leaveHistory.setLeaveType(policy.getLeaveType());
            leaveHistory.setTotalDays(policy.getTotalDays());
            leaveHistory.setUsedDays(BigDecimal.ZERO);
            leaveHistory.setRemainingDays(policy.getTotalDays());
        }

        if (leaveHistory.getRemainingDays() == null || leaveHistory.getRemainingDays().compareTo(requestedDays) < 0) {
            throw new RuntimeException("Insufficient leave balance remaining");
        }

        BigDecimal currentUsed = leaveHistory.getUsedDays() != null ? leaveHistory.getUsedDays() : BigDecimal.ZERO;
        leaveHistory.setUsedDays(currentUsed.add(requestedDays));
        leaveHistory.setRemainingDays(leaveHistory.getRemainingDays().subtract(requestedDays));
        leaveHistory.setStartDate(leaveRequest.getStartDate());
        leaveHistory.setEndDate(leaveRequest.getEndDate());
        LeaveHistory savedHistory = leaveHistoryRepository.save(leaveHistory);

        leaveRequest.setLeaveHistoryId(savedHistory.getId());
        leaveRequest.setStatus("APPROVED");
        if (dto != null && dto.getHrComment() != null) {
            leaveRequest.setHrComment(dto.getHrComment());
        }
        leaveRequest.setDecidedAt(LocalDateTime.now());
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        Employees employee = employeesRepository.findById(leaveRequest.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        try {
            String subject = "Leave Request Approved";
            String hrCommentText = savedRequest.getHrComment() != null ? savedRequest.getHrComment() : "N/A";
            String body = String.format(
                "Dear %s %s,\n\n" +
                "Your leave request has been APPROVED.\n\n" +
                "Details:\n" +
                "- Leave Type: %s\n" +
                "- Start Date: %s\n" +
                "- End Date: %s\n" +
                "- Number of Days: %d\n" +
                "- Decision: APPROVED\n" +
                "- HR Comment: %s\n\n" +
                "Best regards,\nHR Department",
                employee.getFirstName(), employee.getLastName(),
                savedHistory.getLeaveType(),
                savedRequest.getStartDate(),
                savedRequest.getEndDate(),
                requestedDaysCount,
                hrCommentText
            );

            emailService.sendLeaveNotification(employee.getEmail(), subject, body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email notification: " + e.getMessage(), e);
        }

        return savedRequest;
    }

    @Transactional
    public LeaveRequest rejectLeaveRequest(Long id, LeaveDecisionDto dto) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!"PENDING".equalsIgnoreCase(leaveRequest.getStatus())) {
            throw new RuntimeException("Leave request is not in PENDING status");
        }

        leaveRequest.setStatus("REJECTED");
        if (dto != null && dto.getHrComment() != null) {
            leaveRequest.setHrComment(dto.getHrComment());
        }
        leaveRequest.setDecidedAt(LocalDateTime.now());
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        String leaveType = leaveRequest.getLeaveType();
        if (leaveType == null && leaveRequest.getLeaveHistoryId() != null) {
            LeaveHistory history = leaveHistoryRepository.findById(leaveRequest.getLeaveHistoryId()).orElse(null);
            if (history != null) {
                leaveType = history.getLeaveType();
            }
        }
        if (leaveType == null) {
            leaveType = "N/A";
        }

        long requestedDaysCount = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;

        Employees employee = employeesRepository.findById(leaveRequest.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        try {
            String subject = "Leave Request Rejected";
            String hrCommentText = savedRequest.getHrComment() != null ? savedRequest.getHrComment() : "N/A";
            String body = String.format(
                "Dear %s %s,\n\n" +
                "Your leave request has been REJECTED.\n\n" +
                "Details:\n" +
                "- Leave Type: %s\n" +
                "- Start Date: %s\n" +
                "- End Date: %s\n" +
                "- Number of Days: %d\n" +
                "- Decision: REJECTED\n" +
                "- HR Comment / Reason: %s\n\n" +
                "Best regards,\nHR Department",
                employee.getFirstName(), employee.getLastName(),
                leaveType,
                savedRequest.getStartDate(),
                savedRequest.getEndDate(),
                requestedDaysCount,
                hrCommentText
            );

            emailService.sendLeaveNotification(employee.getEmail(), subject, body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email notification: " + e.getMessage(), e);
        }

        return savedRequest;
    }

    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus("PENDING");
    }

    public List<LeaveHistory> getEmployeeLeaveHistoryForHR(Long employeeId) {
        return leaveHistoryRepository.findByEmployeeId(employeeId);
    }
}
