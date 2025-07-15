package net.tetradtech.hrms_leave_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceCalendarDTO {
    private LocalDate date;
    private String status; // PRESENT, ABSENT, LEAVE, LATE, WEEKEND
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Boolean isToday = false;
}
