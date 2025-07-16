package net.tetradtech.hrms_leave_service.service;


import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveHistoryDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.mapper.LeaveHistoryMapper;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveHistoryServiceImpl implements LeaveHistoryService {
    @Autowired
    private  LeaveApplicationRepository leaveApplicationRepository;
    @Autowired
    private  UserServiceClient userServiceClient;
    @Autowired
    private LeaveTypeClient leaveTypeClient;

    @Override
    public List<LeaveHistoryDTO> getLeaveHistoryByUserId(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        List<LeaveApplication> leaves = leaveApplicationRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);

        List<LeaveHistoryDTO> history = new ArrayList<>();

        for (LeaveApplication leave : leaves) {
            LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(leave.getLeaveTypeId());
            String leaveTypeName = null;

            if (leaveType != null) {
                leaveTypeName = leaveType.getName();
            }

            LeaveHistoryDTO dto = LeaveHistoryMapper.toDTO(leave, user.getName(), leaveTypeName);
            history.add(dto);
        }

        return history;
    }

    @Override
    public List<LeaveHistoryDTO> getAllUsersLeaveHistory() {
        List<UserDTO> users = userServiceClient.getAllUsers();
        List<LeaveHistoryDTO> allHistory = new ArrayList<>();

        for (UserDTO user : users) {
            List<LeaveApplication> leaves = leaveApplicationRepository
                    .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId());

            for (LeaveApplication leave : leaves) {
                LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(leave.getLeaveTypeId());
                String leaveTypeName = null;

                if (leaveType != null) {
                    leaveTypeName = leaveType.getName();
                }

                LeaveHistoryDTO dto = LeaveHistoryMapper.toDTO(leave, user.getName(), leaveTypeName);
                allHistory.add(dto);
            }
        }

        return allHistory;
    }



}


