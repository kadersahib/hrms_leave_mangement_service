package net.tetradtech.hrms_leave_service.controller;


import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/leaveDocument")
public class LeaveDocumentController {

    @Autowired
    private LeaveDocumentService leaveDocumentService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<String>> applyLeave(@RequestBody LeaveApplication leaveApplication) {
        leaveDocumentService.applyLeave(
                leaveApplication.getUserId(),
                leaveApplication.getLeaveTypeName(),
                leaveApplication.getStartDate(),
                leaveApplication.getEndDate(),
                null // No file passed in this case
        );
        return ResponseEntity.ok(new ApiResponse<>("success", "Leave applied successfully", null));
    }


}
