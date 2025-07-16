package net.tetradtech.hrms_leave_service.controller;


import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-balance")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;


    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserLeaveBalance(@PathVariable Long userId) {
        List<LeaveBalanceDTO> balances = leaveBalanceService.getLeaveBalanceByUser(userId);

        ApiResponse<?> response = new ApiResponse<>(
                "success",
                "Leave balances fetched successfully",
                Map.of("userId", userId, "balance", balances)
        );

        return ResponseEntity.ok(response);
    }

    // GET: /user/{userId}/type/{leaveTypeId}
    @GetMapping("/user/{userId}/type/{leaveTypeId}")
    public ResponseEntity<ApiResponse<?>> getUserLeaveBalanceByType(
            @PathVariable Long userId,
            @PathVariable Long leaveTypeId
    ) {
        LeaveBalanceDTO balance = leaveBalanceService.getLeaveBalanceByUserAndType(userId, leaveTypeId);

        ApiResponse<?> response = new ApiResponse<>(
                "success",
                "Leave balance for the given type fetched successfully",
                Map.of("userId", userId, "balance", balance)
        );

        return ResponseEntity.ok(response);
    }
}
