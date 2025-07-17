package net.tetradtech.hrms_leave_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.tetradtech.hrms_leave_service.Enum.AttendanceSource;
import net.tetradtech.hrms_leave_service.Enum.AttendanceStatus;
import net.tetradtech.hrms_leave_service.Enum.DayOffType;

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
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    private AttendanceSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "DayOff_Type")
    private DayOffType DayOffType;

//    private String notes;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
