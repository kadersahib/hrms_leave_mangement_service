package net.tetradtech.hrms_leave_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveUpdateDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reportingManager;
}
