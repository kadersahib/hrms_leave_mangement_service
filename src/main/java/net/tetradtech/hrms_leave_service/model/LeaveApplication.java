
package net.tetradtech.hrms_leave_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import net.tetradtech.hrms_leave_service.Enum.DayOffType;
import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

@Table(name = "leave_applications")
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "User ID is required")
    @Column(name = "userId", nullable = false)
    private Long userId;

    @NotNull(message = "Leave type ID is required")
    @Column(name = "leaveTypeId", nullable = false)
    private Long leaveTypeId;

    @NotNull(message = "Start date is required")
    @Column(name = "startDate")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "endDate")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeaveStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "dayoff")
    private DayOffType dayOffType;

    @Column(name = "reportingManger")
    private String reportingManager;

    @Column(name = "maxDays")
    private int maxDays;

    @Column(name = "appliedDays")
    private int appliedDays;

    @Column(name = "isActive")
    private Boolean active = true;

    @Column(name = "remainingDays")
    private Integer remainingDays;

    @Column(name = "approvalComment")
    private String approvalComment;

    @Column(name = "approveBy")
    private String approvedBy;

    @Column(name = "approvalTime")
    private LocalDateTime approvalTimestamp;

    @Column(name = "cancelledBy")
    private String cancelledBy;

    @Column(name = "cancelledAt")
    private LocalDateTime cancelledAt;

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
