package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.util.LeaveTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;



@Service
public class LeaveApprovalServiceImpl implements LeaveApprovalService{

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;



    @Override
    public LeaveApplication approveOrRejectLeave(Long leaveId, LeaveApprovalDTO request) {
        LeaveApplication leave = leaveApplicationRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + leaveId));

        if (request.getApproveId() == null) {
            throw new IllegalArgumentException("Approver ID is required.");
        }

        if (!leave.getReportingId().equals(request.getApproveId())) {
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

        //  Save comment and approver ID
        leave.setComment(request.getComment());
        leave.setApprovedId(String.valueOf(request.getApproveId())); // or Long if field is Long
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(request.getApproveId());

        return leaveApplicationRepository.save(leave);
    }

//    @Override
//    public LeaveApplication updateApproval(Long leaveId, LeaveApprovalDTO dto) {
//        LeaveApplication leave = leaveApplicationRepository.findByIdAndIsDeletedFalse(leaveId)
//                .orElseThrow(() -> new IllegalArgumentException("Leave not found or already deleted (ID: " + leaveId + ")"));
//
//        if (leave.getStatus() == LeaveStatus.CANCELLED || dto.getAction().equalsIgnoreCase("CANCELLED")) {
//            throw new IllegalStateException("Cannot approve/reject a cancelled leave.");
//        }
//
//        LeaveStatus newStatus = LeaveStatus.valueOf(dto.getAction().toUpperCase());
////
////        if (newStatus == LeaveStatus.APPROVED) {
////            long approvedDays = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
////
////            LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(leave.getLeaveTypeId());
////            if (leaveType == null) {
////                throw new IllegalArgumentException("Leave type not found for ID: " + leave.getLeaveTypeId());
////            }
////
////            int maxAllowed = leaveType.getMaxDays();
////            int remaining = maxAllowed - (int) approvedDays;
////
////            leave.setRemainingDays(remaining);
////        } else {
////            leave.setRemainingDays(null);
////        }
//
//        // Update leave application details
//        leave.setStatus(newStatus);
//        leave.setApprovalComment(dto.getComment());
//        leave.setApprovedBy(dto.getPerformedBy());
//        leave.setApprovalTimestamp(LocalDateTime.now());
//        leave.setUpdatedAt(LocalDateTime.now());
//        leave.setUpdatedBy(SYSTEM_USER);
//
//        return leaveApplicationRepository.save(leave);
//    }
//
//    @Override
//    public List<LeaveApplication> getAll() {
//        return leaveApplicationRepository.findByIsDeletedFalse();
//    }
//
//    @Override
//    public LeaveApplication getById(Long id) {
//        return leaveApplicationRepository.findByIdAndIsDeletedFalse(id)
//                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + id));
//    }
//
//    @Override
//    public void deleteById(Long id) {
//        LeaveApplication leave = leaveApplicationRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + id));
//        leave.setDeleted(true);
//        leave.setDeletedAt(LocalDateTime.now());
//        leave.setDeletedBy(SYSTEM_USER);
//        leaveApplicationRepository.save(leave);
//    }


}
