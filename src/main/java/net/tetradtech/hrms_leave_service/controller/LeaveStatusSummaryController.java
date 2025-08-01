//package net.tetradtech.hrms_leave_service.controller;
//
//import net.tetradtech.hrms_leave_service.dto.LeaveSummaryReportDTO;
//import net.tetradtech.hrms_leave_service.response.ApiResponse;
//import net.tetradtech.hrms_leave_service.service.LeaveStatusSummary;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/leave-summary")
//public class LeaveStatusSummaryController {
//
//    @Autowired
//    private LeaveStatusSummary leaveSummaryServiceReport;
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<ApiResponse<LeaveSummaryReportDTO>> getSummaryByUser(@PathVariable Long userId) {
//        LeaveSummaryReportDTO summary = leaveSummaryServiceReport.getSummaryByUser(userId);
//        ApiResponse<LeaveSummaryReportDTO> response = new ApiResponse<>("success", "Leave summary fetched successfully", summary);
//        return ResponseEntity.ok(response);
//    }
//
//
//    @GetMapping("/user/all")
//    public ResponseEntity<ApiResponse<List<LeaveSummaryReportDTO>>> getSummaryForAllUsers() {
//        List<LeaveSummaryReportDTO> data = leaveSummaryServiceReport.getSummaryForAllUsers();
//        return ResponseEntity.ok(new ApiResponse<>("success", "Leave summary for all users", data));
//    }
//
//}