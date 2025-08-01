package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.*;
import net.tetradtech.hrms_leave_service.mapper.LeaveApplicationMapper;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.util.LeaveTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private LeaveApplicationMapper leaveApplicationMapper;



    @Override
    public LeaveApplication applyLeave(LeaveRequestDTO application) {

        UserDTO user = userServiceClient.getUserById(application.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + application.getUserId());
        }

        LocalDate today = LocalDate.now();
        if (!application.getStartDate().isAfter(today)) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (application.getEndDate().isBefore(application.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        long overlappingCount = leaveApplicationRepository.countOverlappingLeaves(
                application.getUserId(),
                application.getStartDate(),
                application.getEndDate()
        );

        if (overlappingCount > 0) {
            throw new IllegalArgumentException("Leave dates overlap with an existing leave for this user.");
        }


        long requestedDays = ChronoUnit.DAYS.between(application.getStartDate(), application.getEndDate()) + 1;

        int maxDays = LeaveTypeUtil.getMaxDays(application.getLeaveTypeId());

        int year = application.getStartDate().getYear();
        int usedDays = leaveApplicationRepository.getTotalUsedDaysForYear(
                application.getUserId(), application.getLeaveTypeId(), year);

        int remainingDays = maxDays - (usedDays + (int) requestedDays);

        if (remainingDays < 0) {
            throw new IllegalArgumentException("You only have " + (maxDays - usedDays) +
                    " days remaining for this leave type in " + year + ".");
        }

        LeaveApplication newLeave = leaveApplicationMapper
                .toNewLeaveApplication(application, remainingDays);

        return leaveApplicationRepository.save(newLeave);
    }

    @Override
    public LeaveApplication updateLeave(Long id, LeaveUpdateRequestDTO updatedData) {
        LeaveApplication existingLeave = leaveApplicationRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave application not found (ID: " + id + ")"));

        if (!existingLeave.getUserId().equals(updatedData.getUserId())) {
            throw new IllegalArgumentException("User ID does not match the leave .");
        }
        if (existingLeave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leave can be updated.");
        }

        LocalDate today = LocalDate.now();
        if (updatedData.getStartDate().isBefore(today)) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }

        if (updatedData.getEndDate().isBefore(updatedData.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        int overlappingCount = leaveApplicationRepository.countOverlappingLeavesForUpdate(
                updatedData.getUserId(),
                id,
                updatedData.getStartDate(),
                updatedData.getEndDate()
        );

        if (overlappingCount > 0) {
            throw new IllegalArgumentException("Leave dates overlap with an existing leave for this user.");
        }

        long requestedDays = ChronoUnit.DAYS.between(updatedData.getStartDate(), updatedData.getEndDate()) + 1;

        int maxDays = LeaveTypeUtil.getMaxDays(updatedData.getLeaveTypeId());

        int year = updatedData.getStartDate().getYear();
        int usedDays = leaveApplicationRepository.getTotalUsedDaysForYearExcludingId(
                updatedData.getUserId(), updatedData.getLeaveTypeId(), year, id);

        int remainingDays = maxDays - (usedDays + (int) requestedDays);

        if (remainingDays < 0) {
            throw new IllegalArgumentException("You only have " + (maxDays - usedDays) +
                    " days remaining for this leave type in " + year + ".");
        }

        existingLeave = leaveApplicationMapper.updateLeaveFromDto(existingLeave, updatedData, (int) requestedDays, remainingDays);

        return leaveApplicationRepository.save(existingLeave);
    }

    @Override
    public LeaveApplication getLeaveById(Long id) {
        return leaveApplicationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave application not found with ID: " + id));
    }

    @Override
    public List<LeaveApplication> getAllLeaves() {
        return leaveApplicationRepository.findByIsDeletedFalse();
    }

    @Override
    public void deleteById(Long Id) {
        // Fetch the latest non-deleted leave
        LeaveApplication latest = leaveApplicationRepository
                .findById(Id)
                .orElseThrow(() -> new IllegalArgumentException("No recent leave found for user"));

        latest.setDeleted(true);
        latest.setDeletedAt(LocalDateTime.now());
        latest.setDeletedBy(String.valueOf(latest.getUserId()));
        leaveApplicationRepository.save(latest);
    }

    public LeaveApplication cancelLeave(Long id) {
        LeaveApplication leave = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found (ID: " + id + ")"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leaves can be cancelled.");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(String.valueOf(leave.getUserId()));

        return leaveApplicationRepository.save(leave);
    }


}