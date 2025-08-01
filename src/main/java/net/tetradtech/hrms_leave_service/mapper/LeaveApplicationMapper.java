package net.tetradtech.hrms_leave_service.mapper;

import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveUpdateRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class LeaveApplicationMapper {

    public LeaveApplication toNewLeaveApplication(LeaveRequestDTO dto, int remainingDays) {
        LeaveApplication leave = new LeaveApplication();
        leave.setUserId(dto.getUserId());
        leave.setLeaveTypeId(dto.getLeaveTypeId());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        int appliedDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        leave.setAppliedDays(appliedDays);
        leave.setReportingId(dto.getReportingId());
        leave.setCreatedAt(LocalDateTime.now());
        leave.setDayOffType(DayOffType.fromString(dto.getDayOffType()));

        if (dto.getLeaveTypeId() == 5L) {
            leave.setLeaveOtherReason(dto.getLeaveOtherReason());
            leave.setReason("Others");
        } else {
            leave.setReason(dto.getReason());
            leave.setLeaveOtherReason(null);
        }
        String userIdString = String.valueOf(dto.getUserId());
        leave.setCreatedBy(userIdString);

        leave.setStatus(LeaveStatus.PENDING);
        leave.setDeleted(false);

        leave.setRemainingDays(remainingDays);
        return leave;
    }

    public LeaveApplication updateLeaveFromDto(LeaveApplication existingLeave, LeaveUpdateRequestDTO dto, int requestedDays, int remainingDays) {
        existingLeave.setLeaveTypeId(dto.getLeaveTypeId());
        existingLeave.setStartDate(dto.getStartDate());
        existingLeave.setEndDate(dto.getEndDate());
        existingLeave.setReportingId(dto.getReportingId());
        existingLeave.setDayOffType(DayOffType.valueOf(dto.getDayOffType().toUpperCase()));
        existingLeave.setUpdatedAt(LocalDateTime.now());
        existingLeave.setUpdatedBy(String.valueOf(dto.getUserId()));
        existingLeave.setAppliedDays(requestedDays);

        if (dto.getLeaveTypeId() == 5L) {
            existingLeave.setReason("Others");
            existingLeave.setLeaveOtherReason(dto.getLeaveOtherReason());
        } else {
            existingLeave.setReason(dto.getReason());
            existingLeave.setLeaveOtherReason(null);
        }

        existingLeave.setRemainingDays(remainingDays);
        return existingLeave;
    }




}