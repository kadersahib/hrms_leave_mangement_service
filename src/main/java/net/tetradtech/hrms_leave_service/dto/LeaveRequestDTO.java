package net.tetradtech.hrms_leave_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestDTO {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "leaveId is required")
    private Long leaveId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private Long reportingId;

    @NotNull(message = "Leave day type is required")
    private String DayOffType;
}
