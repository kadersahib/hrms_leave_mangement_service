package net.tetradtech.hrms_leave_service.controller;

import jakarta.validation.Valid;
import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-approvals")
public class LeaveApprovalController {

    @Autowired
    private LeaveApprovalService leaveApprovalService;


    @PostMapping("/{leaveId}")
    public ResponseEntity<ApiResponse<LeaveApplication>> approveLeave(
            @PathVariable Long leaveId,
            @Valid @RequestBody LeaveApprovalDTO dto) {
        try {
            LeaveApplication result = leaveApprovalService.performAction(leaveId, dto);
            return ResponseEntity.ok(new ApiResponse<>("success", "Leave action processed", result));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "Something went wrong", null));
        }
    }


    @PutMapping("/{leaveId}")
    public ResponseEntity<ApiResponse<LeaveApplication>> updateApproval(
            @PathVariable Long leaveId,
            @Valid @RequestBody LeaveApprovalDTO dto) {
        try {
            LeaveApplication updated = leaveApprovalService.updateApproval(leaveId, dto);
            return ResponseEntity.ok(new ApiResponse<>("success", "Approval updated", updated));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "Something went wrong", null));
        }
    }



    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>("success", "Fetched all approvals", leaveApprovalService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveApplication>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("success", "Fetched approval", leaveApprovalService.getById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        leaveApprovalService.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>("success", "Deleted successfully", "Deleted ID: " + id));
    }
}
