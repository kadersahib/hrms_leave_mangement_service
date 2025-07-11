package net.tetradtech.hrms_leave_service.controller;


import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveApplicationStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/filter")
public class LeaveServiceController {

    @Autowired
    private LeaveApplicationStatusService leaveApplicationFilterService;

    // Filter by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getByStatus(@PathVariable String status) {
        try {
            List<LeaveApplication> leaves = leaveApplicationFilterService.filterByStatus(status);
            return ResponseEntity.ok(new ApiResponse<>("success", "Filtered by status", leaves));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    //  Filter by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getByUserId(@PathVariable Long userId) {
        try {
            List<LeaveApplication> leaves = leaveApplicationFilterService.filterByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Filtered by userId", leaves));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    // Filter by User ID + Status
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        try {
            List<LeaveApplication> leaves = leaveApplicationFilterService.filterByUserIdAndStatus(userId, status);
            return ResponseEntity.ok(new ApiResponse<>("success", "Filtered by userId and status", leaves));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    // Filter by leave type ID
    @GetMapping("/leave-type/{leaveTypeId}")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getByLeaveType(@PathVariable Long leaveTypeId) {
        try {
            List<LeaveApplication> leaves = leaveApplicationFilterService.filterByLeaveType(leaveTypeId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Filtered by leave type", leaves));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @GetMapping("/user-date")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> filterByUserAndDateRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<LeaveApplication> leaves = leaveApplicationFilterService
                    .filterByDateRange(userId, startDate, endDate);

            return ResponseEntity.ok(new ApiResponse<>("success", "Filtered by user and date", leaves));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }



}

