
package net.tetradtech.hrms_leave_service.model;

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
@Table(
        name = "leave_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "start_date", "end_date"})
)
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

    @NotBlank(message = "Reason is required")
    private String reason;

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
    private LocalDate approvedFrom;
    private LocalDate approvedTo;
    private LocalDateTime approvalTimestamp;


    // Audit fields
    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Soft delete fields
    private boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
