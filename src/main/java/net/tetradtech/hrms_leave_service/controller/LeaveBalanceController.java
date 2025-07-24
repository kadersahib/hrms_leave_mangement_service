package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-balance")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDTO>>> getAllLeaveBalances() {
        List<LeaveBalanceDTO> data = leaveBalanceService.getAllLeaves();

        ApiResponse<List<LeaveBalanceDTO>> response = new ApiResponse<>(
                "success",
                "All leave balances fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDTO>>> getLeaveBalancesByUserId(@PathVariable Long userId) {
        List<LeaveBalanceDTO> data = leaveBalanceService.getLeavesByUserId(userId);

        ApiResponse<List<LeaveBalanceDTO>> response = new ApiResponse<>(
                "success",
                "Leave balance for user ID " + userId + " fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveBalanceDTO>>> getLeaveBalance(
            @RequestParam Long userId,
            @RequestParam String leaveTypeName) {

        List<LeaveBalanceDTO> balance = leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(userId, leaveTypeName);

        if (balance.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>("error", "No leave balance found for userId " + userId + " and type " + leaveTypeName, null)
            );
        }
        return ResponseEntity.ok(new ApiResponse<>("success", "Leave balance fetched successfully", balance));
    }


}
