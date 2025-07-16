package net.tetradtech.hrms_leave_service.mapper;

import net.tetradtech.hrms_leave_service.dto.LeaveHistoryDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;

public class LeaveHistoryMapper {
    public static LeaveHistoryDTO toDTO(LeaveApplication leave, String userName, String leaveTypeName) {
        LeaveHistoryDTO dto = new LeaveHistoryDTO();
        dto.setId(leave.getId());
        dto.setUserId(leave.getUserId());
        dto.setName(userName);
        dto.setLeaveTypeId(leave.getLeaveTypeId());
        dto.setLeaveTypeName(leaveTypeName);
        dto.setStatus(leave.getStatus().name());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setAppliedDays(leave.getAppliedDays());
        dto.setReportingManager(leave.getReportingManager());
        dto.setApprovalComment(leave.getApprovalComment());
        dto.setApprovedBy(leave.getApprovedBy());
        dto.setApprovalTimestamp(leave.getApprovalTimestamp());
        dto.setCreatedAt(leave.getCreatedAt());
        return dto;
    }
}
