package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.model.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveTypeClient leaveTypeClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public List<LeaveBalanceDTO> getLeaveBalanceByUser(Long userId) {
        List<LeaveTypeDTO> leaveTypes = leaveTypeClient.getAllLeaveTypes();

        if (userServiceClient.getUserById(userId) == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }


        return leaveTypes.stream()
                .map(type -> calculateLeaveBalance(userId, type))
                .collect(Collectors.toList());
    }

    public LeaveBalanceDTO getLeaveBalanceByUserAndType(Long userId, Long leaveTypeId) {

        LeaveTypeDTO type = leaveTypeClient.getLeaveTypeById(leaveTypeId);
        if (type == null) throw new IllegalArgumentException("Invalid Leave Type");

        boolean hasApplied = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .anyMatch(l -> l.getLeaveTypeId().equals(leaveTypeId));

        if (!hasApplied) {
            throw new IllegalArgumentException("User has not applied for this leave type: " + type.getName());
        }

        return calculateLeaveBalance(userId, type);
    }

    private LeaveBalanceDTO calculateLeaveBalance(Long userId, LeaveTypeDTO type) {
        long usedDays = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .filter(l -> l.getLeaveTypeId().equals(type.getId()))
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .mapToLong(l -> {
                    LocalDate from = l.getApprovedFrom() != null ? l.getApprovedFrom() : l.getStartDate();
                    LocalDate to = l.getApprovedTo() != null ? l.getApprovedTo() : l.getEndDate();
                    return ChronoUnit.DAYS.between(from, to) + 1;
                })
                .sum();

        long remainingDays = type.getMaxDays() - usedDays;

        return new LeaveBalanceDTO(
                type.getId(),
                type.getName(),
                type.getMaxDays(),
                usedDays,
                remainingDays
        );
    }

}
