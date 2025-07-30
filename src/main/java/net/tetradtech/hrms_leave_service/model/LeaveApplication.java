
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

    @Column(name = "leaveid")
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


    @Column(name = "remaining_days")
    private Integer remainingDays;

    @Column(name = "document_path")
    private String documentPath;

    @Column(name = "document_name")
    private String documentName;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] documentData;



    @Column(name = "comment")
    private String comment;

    @Column(name = "approveId")
    private String approvedId;




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



    // Audit fields
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Long updatedBy;

    // Soft delete fields
    @Column(name = "isDeleted")
    private boolean isDeleted;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;

    @Column(name = "deletedBy")
    private String deletedBy;


}
