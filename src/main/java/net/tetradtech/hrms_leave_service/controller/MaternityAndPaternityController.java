package net.tetradtech.hrms_leave_service.controller;

import jakarta.validation.Valid;
import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.dto.LeaveDocDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import net.tetradtech.hrms_leave_service.service.MaternityAndPaternityLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/leaveDocument")
public class MaternityAndPaternityController {

    @Autowired
    private MaternityAndPaternityLeaveService leaveDocumentService;

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;


    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LeaveDocDTO>> applyLeave(
            @Valid
            @RequestParam Long userId,
            @RequestParam Long leaveTypeId,
            @RequestParam String dayOffType,
            @RequestParam Long reportingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestPart(required = false) MultipartFile file
    ) throws IOException {
        DayOffType dayOff = DayOffType.fromString(dayOffType);
        LeaveApplication leave = leaveDocumentService.applyLeave(userId, leaveTypeId,dayOff,reportingId, startDate, endDate, file);

        LeaveDocDTO dto = new LeaveDocDTO();
        dto.setUserId(leave.getUserId());
        dto.setLeaveTypeId(leave.getLeaveTypeId());
        dto.setDayOffType(leave.getDayOffType().name());
        dto.setReportingId(leave.getReportingId());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setDocumentName(leave.getDocumentName());

        return ResponseEntity.ok(new ApiResponse<>("success", "Leave applied successfully", dto));
    }
    @PutMapping("/apply/{leaveId}")
    public ResponseEntity<ApiResponse<LeaveDocDTO>> updateLeave(
            @PathVariable Long leaveId,
            @RequestParam Long userId,
            @RequestParam String dayOffType,
            @RequestParam Long leaveTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Long reportingId,
            @RequestPart(required = false) MultipartFile file
    ) throws IOException {
        DayOffType dayOff = DayOffType.fromString(dayOffType);

        LeaveApplication leave = leaveDocumentService.updateLeave(leaveId, userId, leaveTypeId, dayOff, reportingId, startDate, endDate, file);

        LeaveDocDTO dto = new LeaveDocDTO();
        dto.setUserId(leave.getUserId());
        dto.setLeaveTypeId(leave.getLeaveTypeId());
        dto.setDayOffType(leave.getDayOffType().name());
        dto.setReportingId(leave.getReportingId());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setDocumentName(leave.getDocumentName());

        return ResponseEntity.ok(new ApiResponse<>("success", "Leave updated successfully", dto));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        LeaveApplication leave = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found for ID: " + id));

        if (leave.getDocumentData() == null) {
            throw new RuntimeException("No document attached for this leave.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename(leave.getDocumentName())
                .build());

        return new ResponseEntity<>(leave.getDocumentData(), headers, HttpStatus.OK);
    }



}
