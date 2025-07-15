package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceSummaryDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
        try {
            AttendanceDTO result = attendanceService.clockIn(userId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Clock-in recorded", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PostMapping("/clock-out/{userId}")
    public ResponseEntity<ApiResponse<AttendanceDTO>> clockOut(@PathVariable Long userId) {
        try {
            AttendanceDTO result = attendanceService.clockOut(userId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Clock-out recorded", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/daily-logs")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAllUserDailyLogs(@RequestParam String date) {
        try {
            List<AttendanceDTO> result = attendanceService.getAllUserDailyLogs(LocalDate.parse(date));
            return ResponseEntity.ok(new ApiResponse<>("success", "Daily logs fetched", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/user-log")
    public ResponseEntity<ApiResponse<AttendanceDTO>> getUserLog(
            @RequestParam Long userId,
            @RequestParam String date) {
        try {
            AttendanceDTO result = attendanceService.getUserDailyLog(userId, LocalDate.parse(date));
            return ResponseEntity.ok(new ApiResponse<>("success", "User log fetched", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }


    @GetMapping("/calendar")
    public ResponseEntity<List<AttendanceDTO>> getUserCalendar(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {

        List<AttendanceDTO> calendar = attendanceService.getMonthlyCalendar(userId, year, month);
        return ResponseEntity.ok(calendar);
    }

}
