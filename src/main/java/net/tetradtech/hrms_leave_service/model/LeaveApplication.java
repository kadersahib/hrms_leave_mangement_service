
package net.tetradtech.hrms_leave_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "leave_applications")
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "leaveTypeid")
    private Long leaveTypeId;

    @Column(name = "reportingTo")
    private Long reportingId;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "applied_days")
    private Integer appliedDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeaveStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "dayoff")
    private DayOffType dayOffType;

    @Column(name = "reason")
    private String reason;

    @Column(name = "otherReason")
    private String leaveOtherReason;


    @Column(name = "remaining_days")
    private Integer remainingDays;

    @Column(name = "document_path")
    private String documentPath;

    @Column(name = "document_name")
    private String documentName;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] documentData;


    @Column(name = "approverComments")
    private String approverComment;

    @Column(name = "approverId")
    private Long approverId;


    // Audit fields
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private String updatedBy;

    // Soft delete fields
    @Column(name = "isDeleted")
    private boolean isDeleted;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;

    @Column(name = "deletedBy")
    private String deletedBy;


}
