package net.tetradtech.hrms_leave_service.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarAttendanceDTO {
    private Long userId;
    private String name;
    private LocalDate date;
    private String status;
    private Long leaveTypeId;
}
