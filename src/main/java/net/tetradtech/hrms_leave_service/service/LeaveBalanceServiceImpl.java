package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.stream.Collectors;
@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;


    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public List<LeaveBalanceDTO> getAllLeaves() {
        List<LeaveApplication> leaves = leaveApplicationRepository.findByIsDeletedFalse();

        return leaves.stream().map(leave -> {
            UserDTO user = userServiceClient.getUserById(leave.getUserId());

            return new LeaveBalanceDTO(
                    user.getName(),
                    leave.getUserId(),
                    leave.getLeaveTypeName(),
                    leave.getMaxDays(),
                    leave.getRemainingDays(),
                    leave.getTotalAppliedDays(),
                    leave.getTotalCount()
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<LeaveBalanceDTO> getLeavesByUserId(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }
        List<LeaveApplication> leaves = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);

        return leaves.stream().map(leave -> new LeaveBalanceDTO(
                user.getName(),
                leave.getUserId(),
                leave.getLeaveTypeName(),
                leave.getMaxDays(),
                leave.getRemainingDays(),
                leave.getTotalAppliedDays(),
                leave.getTotalCount()
        )).collect(Collectors.toList());
    }


    public List<LeaveBalanceDTO> getLeaveBalanceByUserIdAndLeaveType(Long userId, String leaveTypeName) {

        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User ID not found: " + userId);
        }

        List<LeaveApplication> allLeaves = leaveApplicationRepository.findAllByUserIdAndIsDeletedFalse(userId);

        List<LeaveApplication> filtered = allLeaves.stream()
                .filter(leave -> leave.getLeaveTypeName() != null &&
                        leave.getLeaveTypeName().equalsIgnoreCase(leaveTypeName))
                .toList();

        if (filtered.isEmpty()) {
            throw new IllegalArgumentException("No leaves found for leave type: " + leaveTypeName);
        }

        return filtered.stream()
                .map(leave -> convertToLeaveBalanceDTO(leave, user))
                .collect(Collectors.toList());
    }

    private LeaveBalanceDTO convertToLeaveBalanceDTO(LeaveApplication leave, UserDTO user) {
        LeaveBalanceDTO dto = new LeaveBalanceDTO();
        dto.setUserId(leave.getUserId());
        dto.setName(user.getName());
        dto.setLeaveTypeName(leave.getLeaveTypeName());
        dto.setMaxDays(leave.getMaxDays());
        dto.setRemainingDays(leave.getRemainingDays());
        dto.setTotalAppliedDays(leave.getTotalAppliedDays());
        dto.setTotalCount(leave.getTotalCount());
        return dto;
    }

}
