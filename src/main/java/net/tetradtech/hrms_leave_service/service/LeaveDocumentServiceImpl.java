package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.constants.LeaveTypeConstants;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class LeaveDocumentServiceImpl implements LeaveDocumentService {

    @Autowired
    private LeaveApplicationRepository leaveRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public LeaveApplication applyLeave(Long userId, Long leaveTypeId, LocalDate startDate, LocalDate endDate, MultipartFile file) {
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

        // Generate virtual download path using the saved ID
        saved.setDocumentPath("/api/leaveDocument/download/" + saved.getId());
        return leaveRepository.save(saved); // Save again with documentPath

    }
}

