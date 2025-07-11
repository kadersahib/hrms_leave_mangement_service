package net.tetradtech.hrms_leave_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceDTO {
    private Long leaveTypeId;
    private String leaveTypeName;
    private int maxDays;
    private long usedDays;
    private long remainingDays;
}
