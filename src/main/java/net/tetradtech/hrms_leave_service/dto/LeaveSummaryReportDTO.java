package net.tetradtech.hrms_leave_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveSummaryReportDTO {
    private Long userId;
    private Long leaveTypeId;
    private String leaveTypeName;
    private int totalApplied;
    private int approvedCount;
    private int rejectedCount;
    private int pendingCount;
    private int cancelledCount;
}
