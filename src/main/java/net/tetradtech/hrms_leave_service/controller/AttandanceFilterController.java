package net.tetradtech.hrms_leave_service.controller;


import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceStatsDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.AttandanceFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/attendance")
public class AttandanceFilterController {

    @Autowired
    private  AttandanceFilterService attandanceFilterService;

    @GetMapping("/designation/{designation}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getByDesignation(@PathVariable String designation) {
        try {
            List<AttendanceDTO> attendances = attandanceFilterService.getFilterByDesignation(designation);
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Attendance records fetched for designation containing: " + designation, attendances)
            );
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>("error", ex.getReason(), null)
            );
        }
    }


    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getByDesignationAndDateRange(
            @RequestParam String designation,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<AttendanceDTO> attendances = attandanceFilterService.getFilterByDesignationAndDateRange(designation, startDate, endDate);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Attendance fetched for designation and date range", attendances)
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<List<AttendanceStatsDTO>>> getAllUserStats() {
        List<AttendanceStatsDTO> stats = attandanceFilterService.getAllUserAttendanceStats();
        return ResponseEntity.ok(new ApiResponse<>("success", "Total attendance stats fetched", stats));
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<ApiResponse<AttendanceStatsDTO>> getUserStats(@PathVariable Long userId) {
        AttendanceStatsDTO stats = attandanceFilterService.getUserAttendanceStats(userId);
        return ResponseEntity.ok(new ApiResponse<>("success", "PerUserId attendance stats fetched", stats));
    }
}
