package net.tetradtech.hrms_leave_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceStatsDTO {
    private Long userId;
    private long totalWorkingDays;
    private long onTimeDays;
    private long lateDays;
    private long weekendDaysWorked;

}
