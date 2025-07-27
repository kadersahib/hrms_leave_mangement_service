package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface MaternityAndPaternityLeaveService {
    LeaveApplication applyLeave(
            Long userId, Long leaveTypeId, DayOffType dayOffType,
            Long reportingId, LocalDate startDate, LocalDate endDate,
            MultipartFile file
    );

    LeaveApplication updateLeave(
            Long leaveId, Long userId, Long leaveTypeId,
            DayOffType dayOffType, Long reportingId,
            LocalDate startDate, LocalDate endDate, MultipartFile file);



}
