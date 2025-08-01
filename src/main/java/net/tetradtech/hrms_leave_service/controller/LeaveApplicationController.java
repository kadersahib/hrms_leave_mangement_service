
package net.tetradtech.hrms_leave_service.controller;

import jakarta.validation.Valid;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveUpdateRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<LeaveApplication>> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveUpdateRequestDTO updatedData) {

        try {
            LeaveApplication updatedLeave = leaveApplicationService.updateLeave(id, updatedData);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave updated successfully", updatedLeave));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "An unexpected error occurred", null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getAll() {
        List<LeaveApplication> leaves = leaveApplicationService.getAllLeaves();
        return ResponseEntity.ok(new ApiResponse<>("success", "All leaves fetched", leaves));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveApplication>> getLeaveById(@PathVariable Long id) {
        try {
            LeaveApplication leave = leaveApplicationService.getLeaveById(id);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave fetched", leave));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id
    ) {
        try {
            leaveApplicationService.deleteById(id);
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
