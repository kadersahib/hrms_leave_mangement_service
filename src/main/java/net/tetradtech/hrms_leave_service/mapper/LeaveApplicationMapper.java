package net.tetradtech.hrms_leave_service.mapper;

import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LeaveApplicationMapper {

    public LeaveApplication toNewLeaveApplication(LeaveRequestDTO dto, int totalAppliedDays,int remainingDays) {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(dto.getUserId());
        leave.setLeaveTypeName(dto.getLeaveTypeName());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setTotalAppliedDays(totalAppliedDays);
        leave.setReportingManager(dto.getReportingManager());
        leave.setAppliedDate(LocalDateTime.now());
        leave.setDayOffType(DayOffType.fromString(dto.getDayOffType()));
        leave.setCreatedAt(LocalDateTime.now());
        leave.setCreatedBy("system");
        leave.setStatus(LeaveStatus.PENDING);
        leave.setDeleted(false);
        leave.setTotalCount(1);
        int maxDays = 20;
        leave.setMaxDays(maxDays);
        leave.setRemainingDays(remainingDays);
        return leave;
    }

    public void updateExistingLeaveApplication(LeaveApplication leave, LeaveRequestDTO dto, long requestedDays) {
        leave.setLeaveTypeName(dto.getLeaveTypeName());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());

        // Add to existing totalAppliedDays instead of overwriting
        int updatedAppliedDays = leave.getTotalAppliedDays() + (int) requestedDays;
        leave.setTotalAppliedDays(updatedAppliedDays);

        leave.setReportingManager(dto.getReportingManager());
        leave.setDayOffType(DayOffType.fromString(dto.getDayOffType()));
        leave.setStatus(LeaveStatus.PENDING);
        leave.setAppliedDate(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy("system");

        leave.setTotalCount(leave.getTotalCount() + 1);

        // Reduce remaining days
        leave.setRemainingDays(leave.getRemainingDays() - (int) requestedDays);
    }

}
