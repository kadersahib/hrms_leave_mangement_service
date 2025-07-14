package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceSummaryDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in/{userId}")
    public ResponseEntity<ApiResponse<AttendanceDTO>> clockIn(@PathVariable Long userId) {
        AttendanceDTO dto = attendanceService.clockIn(userId);
        return ResponseEntity.ok(new ApiResponse<>("success", "Clock-in successful", dto));
    }

    @PostMapping("/clock-out/{userId}")
    public ResponseEntity<ApiResponse<AttendanceDTO>> clockOut(@PathVariable Long userId) {
        AttendanceDTO dto = attendanceService.clockOut(userId);
        return ResponseEntity.ok(new ApiResponse<>("success", "Clock-out successful", dto));
    }

    @PostMapping("/mark-absent/{userId}/{date}")
    public ResponseEntity<ApiResponse<String>> markAbsentForUser(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        attendanceService.markAbsentForUser(userId, date);
        return ResponseEntity.ok(new ApiResponse<>("success", "Marked user " + userId + " as absent on " + date, null));
    }

    @GetMapping("/summary/userId/{userId}/{year}/{month}")
    public ResponseEntity<ApiResponse<AttendanceSummaryDTO>> summary(
            @PathVariable Long userId,
            @PathVariable int year,
            @PathVariable int month) {

        AttendanceSummaryDTO summary = attendanceService.getMonthlySummary(userId, year, month);
        return ResponseEntity.ok(new ApiResponse<>("success", "Attendance summary fetched", summary));
    }

    @GetMapping("/summary/all/{year}/{month}")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryDTO>>> getAllUserMonthlySummary(
            @PathVariable int year,
            @PathVariable int month) {

        List<AttendanceSummaryDTO> summaries = attendanceService.getMonthlySummaryForAllUsers(year, month);
        return ResponseEntity.ok(new ApiResponse<>("success", "Monthly summary for all users fetched", summaries));
    }


    @GetMapping("/daily/{userId}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> logs(@PathVariable Long userId) {
        List<AttendanceDTO> logs = attendanceService.getDailyLogs(userId);
        return ResponseEntity.ok(new ApiResponse<>("success", "Daily logs fetched", logs));
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAllDailyLogs() {
        List<AttendanceDTO> logs = attendanceService.getAllDailyLogs();
        return ResponseEntity.ok(new ApiResponse<>("success", "All user logs fetched", logs));
    }

}
