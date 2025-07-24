package net.tetradtech.hrms_leave_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface LeaveDocumentService {
    void applyLeave(
            Long userId,
            String leaveTypeName,
            LocalDate startDate,
            LocalDate endDate,
            MultipartFile file
    );
}
