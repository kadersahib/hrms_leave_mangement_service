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

    public LeaveApplication toNewLeaveApplication(LeaveRequestDTO dto, int totalAppliedDays,int remainingDays) {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(dto.getUserId());
        leave.setLeaveTypeId(dto.getLeaveId());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setTotalAppliedDays(totalAppliedDays);
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

    public void updateExistingLeaveApplication(LeaveApplication leave, LeaveRequestDTO dto, long requestedDays) {
        leave.setLeaveTypeId(dto.getLeaveId());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());

        // Add to existing totalAppliedDays instead of overwriting
        int updatedAppliedDays = leave.getTotalAppliedDays() + (int) requestedDays;
        leave.setTotalAppliedDays(updatedAppliedDays);
        int appliedDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        leave.setAppliedDays(appliedDays);

        leave.setReportingId(dto.getReportingId());
        leave.setDayOffType(DayOffType.fromString(dto.getDayOffType()));
        leave.setStatus(LeaveStatus.PENDING);
        leave.setCreatedAt(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());

        String userIdString = String.valueOf(dto.getUserId());
        leave.setUpdatedBy(userIdString);

        leave.setRemainingDays(leave.getRemainingDays() - (int) requestedDays);
    }

}
