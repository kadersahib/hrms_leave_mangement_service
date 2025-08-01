package net.tetradtech.hrms_leave_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveMatAndPatDTO {
    private Long id;
    @NotNull(message = "userId is required")
    private Long userId;
    @NotNull(message = "LeaveType is required")
    private Long leaveTypeId;
    @NotNull(message = "dayOffType is required")
    private String dayOffType;
    @NotNull(message = "reportingID is required")
    private Long reportingId;
    @NotNull(message = "date is required")
    private LocalDate startDate;
    @NotNull(message = "date is required")
    private LocalDate endDate;
    private String documentName;

}
