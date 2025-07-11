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

@RestController
@RequestMapping("/api/leave-balance")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<LeaveBalanceDTO>> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(leaveBalanceService.getLeaveBalanceByUser(userId));
    }
}
