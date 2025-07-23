//package net.tetradtech.hrms_leave_service.controller;
//
//
//import net.tetradtech.hrms_leave_service.dto.LeaveHistoryDTO;
//import net.tetradtech.hrms_leave_service.response.ApiResponse;
//import net.tetradtech.hrms_leave_service.service.LeaveHistoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/leave-history")
//public class LeaveHistoryController {
//
//    @Autowired
//    private LeaveHistoryService leaveHistoryService;
//
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<ApiResponse<List<LeaveHistoryDTO>>> getByUserId(@PathVariable Long userId) {
//        List<LeaveHistoryDTO> history = leaveHistoryService.getLeaveHistoryByUserId(userId);
//        return ResponseEntity.ok(new ApiResponse<>("success", "Leave history fetched successfully", history));
//    }
//
//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<List<LeaveHistoryDTO>>> getAllUsersHistory() {
//        List<LeaveHistoryDTO> history = leaveHistoryService.getAllUsersLeaveHistory();
//        return ResponseEntity.ok(new ApiResponse<>("success", "All user leave history fetched successfully", history));
//    }
//}
