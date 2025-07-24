
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
@JsonInclude(JsonInclude.Include.NON_NULL)

@Table(name = "leave_applications")
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;


    @Column(name = "leave_type_name")
    private String leaveTypeName;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeaveStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "dayoff")
    private DayOffType dayOffType;

    @Column(name = "reporting_manger")
    private String reportingManager;

    @Column(name = "max_days")
    private int maxDays;

    @Column(name = "total_applied_days")
    private int totalAppliedDays;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "remaining_days")
    private Integer remainingDays;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "applied_date")
    private LocalDateTime appliedDate;

    @Column(name = "document_path")
    private String documentPath;

    @Column(name = "document_name")
    private String documentName;

    private LocalDateTime documentUploadedAt;



//    @Column(name = "approvalComment")
//    private String approvalComment;
//
//    @Column(name = "approveBy")
//    private String approvedBy;
//
//    @Column(name = "approvalTime")
//    private LocalDateTime approvalTimestamp;



//    @Column(name = "total_applied")
//    private Integer totalApplied;
//
//    @Column(name = "approved_count")
//    private Integer approvedCount;
//
//    @Column(name = "rejected_count")
//    private Integer rejectedCount;
//
//    @Column(name = "pending_count")
//    private Integer pendingCount;
//
//    @Column(name = "cancelled_count")
//    private Integer cancelledCount;


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
