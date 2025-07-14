package net.tetradtech.hrms_leave_service.controller;

import net.tetradtech.hrms_leave_service.dto.LeaveSummaryReportDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<LeaveSummaryReportDTO>>> getSummaryByUser(@PathVariable Long userId) {
        List<LeaveSummaryReportDTO> summary = leaveSummaryService.getSummaryByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("success", "Leave summary fetched successfully", summary));
    }

    @GetMapping("/user/all")
    public ResponseEntity<ApiResponse<List<LeaveSummaryReportDTO>>> getSummaryForAllUsers() {
        List<LeaveSummaryReportDTO> data = leaveSummaryService.getSummaryForAllUsers();
        return ResponseEntity.ok(new ApiResponse<>("success", "Leave summary for all users", data));
    }

}