
package net.tetradtech.hrms_leave_service.controller;

import jakarta.validation.Valid;
import net.tetradtech.hrms_leave_service.dto.LeaveCancelDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveUpdateDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/leaves")
public class LeaveApplicationController {

    @Autowired
    private LeaveApplicationService leaveApplicationService;


    @PostMapping
    public ResponseEntity<ApiResponse<LeaveApplication>> apply(
            @Valid @RequestBody LeaveApplication application) {
        try {
            LeaveApplication saved = leaveApplicationService.applyLeave(application);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave applied successfully", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<LeaveApplication>> updateLatest(
            @PathVariable Long userId,
            @Valid @RequestBody LeaveUpdateDTO updatedData
    ) {
        try {
            Optional<LeaveApplication> latestOpt = leaveApplicationService.getUpdateByUserId(userId);
            if (latestOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>("error", "No recent leave found for user ID: " + userId, null));
            }

            LeaveApplication existingLeave = latestOpt.get();

            existingLeave.setStartDate(updatedData.getStartDate());
            existingLeave.setEndDate(updatedData.getEndDate());
            existingLeave.setReportingManager(updatedData.getReportingManager());

            existingLeave.setUserId(userId);

            LeaveApplication updated = leaveApplicationService.updateLeave(existingLeave.getId(), existingLeave);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave updated successfully", updated));

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

    @PostMapping ("/cancel")
    public ResponseEntity<ApiResponse<LeaveApplication>> cancelLeave(@RequestBody LeaveCancelDTO dto) {
        try {
            LeaveApplication cancelled = leaveApplicationService.cancelLeave(dto);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave cancelled successfully", cancelled));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }




}
