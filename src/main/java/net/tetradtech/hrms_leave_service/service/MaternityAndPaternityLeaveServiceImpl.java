package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.constants.LeaveTypeConstants;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class MaternityAndPaternityLeaveServiceImpl implements MaternityAndPaternityLeaveService {

    @Autowired
    private LeaveApplicationRepository leaveRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public LeaveApplication applyLeave(Long userId, Long leaveTypeId,DayOffType dayOffType,Long reportingId, LocalDate startDate, LocalDate endDate, MultipartFile file) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }

        String leaveTypeName;

        if (leaveTypeId == 3L) {
            leaveTypeName = LeaveTypeConstants.MATERNITY;
            if (!"FEMALE".equalsIgnoreCase(user.getGender())) {
                throw new IllegalArgumentException("Maternity leave only applicable to female users.");
            }
        } else if (leaveTypeId == 4L) {
            leaveTypeName = LeaveTypeConstants.PATERNITY;
            if (!"MALE".equalsIgnoreCase(user.getGender())) {
                throw new IllegalArgumentException("Paternity leave only applicable to male users.");
            }
        } else {
            throw new IllegalArgumentException("Invalid leaveTypeId: " + leaveTypeId);
        }

        int leaveYear = startDate.getYear();
        boolean alreadyApplied = leaveRepository.existsByUserIdAndLeaveTypeIdAndStartDateBetween(
                userId, leaveTypeId,
                LocalDate.of(leaveYear, 1, 1),
                LocalDate.of(leaveYear, 12, 31)
        );

        if (alreadyApplied) {
            throw new IllegalArgumentException(leaveTypeName + " leave already applied for year " + leaveYear);
        }

        long leaveDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (leaveTypeId == 3L && leaveDays > LeaveTypeConstants.MAX_MATERNITY_DAYS) {
            throw new IllegalArgumentException("Maternity leave cannot exceed 100 days.");
        }
        if (leaveTypeId == 4L && leaveDays > LeaveTypeConstants.MAX_PATERNITY_DAYS) {
            throw new IllegalArgumentException("Paternity leave cannot exceed 20 days.");
        }

        // Save document
        String documentName = null;
        byte[] documentData = null;
        if (file != null && !file.isEmpty()) {
            documentName = file.getOriginalFilename();
            try {
                documentData = file.getBytes();  // Save bytes to DB
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file", e);
            }
        }

        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(userId);
        leave.setLeaveTypeId(leaveTypeId);
        leave.setReportingId(reportingId);
        leave.setDayOffType(DayOffType.LEAVE);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setAppliedDays((int) leaveDays);
        leave.setTotalAppliedDays((int) leaveDays);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setDocumentName(documentName);
        leave.setDocumentData(documentData);
        leave.setCreatedAt(LocalDateTime.now());

        String userIdString = String.valueOf(leave.getUserId());
        leave.setCreatedBy(userIdString);

        LeaveApplication saved = leaveRepository.save(leave);

        saved.setDocumentPath("/api/leaveDocument/download/" + saved.getId());
        return leaveRepository.save(saved); // Save again with documentPath
    }


    @Override
    public LeaveApplication updateLeave(Long leaveId, Long userId, Long leaveTypeId,
                                        DayOffType dayOffType, Long reportingId,
                                        LocalDate startDate, LocalDate endDate, MultipartFile file) {
        LeaveApplication existing = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found with ID: " + leaveId));

        if (!LeaveStatus.PENDING.equals(existing.getStatus())) {
            throw new IllegalArgumentException("Only PENDING leaves can be updated.");
        }

        long leaveDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (leaveTypeId == 3L && leaveDays > LeaveTypeConstants.MAX_MATERNITY_DAYS) {
            throw new IllegalArgumentException("Maternity leave cannot exceed 100 days.");
        }
        if (leaveTypeId == 4L && leaveDays > LeaveTypeConstants.MAX_PATERNITY_DAYS) {
            throw new IllegalArgumentException("Paternity leave cannot exceed 20 days.");
        }

        int currentYear = LocalDate.now().getYear();
        if (existing.getStartDate().getYear() != currentYear) {
            throw new IllegalArgumentException("Only current year leaves can be updated.");
        }

        existing.setUserId(userId);
        existing.setLeaveTypeId(leaveTypeId);
        existing.setStartDate(startDate);
        existing.setEndDate(endDate);
        existing.setAppliedDays((int) leaveDays);
        existing.setTotalAppliedDays((int) leaveDays);
        existing.setReportingId(reportingId);
        existing.setDayOffType(DayOffType.LEAVE);

        if (file != null && !file.isEmpty()) {
            existing.setDocumentName(file.getOriginalFilename());
            try {
                existing.setDocumentData(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            existing.setDocumentPath("/api/leaveDocument/download/" + leaveId);
        }

        existing.setUpdatedAt(LocalDateTime.now());
        return leaveRepository.save(existing);
    }

}

