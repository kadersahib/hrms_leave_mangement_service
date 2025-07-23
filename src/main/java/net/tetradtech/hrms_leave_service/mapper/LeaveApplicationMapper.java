package net.tetradtech.hrms_leave_service.mapper;

import net.tetradtech.hrms_leave_service.Enum.DayOffType;
import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LeaveApplicationMapper {

    public LeaveApplication toNewLeaveApplication(LeaveRequestDTO dto, long requestedDays, int totalLeaveApplyCount) {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(dto.getUserId());
        leave.setLeaveTypeName(dto.getLeaveTypeName());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setAppliedDays((int) requestedDays);
        leave.setReportingManager(dto.getReportingManager());
        leave.setDayOffType(DayOffType.fromString(dto.getDayOffType()));
        leave.setCreatedAt(LocalDateTime.now());
        leave.setCreatedBy("system");
        leave.setStatus(LeaveStatus.PENDING);
        leave.setDeleted(false);
        leave.setTotalLeaveApply(totalLeaveApplyCount );

        int defaultMaxDays = 20;
        leave.setMaxDays(defaultMaxDays);
        leave.setRemainingDays(defaultMaxDays - (int) requestedDays); // First time remaining
        return leave;
    }

    public void updateExistingLeaveApplication(LeaveApplication leave, LeaveRequestDTO dto, long requestedDays, int newTotalLeaveApply) {
        leave.setLeaveTypeName(dto.getLeaveTypeName());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setAppliedDays((int) requestedDays);
        leave.setReportingManager(dto.getReportingManager());
        leave.setDayOffType(DayOffType.fromString(dto.getDayOffType()));
        leave.setStatus(LeaveStatus.PENDING);
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy("system");
        leave.setTotalLeaveApply(newTotalLeaveApply );

        leave.setRemainingDays(leave.getRemainingDays() - (int) requestedDays);

    }
}
