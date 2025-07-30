package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/leaves-approval")
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

}
