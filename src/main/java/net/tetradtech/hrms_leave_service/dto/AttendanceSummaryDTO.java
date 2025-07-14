package net.tetradtech.hrms_leave_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceSummaryDTO {
    private Long userId;
    private int totalPresent;
    private int totalAbsent;
    private int totalLate;
}
