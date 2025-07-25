package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.constants.DayOffType;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.*;
import net.tetradtech.hrms_leave_service.mapper.LeaveApplicationMapper;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.constants.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
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

    private static final String SYSTEM_USER = "system";
    private static final int SICK_LEAVE = 30;
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

        int requestedDays = (int) ChronoUnit.DAYS.between(application.getStartDate(),
                application.getEndDate()) + 1;

        boolean sameDateLeaveExists = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(application.getUserId())
                .stream()
                .anyMatch(existing ->
                        existing.getStartDate().equals(application.getStartDate()) &&
                                existing.getEndDate().equals(application.getEndDate()));
        if (sameDateLeaveExists) {
            throw new IllegalArgumentException("Leave already applied for the same date range.");
        }
        int maxDays = getMaxDaysForLeaveType(application.getLeaveId());

        List<LeaveApplication> existingLeave = leaveApplicationRepository
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(application.getUserId(), application.getLeaveId());

        if (existingLeave.isEmpty()) {
            int remainingDays = maxDays - requestedDays;
            if (remainingDays < 0) {
                throw new IllegalArgumentException("Leave request exceeds maximum allowed days.");
            }

            LeaveApplication newLeave = leaveApplicationMapper
                    .toNewLeaveApplication(application, requestedDays, remainingDays);

            return leaveApplicationRepository.save(newLeave);
        } else {
            LeaveApplication existing = existingLeave.stream().findFirst().orElse(null);

            if (existing == null) {
                throw new IllegalArgumentException("No previous leave record found.");
            }

            int remainingDays = existing.getRemainingDays();

            if (remainingDays < requestedDays) {
                throw new IllegalArgumentException("Leave request exceeds remaining balance. You have only "
                        + remainingDays + " days left.");
            }

            leaveApplicationMapper.updateExistingLeaveApplication(existing, application, requestedDays);
            return leaveApplicationRepository.save(existing);
        }
    }

    private int getMaxDaysForLeaveType(Long leaveTypeId) {
        if (leaveTypeId == 1L) {
            return SICK_LEAVE;
        } else if (leaveTypeId == 2L) {
            return PERSONAL_LEAVE;
        } else {
            throw new IllegalArgumentException("Unsupported leave type ID: " + leaveTypeId);
        }
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

        long requestedDays = ChronoUnit.DAYS.between(updatedData.getStartDate(), updatedData.getEndDate()) + 1;

        boolean isOverlapping = leaveApplicationRepository
                .findByUserIdAndIsDeletedFalse(existingLeave.getUserId()).stream()
                .filter(l -> !l.getId().equals(existingLeave.getId()))
                .anyMatch(l ->
                        !l.getStartDate().isAfter(updatedData.getEndDate()) &&
                                !l.getEndDate().isBefore(updatedData.getStartDate()));

        if (isOverlapping) {
            throw new IllegalArgumentException("Leave dates overlap with an existing leave for the user.");
        }

        int maxDays = getMaxDaysForLeaveType(updatedData.getLeaveId());

        int totalOtherAppliedDays = leaveApplicationRepository
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(existingLeave.getUserId(), updatedData.getLeaveId()).stream()
                .filter(l -> !l.getId().equals(existingLeave.getId()))
                .mapToInt(LeaveApplication::getTotalAppliedDays)
                .sum();

        int totalAppliedDays = totalOtherAppliedDays + (int) requestedDays;
        int remainingDays = maxDays - totalAppliedDays;

        existingLeave.setLeaveTypeId(updatedData.getLeaveId());
        existingLeave.setStartDate(updatedData.getStartDate());
        existingLeave.setEndDate(updatedData.getEndDate());
        existingLeave.setTotalAppliedDays((int) requestedDays);
        existingLeave.setReportingId(updatedData.getReportingId());
        existingLeave.setDayOffType(DayOffType.valueOf(updatedData.getDayOffType().toUpperCase()));
        existingLeave.setUpdatedAt(LocalDateTime.now());
        existingLeave.setRemainingDays(remainingDays);

        String userIdString = String.valueOf(updatedData.getUserId());
        existingLeave.setUpdatedBy(userIdString);

        int appliedDays = (int) ChronoUnit.DAYS.between(updatedData.getStartDate(), updatedData.getEndDate()) + 1;
        existingLeave.setAppliedDays(appliedDays);

        return leaveApplicationRepository.save(existingLeave);
    }



    @Override
    public List<LeaveApplication> getLeavesByUserId(Long userId) {
        // Validate the user exists
        userServiceClient.getUserById(userId);
        return leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);
    }

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
        latest.setDeletedBy(SYSTEM_USER);
        leaveApplicationRepository.save(latest);
    }


    public LeaveApplication cancelLeave(Long Id) {
        LeaveApplication leave = leaveApplicationRepository.findById(Id)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found or already deleted (ID: " + Id + ")"));


        if (leave.isDeleted()) {
            throw new IllegalArgumentException("Cannot cancel a leave that is already deleted (ID: " + Id + ")");
        }

        if (!leave.getId().equals(leave.getId())) {
            throw new IllegalArgumentException("Leave does not belong to this ID: " + leave.getId());
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leaves can be cancelled.");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setDeletedAt(LocalDateTime.now());
        leave.setDeletedBy(SYSTEM_USER);

        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(SYSTEM_USER);

        return leaveApplicationRepository.save(leave);
    }

}