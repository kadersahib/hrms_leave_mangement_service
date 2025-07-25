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
    private Long leaveId;
    private Integer remainingDays;
    private int totalAppliedDays;

}
