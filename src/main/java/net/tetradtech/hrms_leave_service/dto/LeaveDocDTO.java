package net.tetradtech.hrms_leave_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveDocDTO {

    private Long userId;
    private Long leaveTypeId;
    private String dayOffType;
    private Long reportingId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String documentName;
}
