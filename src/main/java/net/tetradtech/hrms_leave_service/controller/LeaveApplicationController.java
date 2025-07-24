
package net.tetradtech.hrms_leave_service.controller;

import jakarta.validation.Valid;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveUpdateRequestDTO;
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
            @Valid @RequestBody LeaveUpdateRequestDTO updatedData) {
        try {
            if (updatedData.getId() == null) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>("error", "User ID is required in the request body", null)
                );
            }
            LeaveApplication updated = leaveApplicationService.updateLeave(updatedData.getId(), updatedData);
            int remainingDays = updated.getMaxDays() - updated.getTotalAppliedDays();
            String message = "Leave updated successfully. Remaining days: " + remainingDays;

            return ResponseEntity.ok(new ApiResponse<>("success", message, updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getAll() {
        List<LeaveApplication> leaves = leaveApplicationService.getAllLeaves();
        return ResponseEntity.ok(new ApiResponse<>("success", "All leaves fetched", leaves));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getByUser(@PathVariable Long id) {
        List<LeaveApplication> leaves = leaveApplicationService.getLeavesByUserId(id);
        if (leaves.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse<>("error", "No UserId  found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("success", "Leaves fetched", leaves));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteLatest(
            @PathVariable Long id
    ) {
        try {
            leaveApplicationService.deleteLatestLeaveByUserId(id);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave deleted (soft)", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<LeaveApplication>> cancelLeave(@PathVariable Long id) {
        try {
            LeaveApplication cancelled = leaveApplicationService.cancelLeave(id);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave cancelled successfully", cancelled));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }


}
