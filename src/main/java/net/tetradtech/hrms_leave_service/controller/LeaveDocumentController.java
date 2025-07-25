package net.tetradtech.hrms_leave_service.controller;


import net.tetradtech.hrms_leave_service.dto.LeaveDocDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.LeaveDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/leaveDocument")
public class LeaveDocumentController {

    @Autowired
    private LeaveDocumentService leaveDocumentService;


    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LeaveDocDTO>> applyLeave(
            @RequestParam Long userId,
            @RequestParam Long leaveTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestPart(required = false) MultipartFile file
    ) throws IOException {
        LeaveApplication leave = leaveDocumentService.applyLeave(userId, leaveTypeId, startDate, endDate, file);

        LeaveDocDTO dto = new LeaveDocDTO();
        dto.setUserId(leave.getUserId());
        dto.setLeaveTypeId(leave.getLeaveTypeId());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setDocumentName(leave.getDocumentName());

        return ResponseEntity.ok(new ApiResponse<>("success", "Leave applied successfully", dto));
    }


}
