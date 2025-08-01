package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/leaveApproval")
public class LeaveApprovalController {

    @Autowired
    private LeaveApprovalService leaveApprovalService;

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveApplication>> approveOrRejectLeave(
            @PathVariable Long id,
            @RequestBody LeaveApprovalDTO request) {

        LeaveApplication updatedLeave = leaveApprovalService.approveOrRejectLeave(id, request);

        ApiResponse<LeaveApplication> response = new ApiResponse<>(
                "success",
                "Leave " + request.getAction() + " successfully",
                updatedLeave
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveApplication>> toggleLeave(
            @PathVariable Long id,
            @RequestBody LeaveApprovalDTO request) {

        LeaveApplication updatedLeave = leaveApprovalService.changeLeaveStatus(
                id,
                request.getApproverId(),
                request.getApproverComment()
        );

        ApiResponse<LeaveApplication> response = new ApiResponse<>(
                "success",
                "Leave status toggled successfully",
                updatedLeave
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getAllApprovals() {
        List<LeaveApplication> approvals = leaveApprovalService.getAllApprovals();
        ApiResponse<List<LeaveApplication>> response = new ApiResponse<>(
                "success", "All leave approvals fetched", approvals);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getLeavesByUserId(@PathVariable Long userId) {
        List<LeaveApplication> leaves = leaveApprovalService.getByUserId(userId);
        if (leaves.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>("error", "No leaves found for this user", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("success", "Leaves fetched successfully", leaves));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteApprovalById(@PathVariable Long id) {
        leaveApprovalService.deleteApprovalById(id);
        ApiResponse<String> response = new ApiResponse<>(
                "success", "Leave approval deleted successfully", null);
        return ResponseEntity.ok(response);
    }


}
