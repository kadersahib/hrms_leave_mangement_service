package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.LeaveSummaryReportDTO;
import net.tetradtech.hrms_leave_service.service.LeaveSummaryServiceReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leave-summary")
public class LeaveSummaryReportController {

    @Autowired
    private LeaveSummaryServiceReport leaveSummaryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveSummaryReportDTO>> getSummaryByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(leaveSummaryService.getSummaryByUser(userId));
    }
}