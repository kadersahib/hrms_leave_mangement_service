
package net.tetradtech.hrms_leave_service.controller;

import jakarta.validation.Valid;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveApplicationController {

    @Autowired
    private LeaveApplicationService leaveApplicationService;


    @PostMapping
    public ResponseEntity<ApiResponse<LeaveApplication>> apply(
            @Valid @RequestBody LeaveRequestDTO application) {
        try {
            LeaveApplication saved = leaveApplicationService.applyLeave(application);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave applied successfully", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<LeaveApplication>> updateLeave(
            @Valid @RequestBody LeaveRequestDTO updatedData) {
        try {
            if (updatedData.getUserId() == null) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>("error", "User ID is required in the request body", null)
                );
            }
            LeaveApplication updated = leaveApplicationService.updateLeave(updatedData.getUserId(), updatedData);
            int remainingDays = updated.getMaxDays() - updated.getAppliedDays();
            String message = "Leave updated successfully. Remaining days: " + remainingDays;

            return ResponseEntity.ok(new ApiResponse<>("success", message, updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }


    @GetMapping("/all")    //active data only
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getAll() {
        List<LeaveApplication> leaves = leaveApplicationService.getAllLeaves();
        return ResponseEntity.ok(new ApiResponse<>("success", "All leaves fetched", leaves));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getByUser(@PathVariable Long userId) {
        List<LeaveApplication> leaves = leaveApplicationService.getLeavesByUserId(userId);
        if (leaves.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse<>("error", "No UserId  found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("success", "Leaves fetched", leaves));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteLatest(
            @PathVariable Long userId
    ) {
        try {
            leaveApplicationService.deleteLatestLeaveByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave deleted (soft)", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PostMapping("/cancel/{userId}")
    public ResponseEntity<ApiResponse<LeaveApplication>> cancelLeave(@PathVariable Long userId) {
        try {
            LeaveApplication cancelled = leaveApplicationService.cancelLeave(userId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave cancelled successfully", cancelled));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }


}
