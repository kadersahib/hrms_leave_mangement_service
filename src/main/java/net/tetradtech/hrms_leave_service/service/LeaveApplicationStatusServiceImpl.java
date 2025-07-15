package net.tetradtech.hrms_leave_service.service;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveApplicationStatusServiceImpl implements LeaveApplicationStatusService {

    @Autowired
    private  LeaveApplicationRepository leaveApplicationRepository;


    @Override
    public List<LeaveApplication> filterByAllStatus() {
        return leaveApplicationRepository.findByIsDeletedFalse();
    }
    @Override
    public List<LeaveApplication> filterByStatus(String status) {
        LeaveStatus parsedStatus = parseStatus(status);
        return leaveApplicationRepository.findByStatusAndIsDeletedFalse(parsedStatus);
    }

    @Override
    public List<LeaveApplication> filterByUserId(Long userId) {
        if (!leaveApplicationRepository.existsByUserIdAndIsDeletedFalse(userId)) {
            throw new IllegalArgumentException("User ID " + userId + " not found in leave applications");
        }
        return leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public List<LeaveApplication> filterByLeaveType(Long leaveTypeId) {
        if (!leaveApplicationRepository.findByLeaveTypeIdAndIsDeletedFalse(leaveTypeId).isEmpty()) {
            return leaveApplicationRepository.findByLeaveTypeIdAndIsDeletedFalse(leaveTypeId);
        } else {
            throw new IllegalArgumentException("No leave applications found for leaveTypeId: " + leaveTypeId);
        }
    }

    @Override
    public List<LeaveApplication> filterByUserIdAndStatus(Long userId, String status) {
        if (!leaveApplicationRepository.existsByUserIdAndIsDeletedFalse(userId)) {
            throw new IllegalArgumentException("User ID " + userId + " not found in leave applications.");
        }
        LeaveStatus parsedStatus = parseStatus(status);
        List<LeaveApplication> result = leaveApplicationRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, parsedStatus);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("No leave records found with status '" + parsedStatus + "' for user ID: " + userId);
        }

        return result;
    }

    private LeaveStatus parseStatus(String status) {
        try {
            return LeaveStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: Only PENDING, APPROVED, REJECTED are allowed.");
        }
    }

    @Override
    public List<LeaveApplication> filterByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        if (!leaveApplicationRepository.existsByUserIdAndIsDeletedFalse(userId)) {
            throw new IllegalArgumentException("No leaves found for user ID: " + userId);
        }

        List<LeaveApplication> leaves = leaveApplicationRepository
                .findByUserIdAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndIsDeletedFalse(
                        userId, startDate, endDate);
        if (leaves.isEmpty()) {
            throw new IllegalArgumentException("No leave data found for user ID " + userId +
                    " between " + startDate + " and " + endDate);
        }

        return leaves;
    }



}