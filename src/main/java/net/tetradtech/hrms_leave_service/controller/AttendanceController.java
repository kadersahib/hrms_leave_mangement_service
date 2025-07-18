package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @GetMapping("/all")
    public ResponseEntity<List<AttendanceDTO>> getAllAttendanceRecords() {
        List<AttendanceDTO> records = attendanceService.getAllAttendanceRecords();
        return ResponseEntity.ok(records);
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

    @GetMapping("/daily-present")
    public ResponseEntity<ApiResponse<Integer>> getDailyPresentCount(@RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date.trim());
            int count = attendanceService.getDailyPresentCount(localDate);
            ApiResponse<Integer> response = new ApiResponse<>("success", "Present user count fetched", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Integer> errorResponse = new ApiResponse<>(
                    "error",
                    "Invalid date format or internal error",
                    null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteRecentByUserId(@PathVariable Long userId) {
        attendanceService.deleteRecentAttendanceByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>("success", "Recent attendance record deleted for user ID: " + userId, null));
    }

    @GetMapping("/by-designation/{designation}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getByDesignation(@PathVariable String designation) {
        List<AttendanceDTO> attendances = attendanceService.getAttendanceByDesignation(designation);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Attendance records fetched for designation containing: " + designation, attendances)
        );
    }


    @GetMapping("/by-designation-and-date")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getByDesignationAndDateRange(
            @RequestParam String designation,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<AttendanceDTO> attendances = attendanceService.getAttendanceByDesignationAndDateRange(designation, startDate, endDate);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Attendance fetched for designation and date range", attendances)
        );
    }





}
