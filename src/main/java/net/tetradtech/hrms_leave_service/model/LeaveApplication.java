
package net.tetradtech.hrms_leave_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
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
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Leave type ID is required")
    @Column(name = "leave_type_id", nullable = false)
    private Long leaveTypeId;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeaveStatus status;

    private String reportingManager;

    private int maxDays;
    private int appliedDays;

    private boolean active = true;

    @Column(name = "remaining_days")
    private Integer remainingDays;

    private String approvalComment;
    private String approvedBy;
    @Column(name = "approval_time")
    private LocalDateTime approvalTimestamp;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

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
