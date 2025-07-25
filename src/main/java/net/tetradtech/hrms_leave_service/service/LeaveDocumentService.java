package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface LeaveDocumentService {
    LeaveApplication applyLeave(
            Long userId,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            MultipartFile file
    );
}
