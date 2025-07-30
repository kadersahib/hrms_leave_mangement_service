package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-balance")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllLeaveBalances() {
        List<Map<String, Object>> data = leaveBalanceService.getAllLeaves();

        ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                "success",
                "All leave balances fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLeavesByUserId(@PathVariable Long userId) {
        List<Map<String, Object>> data = leaveBalanceService.getLeavesByUserId(userId);

        ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                "success",
                "Leave balances fetched successfully",
                data
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping("/user/{userId}/leaveTypeId/{leaveTypeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLeaveBalanceByUserIdAndLeaveType(
            @PathVariable Long userId,
            @PathVariable Long leaveTypeId) {

        Map<String, Object> data = leaveBalanceService.getLeaveBalanceByUserIdAndLeaveType(userId, leaveTypeId);

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                "success",
                "Leave balance fetched successfully",
                data
        );

        return ResponseEntity.ok(response);
    }




}
