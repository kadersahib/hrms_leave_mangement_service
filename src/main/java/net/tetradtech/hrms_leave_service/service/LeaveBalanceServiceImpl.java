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

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService{

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveTypeClient leaveTypeClient;

    @Override
    public List<LeaveBalanceDTO> getBalanceForUser(Long userId) {
        List<LeaveApplication> approvedLeaves =
                leaveApplicationRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, LeaveStatus.APPROVED);

        Map<Long, Integer> usedMap = new HashMap<>();

        for (LeaveApplication leave : approvedLeaves) {
            long days = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            usedMap.merge(leave.getLeaveTypeId(), (int) days, Integer::sum);
        }

        List<LeaveBalanceDTO> balances = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : usedMap.entrySet()) {
            Long leaveTypeId = entry.getKey();
            int used = entry.getValue();

            LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(leaveTypeId);
            int total = (leaveType != null) ? leaveType.getMaxDays() : 0;
            int remaining = Math.max(0, total - used);

            balances.add(new LeaveBalanceDTO(userId, leaveTypeId, total, used, remaining));
        }

        return balances;
    }
}
