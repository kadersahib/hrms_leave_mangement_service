package net.tetradtech.hrms_leave_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveHistoryDTO {
    private Long id;
    private Long userId;
    private String name;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int appliedDays;
    private String status;
    private String reportingManager;
    private String approvalComment;
    private String approvedBy;
    private LocalDateTime approvalTimestamp;
    private LocalDateTime createdAt;


}
