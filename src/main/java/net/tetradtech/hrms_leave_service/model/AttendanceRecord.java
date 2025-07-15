package net.tetradtech.hrms_leave_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attendance_records")

public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private LocalDate date;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private boolean isLate;
    private boolean isDeleted ;
    private boolean absent ;

    private String status; // PRESENT, ABSENT, LEAVE, WEEKEND, HOLIDAY
    private String source; // AUTO, MANUAL, LEAVE_SYSTEM
    private String notes;
    private boolean isWorkingDay;
    private String attendanceType; // FULL_DAY, HALF_DAY

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
