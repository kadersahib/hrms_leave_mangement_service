package net.tetradtech.hrms_leave_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceDTO {
    private String name;
    private Long userId;
    private String leaveTypeName;
    private int maxDays;
    private int remainingDays;
    private int totalAppliedDays;
    private int totalCount;


}
