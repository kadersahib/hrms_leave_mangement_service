package net.tetradtech.hrms_leave_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceDTO {
    private Long userId;
    private Long leaveId;
    private String leaveTypeName;
    private int totalApplied;
    private int remainingDays;
}
