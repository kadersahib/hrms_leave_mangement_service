package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.util.LeaveTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class LeaveApprovalServiceImpl implements LeaveApprovalService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;


    @Override
    public LeaveApplication approveOrRejectLeave(Long leaveId, LeaveApprovalDTO request) {
        LeaveApplication leave = leaveApplicationRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + leaveId));

        if (request.getApproverId() == null) {
            throw new IllegalArgumentException("Approver ID is required.");
        }

        if (!leave.getReportingId().equals(request.getApproverId())) {
            throw new IllegalArgumentException("You are not authorized to approve/reject this leave.");
        }

        if (leave.isDeleted()) {
            throw new IllegalArgumentException("Cannot approve/reject a deleted leave.");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leaves can be approved or rejected.");
        }

        int maxDays = LeaveTypeUtil.getMaxDays(leave.getLeaveTypeId());
        int currentYear = leave.getStartDate().getYear();

        if ("approved".equalsIgnoreCase(request.getAction())) {
            leave.setStatus(LeaveStatus.APPROVED);

            int usedDays = leaveApplicationRepository.getTotalUsedDaysForYear(
                    leave.getUserId(), leave.getLeaveTypeId(), currentYear);
            leave.setRemainingDays(maxDays - usedDays);

        } else if ("rejected".equalsIgnoreCase(request.getAction())) {
            leave.setStatus(LeaveStatus.REJECTED);

            int usedDays = leaveApplicationRepository.getTotalUsedDaysForYear(
                    leave.getUserId(), leave.getLeaveTypeId(), currentYear) - leave.getAppliedDays();
            leave.setRemainingDays(maxDays - usedDays);

        } else {
            throw new IllegalArgumentException("Invalid action. Use 'approved' or 'rejected'.");
        }

        leave.setApproverComment(request.getApproverComment());
        leave.setApproverId(request.getApproverId());
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(String.valueOf(request.getApproverId()));

        return leaveApplicationRepository.save(leave);
    }


    @Override
    public LeaveApplication changeLeaveStatus(Long leaveId, Long approverId, String comment) {
        LeaveApplication leave = leaveApplicationRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + leaveId));

        if (approverId == null) {
            throw new IllegalArgumentException("Approver ID is required.");
        }

        if (!leave.getReportingId().equals(approverId)) {
            throw new IllegalArgumentException("You are not authorized to toggle this leave.");
        }

        if (leave.isDeleted()) {
            throw new IllegalArgumentException("Cannot toggle a deleted leave.");
        }

        int maxDays = LeaveTypeUtil.getMaxDays(leave.getLeaveTypeId());
        int currentYear = leave.getStartDate().getYear();

        if (leave.getStatus() == LeaveStatus.APPROVED) {
            leave.setStatus(LeaveStatus.REJECTED);
            int usedDays = leaveApplicationRepository.getTotalUsedDaysForYear(
                    leave.getUserId(), leave.getLeaveTypeId(), currentYear) - leave.getAppliedDays();
            leave.setRemainingDays(maxDays - usedDays);

        } else if (leave.getStatus() == LeaveStatus.REJECTED) {
            leave.setStatus(LeaveStatus.APPROVED);
            int usedDays = leaveApplicationRepository.getTotalUsedDaysForYear(
                    leave.getUserId(), leave.getLeaveTypeId(), currentYear);
            leave.setRemainingDays(maxDays - usedDays);

        } else {
            throw new IllegalStateException("Only APPROVED or REJECTED leaves can be toggled.");
        }

        leave.setApproverComment(comment);
        leave.setApproverId(approverId);
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(String.valueOf(approverId));

        return leaveApplicationRepository.save(leave);
    }

    @Override
    public List<LeaveApplication> getAllApprovals() {
        return leaveApplicationRepository.findByIsDeletedFalse();
    }

    @Override
    public List<LeaveApplication> getByUserId(Long userId) {
        return leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);
    }


    @Override
    public void deleteApprovalById(Long id) {
        LeaveApplication leave = leaveApplicationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + id));

        leave.setDeleted(true);
        leave.setDeletedAt(LocalDateTime.now());
        leave.setDeletedBy(String.valueOf(leave.getApproverId()));
        leave.setUpdatedAt(LocalDateTime.now());
        leaveApplicationRepository.save(leave);
    }

}