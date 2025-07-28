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

import java.time.DayOfWeek;
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

        boolean activeLeaveExists = leaveApplicationRepository
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(application.getUserId(), application.getLeaveId())
                .stream()
                .anyMatch(l -> !("CANCELLED".equalsIgnoreCase(String.valueOf(l.getStatus())) ||
                        "APPROVED".equalsIgnoreCase(String.valueOf(l.getStatus())) ||
                        "REJECTED".equalsIgnoreCase(String.valueOf(l.getStatus()))));

        if (activeLeaveExists) {
            throw new IllegalArgumentException("You already have an active leave request for this leave type. Wait until its status changes.");
        }

        boolean sameDateLeaveExists = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(application.getUserId())
                .stream()
                .anyMatch(existing ->
                        existing.getStartDate().equals(application.getStartDate()) &&
                                existing.getEndDate().equals(application.getEndDate()));
        if (sameDateLeaveExists) {
            throw new IllegalArgumentException("Leave already applied for the same date range.");
        }
        int requestedDays = calculateWorkingDays(application.getStartDate(), application.getEndDate());
        int maxDays = getMaxDaysForLeaveType(application.getLeaveId());
        int leaveYear = application.getStartDate().getYear();

        Optional<LeaveApplication> existingLeaveOpt = leaveApplicationRepository
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(application.getUserId(), application.getLeaveId()).stream()
                .filter(l -> l.getStartDate().getYear() == leaveYear)
                .findFirst();


        if (existingLeaveOpt.isPresent()) {
            LeaveApplication existingLeave = existingLeaveOpt.get();

            // Calculate new total used days (excluding weekends)
            int usedDays = existingLeave.getTotalLeaveDays();
            int totalUsedDays = usedDays + requestedDays;
            int remainingDays = maxDays - totalUsedDays;

            if (remainingDays < 0) {
                throw new IllegalArgumentException("Leave request exceeds remaining balance for " + leaveYear +
                        ". You have only " + (maxDays - usedDays) + " days left.");
            }

            leaveApplicationMapper.updateExistingLeaveApplication(existingLeave, application, requestedDays, remainingDays);
            return leaveApplicationRepository.save(existingLeave);
        }

        // if no existing leave found, create a new one
        int remainingDays = maxDays - requestedDays;
        if (remainingDays < 0) {
            throw new IllegalArgumentException("Leave request exceeds maximum allowed days.");
        }

        LeaveApplication newLeave = leaveApplicationMapper
                .toNewLeaveApplication(application, requestedDays, remainingDays);

        return leaveApplicationRepository.save(newLeave);
    }

     int getMaxDaysForLeaveType(Long leaveTypeId) {
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

        int requestedDays = calculateWorkingDays(updatedData.getStartDate(), updatedData.getEndDate());
        int leaveYear = updatedData.getStartDate().getYear();

        boolean isOverlapping = leaveApplicationRepository
                .findByUserIdAndIsDeletedFalse(existingLeave.getUserId()).stream()
                .filter(l -> !l.getId().equals(existingLeave.getId()))
                .filter(l -> l.getStartDate().getYear() == leaveYear)
                .anyMatch(l ->
                        !l.getStartDate().isAfter(updatedData.getEndDate()) &&
                                !l.getEndDate().isBefore(updatedData.getStartDate()));

        if (isOverlapping) {
            throw new IllegalArgumentException("Leave dates overlap with an existing leave for the user in " + leaveYear + ".");
        }
        int maxDays = getMaxDaysForLeaveType(updatedData.getLeaveId());
        //  Get all leaves for same user, leave type, and same year (excluding current leave)
        int usedDays = leaveApplicationRepository
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(existingLeave.getUserId(), updatedData.getLeaveId()).stream()
                .filter(l -> !l.getId().equals(existingLeave.getId()))
                .filter(l -> l.getStartDate().getYear() == leaveYear)
                .mapToInt(l -> (int) ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1)
                .sum();
        int totalUsedDays = usedDays + requestedDays;


        int remainingDays = maxDays - totalUsedDays;
        if (remainingDays < requestedDays) {
            throw new IllegalArgumentException("Leave request exceeds remaining balance for " + leaveYear +
                    ". You have only " + remainingDays + " days left.");
        }

        existingLeave.setLeaveTypeId(updatedData.getLeaveId());
        existingLeave.setStartDate(updatedData.getStartDate());
        existingLeave.setEndDate(updatedData.getEndDate());
        existingLeave.setTotalLeaveDays( requestedDays);
        existingLeave.setReportingId(updatedData.getReportingId());
        existingLeave.setDayOffType(DayOffType.valueOf(updatedData.getDayOffType().toUpperCase()));
        existingLeave.setUpdatedAt(LocalDateTime.now());

        String userIdString = String.valueOf(updatedData.getUserId());
        existingLeave.setUpdatedBy(userIdString);
        existingLeave.setRemainingDays(remainingDays);

        int appliedDays = (int) ChronoUnit.DAYS.between(updatedData.getStartDate(), updatedData.getEndDate()) + 1;
        existingLeave.setAppliedDays(appliedDays);




        return leaveApplicationRepository.save(existingLeave);
    }


     int calculateWorkingDays(LocalDate start, LocalDate end) {
        int days = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                days++;
            }
            date = date.plusDays(1);
        }
        return days;
    }



    @Override
    public List<LeaveApplication> getLeavesById(Long Id) {
        // Validate the user exists
        return leaveApplicationRepository.findByUserIdAndIsDeletedFalse(Id);
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

        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(String.valueOf(leave.getUserId()));

        return leaveApplicationRepository.save(leave);
    }

}