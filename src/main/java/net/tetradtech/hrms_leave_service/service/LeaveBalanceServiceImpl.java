package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.model.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService{

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveTypeClient leaveTypeClient;

    @Override
    public List<LeaveBalanceDTO> getLeaveBalanceByUser(Long userId) {
        List<LeaveTypeDTO> leaveTypes = leaveTypeClient.getAllLeaveTypes(); // Fetch all leave types

        return leaveTypes.stream().map(type -> {
            long usedDays = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                    .filter(l -> l.getLeaveTypeId().equals(type.getId()))
                    .filter(l -> l.getStatus() == LeaveStatus.APPROVED || l.getStatus() == LeaveStatus.PENDING)
                    .mapToLong(l -> ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1)
                    .sum();

            long remainingDays = type.getMaxDays() - usedDays;

            return new LeaveBalanceDTO(
                    type.getId(),
                    type.getName(),
                    type.getMaxDays(),
                    usedDays,
                    remainingDays
            );
        }).collect(Collectors.toList());
    }
}
