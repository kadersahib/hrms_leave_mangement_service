package net.tetradtech.hrms_leave_service.controller;


import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
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
    public ResponseEntity<?> getUserLeaveBalance(@PathVariable Long userId) {
        List<LeaveBalanceDTO> balances = leaveBalanceService.getLeaveBalanceByUser(userId);

        return ResponseEntity.ok(Map.of("balance", balances,"userId", userId ));

    }


    @GetMapping("/user/{userId}/type/{leaveTypeId}")
    public ResponseEntity<?> getUserLeaveBalanceByType(
            @PathVariable Long userId,
            @PathVariable Long leaveTypeId
    ) {
        LeaveBalanceDTO balance = leaveBalanceService.getLeaveBalanceByUserAndType(userId, leaveTypeId);
        return ResponseEntity.ok(Map.of("balance", balance,"userId", userId ));


    }
}
