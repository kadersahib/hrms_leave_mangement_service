package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.Enum.DayOffType;
import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.*;
import net.tetradtech.hrms_leave_service.mapper.LeaveApplicationMapper;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private LeaveApplicationMapper leaveApplicationMapper;


    private static final String SYSTEM_USER = "system";
    private static final int SICK_LEAVE = 20;
    private static final int PERSONAL_LEAVE = 20;


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

        long requestedDays = ChronoUnit.DAYS.between(application.getStartDate(), application.getEndDate()) + 1;

        // Check for existing pending leave
//        boolean hasPending = leaveApplicationRepository
//                .findByUserIdAndIsDeletedFalse(application.getUserId())
//                .stream()
//                .anyMatch(leaveApp -> leaveApp.getStatus() == LeaveStatus.PENDING);
//
//        if (hasPending) {
//            throw new IllegalStateException("You already have a pending leave request. Please wait for it to be approved/rejected/cancelled.");
//        }

        boolean sameDateLeaveExists = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(application.getUserId())
                .stream()
                .anyMatch(existing ->
                        existing.getStartDate().equals(application.getStartDate()) &&
                                existing.getEndDate().equals(application.getEndDate()));
        if (sameDateLeaveExists) {
            throw new IllegalArgumentException("Leave already applied for the same date range.");
        }

        LeaveApplication leave = leaveApplicationRepository
                .findByUserIdAndLeaveTypeNameAndIsDeletedFalse(application.getUserId(), application.getLeaveTypeName());

        String leaveTypeName = application.getLeaveTypeName().toLowerCase();
        int maxDays;

        if (leaveTypeName.equals("sick leave")) {
            maxDays = SICK_LEAVE;
        } else if (leaveTypeName.equals("personal leave")) {
            maxDays = PERSONAL_LEAVE;
        } else {
            throw new IllegalArgumentException("Unsupported leave type: " + leaveTypeName);
        }

        if (leave == null) {
            // First-time application for this leave type
            int totalApplyCount = 1;
            leave = leaveApplicationMapper.toNewLeaveApplication(application, requestedDays, totalApplyCount);
            leave.setMaxDays(maxDays);
            leave.setRemainingDays(maxDays - totalApplyCount);

            if (leave.getRemainingDays() < 0) {
                throw new IllegalArgumentException("Leave request exceeds maximum allowed days.");
            }

        } else {
            // Existing leave application
            int remainingDays = leave.getRemainingDays();
            if (remainingDays < requestedDays) {
                throw new IllegalArgumentException("Leave request exceeds remaining balance. You have only " + remainingDays + " days left.");
            }

            int currentTotal = (leave.getTotalLeaveApply() != null) ? leave.getTotalLeaveApply() : 0;
            int newTotal = currentTotal + 1; // ✅ increment by 1 per request

            leaveApplicationMapper.updateExistingLeaveApplication(leave, application, requestedDays, newTotal);

        }

        return leaveApplicationRepository.save(leave);
    }

    @Override
    public LeaveApplication updateLeave(Long userId, LeaveRequestDTO updatedData) {
        LeaveApplication existingLeave = leaveApplicationRepository
                .findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("Leave application not found for user and type"));

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

        long requestedDays = ChronoUnit.DAYS.between(updatedData.getStartDate(), updatedData.getEndDate()) + 1;

        boolean isOverlapping = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .filter(existing -> !existing.getId().equals(existingLeave.getId())) // Exclude current leave
                .anyMatch(existing ->
                        !existing.getStartDate().isAfter(updatedData.getEndDate()) &&
                                !existing.getEndDate().isBefore(updatedData.getStartDate()));

        if (isOverlapping) {
            throw new IllegalArgumentException("Leave dates overlap with an existing leave for the user.");
        }

        String leaveTypeName = updatedData.getLeaveTypeName().toLowerCase();
        int maxDays;

        if (leaveTypeName.equals("sick leave")) {
            maxDays = SICK_LEAVE;
        } else if (leaveTypeName.equals("personal leave")) {
            maxDays = PERSONAL_LEAVE;
        } else {
            throw new IllegalArgumentException("Unsupported leave type: " + leaveTypeName);
        }

        // Update fields
        existingLeave.setLeaveTypeName(updatedData.getLeaveTypeName());
        existingLeave.setStartDate(updatedData.getStartDate());
        existingLeave.setEndDate(updatedData.getEndDate());
        existingLeave.setAppliedDays((int) requestedDays);
        existingLeave.setReportingManager(updatedData.getReportingManager());
        existingLeave.setDayOffType(DayOffType.valueOf(updatedData.getDayOffType().toUpperCase()));
        existingLeave.setUpdatedAt(LocalDateTime.now());
        existingLeave.setUpdatedBy("system");
        existingLeave.setMaxDays(maxDays);

        int updatedRemainingDays = existingLeave.getMaxDays() - (int) requestedDays;
        existingLeave.setRemainingDays(updatedRemainingDays);

        return leaveApplicationRepository.save(existingLeave);
    }



    @Override
    public Optional<LeaveApplication> getUpdateByUserId(Long userId) {
        return leaveApplicationRepository.findAll().stream()
                .filter(l -> l.getUserId().equals(userId) && !l.isDeleted())
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .findFirst();
    }


    @Override
    public List<LeaveApplication> getLeavesByUserId(Long userId) {
        // Validate the user exists
        userServiceClient.getUserById(userId);
        return leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    // Return only active (non-deleted) leaves
    @Override
    public List<LeaveApplication> getAllLeaves() {
        return leaveApplicationRepository.findByIsDeletedFalse();
    }

    @Override
    public void deleteLatestLeaveByUserId(Long userId) {
        // Validate user exists
        UserDTO user = userServiceClient.getUserById(userId);

        // Fetch the latest non-deleted leave
        LeaveApplication latest = leaveApplicationRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No recent leave found for user"));

        latest.setDeleted(true);
        latest.setDeletedAt(LocalDateTime.now());
//        latest.setDeletedBy(user.getName());
        latest.setDeletedBy(SYSTEM_USER);
        leaveApplicationRepository.save(latest);
    }


    public LeaveApplication cancelLeave(Long userId) {
        LeaveApplication leave = leaveApplicationRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found or already deleted (ID: " + userId + ")"));

        if (!leave.getUserId().equals(leave.getUserId())) {
            throw new IllegalArgumentException("Leave does not belong to user ID: " + leave.getUserId());
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leaves can be cancelled.");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setCancelledAt(LocalDateTime.now());
        leave.setCancelledBy(SYSTEM_USER);

//        int originalAppliedDays = leave.getAppliedDays();
//        leave.setAppliedDays(0);
//
//        int updatedRemainingDays = leave.getRemainingDays() + originalAppliedDays;
//        leave.setRemainingDays(updatedRemainingDays);
//
//        int totalLeaveApply = leave.getTotalLeaveApply();
//        leave.setTotalLeaveApply(Math.max(0, totalLeaveApply - 1));

        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(SYSTEM_USER);

        return leaveApplicationRepository.save(leave);
    }

}