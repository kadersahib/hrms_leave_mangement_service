//package net.tetradtech.hrms_leave_service.service;
//
//import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
//import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
//import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
//import net.tetradtech.hrms_leave_service.model.LeaveApplication;
//import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;
//import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.Optional;
//
//
//@Service
//
//public class LeaveApprovalServiceImpl implements LeaveApprovalService{
//
//    @Autowired
//    private LeaveApplicationRepository leaveApplicationRepository;
//
//    @Autowired
//    private LeaveTypeClient leaveTypeClient;
//
//
//    private static final String SYSTEM_USER = "system";
//
//
//    public LeaveApplication performAction(Long userId, LeaveApprovalDTO dto) {
//        // Find the latest pending leave application for the user
//        LeaveApplication leave = leaveApplicationRepository
//                .findTopByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
//                .orElseThrow(() -> new IllegalArgumentException("No active leave found for user ID: " + userId));
//
//        if (leave.getStatus() == LeaveStatus.CANCELLED || dto.getAction().equalsIgnoreCase("CANCELLED")) {
//            throw new IllegalStateException("Cannot approve/reject a cancelled leave.");
//        }
//
//        LeaveStatus newStatus = LeaveStatus.valueOf(dto.getAction().toUpperCase());
//        long appliedDays = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
//
//        int year = leave.getStartDate().getYear();
//        long totalUsedDays = leaveApplicationRepository.sumApprovedLeaveDaysByUserIdAndYear(userId, year);
//
//        if (newStatus == LeaveStatus.APPROVED) {
//            if (totalUsedDays + appliedDays > 20) {
//                throw new IllegalStateException("Leave limit exceeded. Only 20  leave days allowed per year.");
//            }
//
//            leave.setAppliedDays((int) appliedDays);
//            leave.setRemainingDays(20 -(int) (totalUsedDays + appliedDays));
//        } else {
//            leave.setAppliedDays((int) appliedDays);
//            leave.setRemainingDays(null);
//        }
//
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
//
//
//}
