
package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveCancelDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.model.LeaveStatus;
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
    private LeaveTypeClient leaveTypeClient;


    private static final String SYSTEM_USER = "system";

    @Override
    public LeaveApplication applyLeave(LeaveApplication application) {

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

        long requestedDays = ChronoUnit.DAYS.between(application.getStartDate(), application.getEndDate()) + 1;
        if (requestedDays > leaveType.getMaxDays()) {
            throw new IllegalArgumentException("Requested days (" + requestedDays + ") exceed max allowed (" + leaveType.getMaxDays() + ") for leave type: " + leaveType.getName());
        }


        boolean exists = leaveApplicationRepository.findAll().stream()
                .filter(l -> !l.isDeleted())
                .anyMatch(existing ->
                        existing.getUserId().equals(application.getUserId()) &&
                                existing.getStartDate().equals(application.getStartDate()) &&
                                existing.getEndDate().equals(application.getEndDate()));

        if (exists) {
            throw new IllegalArgumentException("Leave already applied for this date range.");
        }

        int currentYear = application.getStartDate().getYear();
        LocalDate yearStart = LocalDate.of(currentYear, 1, 1);
        LocalDate yearEnd = LocalDate.of(currentYear, 12, 31);

        long usedDays = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(application.getUserId()).stream()
                .filter(l -> l.getLeaveTypeId().equals(application.getLeaveTypeId()))
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED || l.getStatus() == LeaveStatus.PENDING)
                .filter(l -> !l.getStartDate().isBefore(yearStart) && !l.getEndDate().isAfter(yearEnd)) // Only current year
                .mapToLong(l -> ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1)
                .sum();

        long totalAfterRequest = usedDays + requestedDays;

        if (totalAfterRequest > leaveType.getMaxDays()) {
            throw new IllegalArgumentException("Leave limit exceeded for leave type: " + leaveType.getName()
                    + ". Allowed: " + leaveType.getMaxDays()
                    + ", Already used/requested: " + usedDays
                    + ", Trying to apply: " + requestedDays);
        }

        application.setAppliedDays((int) requestedDays);
        application.setMaxDays(leaveType.getMaxDays());
        application.setCreatedAt(LocalDateTime.now());
        application.setCreatedBy(SYSTEM_USER);
        application.setStatus(LeaveStatus.PENDING);
        application.setDeleted(false);
        return leaveApplicationRepository.save(application);
    }

    @Override
    public LeaveApplication updateLeave(Long id, LeaveApplication updatedData) {
        LeaveApplication leave = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave application not found with ID: " + id));


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
        long requestedDays = ChronoUnit.DAYS.between(updatedData.getStartDate(), updatedData.getEndDate()) + 1;
        if (requestedDays > leaveType.getMaxDays()) {
            throw new IllegalArgumentException("Requested days (" + requestedDays + ") exceed max allowed ("
                    + leaveType.getMaxDays() + ") for leave type: " + leaveType.getName());
        }

        boolean overlap = leaveApplicationRepository.findAll().stream()
                .filter(l -> !l.isDeleted() && !l.getId().equals(id))
                .anyMatch(l ->
                        l.getUserId().equals(userId) &&

                                !l.getStartDate().isAfter(updatedData.getEndDate()) &&
                                !l.getEndDate().isBefore(updatedData.getStartDate()));

        if (overlap) {
            throw new IllegalArgumentException("Updated leave dates overlap with an existing leave.");
        }

        int currentYear = updatedData.getStartDate().getYear();
        LocalDate yearStart = LocalDate.of(currentYear, 1, 1);
        LocalDate yearEnd = LocalDate.of(currentYear, 12, 31);

        //  Check if updating the leave will exceed max allowed days
        long usedDays = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .filter(l -> l.getLeaveTypeId().equals(updatedData.getLeaveTypeId()))
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED || l.getStatus() == LeaveStatus.PENDING)
                .filter(l -> !l.getId().equals(id)) // exclude current leave
                .filter(l -> !l.getStartDate().isBefore(yearStart) && !l.getEndDate().isAfter(yearEnd))
                .mapToLong(l -> ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1)
                .sum();

        long totalAfterUpdate = usedDays + requestedDays;

        if (totalAfterUpdate > leaveType.getMaxDays()) {
            long remaining = leaveType.getMaxDays() - usedDays;
            throw new IllegalArgumentException("Leave limit exceeded for leave type: " + leaveType.getName()
                    + ". Allowed: " + leaveType.getMaxDays()
                    + ", Already used/requested: " + usedDays
                    + ", Trying to update: " + requestedDays
                    + ", Remaining: " + remaining + " day(s).");
        }

        leave.setUserId(userId);
        leave.setLeaveTypeId(updatedData.getLeaveTypeId());
        leave.setReason(updatedData.getReason());
        leave.setStartDate(updatedData.getStartDate());
        leave.setEndDate(updatedData.getEndDate());
        leave.setStatus(updatedData.getStatus());
        leave.setReportingManager(updatedData.getReportingManager());
        leave.setMaxDays(updatedData.getMaxDays());
        leave.setAppliedDays((int) requestedDays);
        leave.setMaxDays(leaveType.getMaxDays());
//        leave.setUpdatedBy(user.getName());
        leave.setUpdatedBy(SYSTEM_USER);
        leave.setUpdatedAt(LocalDateTime.now());

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
        LeaveApplication leave = leaveApplicationRepository.findById(dto.getLeaveId())
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + dto.getLeaveId()));
        if (!leave.getUserId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("Leave does not belong to user ID: " + dto.getUserId());
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING leaves can be cancelled.");
        }
        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setCancelReason(dto.getCancelReason());
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setCreatedAt(LocalDateTime.now());
        leave.setCancelledAt(LocalDateTime.now());
        leave.setCancelledBy("system");
        leave.setUpdatedBy("system"); 

        return leaveApplicationRepository.save(leave);

    }
}