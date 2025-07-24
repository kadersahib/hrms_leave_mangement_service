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
    public void applyLeave(Long userId, String leaveTypeName, LocalDate startDate, LocalDate endDate, MultipartFile file) {
        // 1. Validate user
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }


        // 2. Validate gender vs leave type
        if (LeaveTypeConstants.MATERNITY.equalsIgnoreCase(leaveTypeName) && !"FEMALE".equalsIgnoreCase(user.getGender())) {
            throw new IllegalArgumentException("Maternity leave only applicable to female users.");
        }

        if (LeaveTypeConstants.PATERNITY.equalsIgnoreCase(leaveTypeName) && !"MALE".equalsIgnoreCase(user.getGender())) {
            throw new IllegalArgumentException("Paternity leave only applicable to male users.");
        }

        // 3. Calculate duration
        long leaveDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (LeaveTypeConstants.MATERNITY.equalsIgnoreCase(leaveTypeName) && leaveDays > LeaveTypeConstants.MAX_MATERNITY_DAYS) {
            throw new IllegalArgumentException("Maternity leave cannot exceed 80 days.");
        }

        if (LeaveTypeConstants.PATERNITY.equalsIgnoreCase(leaveTypeName) && leaveDays > LeaveTypeConstants.MAX_PATERNITY_DAYS) {
            throw new IllegalArgumentException("Paternity leave cannot exceed 20 days.");
        }

        if (!LeaveTypeConstants.MATERNITY.equalsIgnoreCase(leaveTypeName) && !LeaveTypeConstants.PATERNITY.equalsIgnoreCase(leaveTypeName)) {
            throw new IllegalArgumentException("Invalid leave type: " + leaveTypeName);
        }


        // 4. Save file (if provided)
        String filePath = null;
        String fileName = null;
        LocalDateTime uploadedAt = null;

        if (file != null && !file.isEmpty()) {
            fileName = file.getOriginalFilename();
            filePath = fileStorageService.saveFile(file);
            uploadedAt = LocalDateTime.now();
        }

        // 5. Calculate applied days
        int appliedDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 6. Save LeaveApplication
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(userId);
        leave.setLeaveTypeName(leaveTypeName);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setAppliedDate(LocalDateTime.now());
        leave.setStatus(LeaveStatus.PENDING); // default
        leave.setTotalAppliedDays(appliedDays);
        leave.setDocumentPath(filePath);
        leave.setDocumentName(fileName);
        leave.setDocumentUploadedAt(uploadedAt);
        leave.setActive(true);

        leaveRepository.save(leave);
    }
}

