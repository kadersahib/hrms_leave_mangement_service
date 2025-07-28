package net.tetradtech.hrms_leave_service.mapper;

import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class LeaveApplicationMapper {

    public LeaveApplication toNewLeaveApplication(LeaveRequestDTO dto, int totalLeaveDays, int remainingDays) {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(dto.getUserId());
        leave.setLeaveTypeId(dto.getLeaveId());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setTotalLeaveDays(totalLeaveDays);
        int appliedDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        leave.setAppliedDays(appliedDays);
        leave.setReportingId(dto.getReportingId());
        leave.setCreatedAt(LocalDateTime.now());
        leave.setDayOffType(DayOffType.fromString(dto.getDayOffType()));

        String userIdString = String.valueOf(dto.getUserId());
        leave.setCreatedBy(userIdString);

        leave.setStatus(LeaveStatus.PENDING);
        leave.setDeleted(false);

        leave.setRemainingDays(remainingDays);
        return leave;
    }


    public void updateExistingLeaveApplication(LeaveApplication leave, LeaveRequestDTO dto, int requestedDays, int remainingDays) {
        leave.setLeaveTypeId(dto.getLeaveId());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());

        leave.setAppliedDays(requestedDays);
        leave.setTotalLeaveDays(leave.getTotalLeaveDays() + requestedDays);

        leave.setReportingId(dto.getReportingId());
        leave.setDayOffType(DayOffType.fromString(dto.getDayOffType()));
        leave.setStatus(LeaveStatus.PENDING);
        leave.setUpdatedAt(LocalDateTime.now());

        String userIdString = String.valueOf(dto.getUserId());
        leave.setUpdatedBy(userIdString);

        leave.setRemainingDays(remainingDays);
    }

}