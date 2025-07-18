package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.Enum.DayOffType;
import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveCancelDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
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
import java.util.stream.Collectors;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private LeaveTypeClient leaveTypeClient;

    private static final String SYSTEM_USER = "system";


    @Override
    public LeaveApplication applyLeave(LeaveRequestDTO application) {
        UserDTO user = userServiceClient.getUserById(application.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + application.getUserId());
        }

        LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(application.getLeaveTypeId());
        if (leaveType == null) {
            throw new IllegalArgumentException("Invalid leave type ID: " + application.getLeaveTypeId());
        }

        if (!leaveType.isActive()) {
            throw new IllegalArgumentException("Leave Type is inactive: " + leaveType.getName());
        }

        LocalDate today = LocalDate.now();
        if (!application.getStartDate().isAfter(today)) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (application.getEndDate().isBefore(application.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        long requestedDays = ChronoUnit.DAYS.between(application.getStartDate(), application.getEndDate()) + 1;

        boolean sameDateLeaveExists = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(application.getUserId())
                .stream()
                .anyMatch(existing ->
                        existing.getStartDate().equals(application.getStartDate()) &&
                                existing.getEndDate().equals(application.getEndDate()));
        if (sameDateLeaveExists) {
            throw new IllegalArgumentException("Leave already applied for the same date range.");
        }


        int month = application.getStartDate().getMonthValue();
        int year = application.getStartDate().getYear();


        List<LeaveApplication> leavesThisMonth = leaveApplicationRepository
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(application.getUserId(), application.getLeaveTypeId())
                .stream()
                .filter(l -> (l.getStatus() == LeaveStatus.PENDING || l.getStatus() == LeaveStatus.APPROVED)) // exclude CANCELLED
                .filter(l -> l.getStartDate().getMonthValue() == month && l.getStartDate().getYear() == year)
                .collect(Collectors.toList());


        int alreadyUsedDays = leavesThisMonth.stream().mapToInt(LeaveApplication::getAppliedDays).sum();

        if (alreadyUsedDays + requestedDays > 2) {
            throw new IllegalArgumentException("You can only apply 2 days per month for this leave type.");
        }

        //  Check if any previous record exists (to update)
        Optional<LeaveApplication> existingLeaveOpt = leaveApplicationRepository
                .findTopByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(application.getUserId());

        LeaveApplication leave;
        if (existingLeaveOpt.isPresent()) {
            //  Update the existing leave record
            leave = existingLeaveOpt.get();
            leave.setLeaveTypeId(application.getLeaveTypeId());
            leave.setStartDate(application.getStartDate());
            leave.setEndDate(application.getEndDate());
            leave.setAppliedDays((int) requestedDays);
            leave.setReportingManager(application.getReportingManager());
            leave.setDayOffType(DayOffType.fromString(application.getDayOffType()));
            leave.setStatus(LeaveStatus.PENDING);
            leave.setUpdatedAt(LocalDateTime.now());
            leave.setUpdatedBy("system");
        } else {
            //  No existing leave – create a new one
            leave = new LeaveApplication();
            leave.setUserId(application.getUserId());
            leave.setLeaveTypeId(application.getLeaveTypeId());
            leave.setStartDate(application.getStartDate());
            leave.setEndDate(application.getEndDate());
            leave.setAppliedDays((int) requestedDays);
            leave.setReportingManager(application.getReportingManager());
            leave.setDayOffType(DayOffType.fromString(application.getDayOffType()));
            leave.setCreatedAt(LocalDateTime.now());
            leave.setCreatedBy("system");
            leave.setStatus(LeaveStatus.PENDING);
            leave.setDeleted(false);
        }

        leave.setMaxDays(leaveType.getMaxDays());
        return leaveApplicationRepository.save(leave);
    }


    @Override
    public LeaveApplication updateLeave(Long id, LeaveApplication updatedData) {
        LeaveApplication leave = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave application not found with ID: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leave can be updated.");
        }

        Long userId = updatedData.getUserId() != null ? updatedData.getUserId() : leave.getUserId();

        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(updatedData.getLeaveTypeId());
        if (leaveType == null) {
            throw new IllegalArgumentException("Invalid leave type ID: " + updatedData.getLeaveTypeId());
        }

        if (!leaveType.isActive()) {
            throw new IllegalArgumentException("Leave Type is inactive: " + leaveType.getName());
        }

        LocalDate today = LocalDate.now();

        if (!updatedData.getStartDate().isBefore(today)) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }

        if (updatedData.getEndDate().isBefore(updatedData.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        long requestedDays = ChronoUnit.DAYS.between(updatedData.getStartDate(), updatedData.getEndDate()) + 1;

        boolean isOverlapping = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .filter(existing -> !existing.getId().equals(id)) // Exclude the leave being updated
                .anyMatch(existing ->
                        !existing.getStartDate().isAfter(updatedData.getEndDate()) &&
                                !existing.getEndDate().isBefore(updatedData.getStartDate()));

        if (isOverlapping) {
            throw new IllegalArgumentException("Leave dates overlap with an existing leave for the user.");
        }

        int month = updatedData.getStartDate().getMonthValue();
        int year = updatedData.getStartDate().getYear();

        List<LeaveApplication> leavesThisMonth = leaveApplicationRepository
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(userId, updatedData.getLeaveTypeId())
                .stream()
                .filter(l -> !l.getId().equals(id)) // Exclude the current leave
                .filter(l -> l.getStartDate().getMonthValue() == month && l.getStartDate().getYear() == year)
                .collect(Collectors.toList());

        int alreadyUsedDays = leavesThisMonth.stream().mapToInt(LeaveApplication::getAppliedDays).sum();

        if (alreadyUsedDays + requestedDays > 2) {
            throw new IllegalArgumentException("You can only apply 2 days per month for this leave type. More than that will affect your salary.");
        }

        leave.setUserId(userId);
        leave.setLeaveTypeId(updatedData.getLeaveTypeId());
        leave.setStartDate(updatedData.getStartDate());
        leave.setEndDate(updatedData.getEndDate());
        leave.setReportingManager(updatedData.getReportingManager());
        leave.setAppliedDays((int) requestedDays);
        leave.setMaxDays(leaveType.getMaxDays());
        leave.setDayOffType(updatedData.getDayOffType());
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy("system");

        return leaveApplicationRepository.save(leave);
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
                .findTopByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("No recent leave found for user"));

        latest.setDeleted(true);
        latest.setDeletedAt(LocalDateTime.now());
//        latest.setDeletedBy(user.getName());
        latest.setDeletedBy(SYSTEM_USER);
        leaveApplicationRepository.save(latest);
    }


    @Override
    public LeaveApplication cancelLeave(LeaveCancelDTO dto) {
        LeaveApplication leave = leaveApplicationRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + dto.getId()));
        if (!leave.getUserId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("Leave does not belong to user ID: " + dto.getUserId());
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leaves can be cancelled.");
        }
        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setCreatedAt(LocalDateTime.now());
        leave.setCancelledAt(LocalDateTime.now());
        leave.setCancelledBy(SYSTEM_USER);
        leave.setUpdatedBy(SYSTEM_USER);

        return leaveApplicationRepository.save(leave);

    }
}