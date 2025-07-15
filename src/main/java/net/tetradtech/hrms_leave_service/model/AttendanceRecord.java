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

    private boolean late = false;

    private boolean absent = false;


    // Audit fields
    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    // Soft delete fields
    private boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;


}
