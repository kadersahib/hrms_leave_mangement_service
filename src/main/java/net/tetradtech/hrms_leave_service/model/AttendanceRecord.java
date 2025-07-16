package net.tetradtech.hrms_leave_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.tetradtech.hrms_leave_service.Enum.AttendanceSource;
import net.tetradtech.hrms_leave_service.Enum.AttendanceStatus;
import net.tetradtech.hrms_leave_service.Enum.AttendanceType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "date"}) // prevent duplicate for same day
)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private LocalDate date;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private boolean isLate;
    private boolean isDeleted;
    private boolean isWorkingDay;


    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;  // PRESENT, ABSENT, LEAVE

    @Enumerated(EnumType.STRING)
    private AttendanceSource source;  // MANUAL, AUTO, LEAVE_SYSTEM

    @Enumerated(EnumType.STRING)
    private AttendanceType attendanceType; // FULL_TIME, HALF_DAY, NULL if not clocked-out

    private String notes;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
