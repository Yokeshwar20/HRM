package com.example.hrmtask.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrmtask.Model.PayrollSchedule;

public interface PayrollScheduleRepository extends JpaRepository<PayrollSchedule, Long> {
    List<PayrollSchedule> findByEnabledTrueAndScheduledAtLessThanEqualAndStatus(LocalDateTime now, String status);
    List<PayrollSchedule> findByPayMonthAndPayYearAndStatus(Integer payMonth, Integer payYear, String status);
}
