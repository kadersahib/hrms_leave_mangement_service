package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.util.LeaveTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;


@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    public List<Map<String, Object>> getAllLeaves() {
        List<LeaveApplication> leaves = leaveApplicationRepository.findByIsDeletedFalse();

        List<LeaveApplication> filtered = leaves.stream()
                .filter(l -> LeaveTypeUtil.isSupported(l.getLeaveTypeId()))
                .toList();

        Map<Long, List<LeaveApplication>> groupedByUser = filtered.stream()
                .collect(Collectors.groupingBy(LeaveApplication::getUserId));

        List<Map<String, Object>> response = new ArrayList<>();

        for (Map.Entry<Long, List<LeaveApplication>> entry : groupedByUser.entrySet()) {
            Map<String, Object> userData = new LinkedHashMap<>();
            Long userId = entry.getKey();
            userData.put("userId", userId);

            Map<Long, List<LeaveApplication>> groupedByType =
                    entry.getValue().stream().collect(Collectors.groupingBy(LeaveApplication::getLeaveTypeId));

            for (Map.Entry<Long, List<LeaveApplication>> typeEntry : groupedByType.entrySet()) {
                Long leaveTypeId = typeEntry.getKey();
                int totalApplied = typeEntry.getValue().stream().mapToInt(LeaveApplication::getAppliedDays).sum();
                int balanceDays = LeaveTypeUtil.getMaxDays(leaveTypeId) - totalApplied;

                userData.put(LeaveTypeUtil.getLeaveTypeName(leaveTypeId),
                        Map.of(
                                "leaveTypeId", leaveTypeId,
                                "totalApplied", totalApplied,
                                "balanceDays", balanceDays
                        ));
            }

            response.add(userData);
        }

        return response;
    }


    @Override
    public List<Map<String, Object>> getLeavesByUserId(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }

        List<LeaveApplication> leaves = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);

        if (leaves.isEmpty()) {
            throw new IllegalArgumentException("No leave data found for user ID " + userId);
        }

        // Filter out maternity/paternity or unsupported types
        List<LeaveApplication> filtered = leaves.stream()
                .filter(l -> LeaveTypeUtil.isSupported(l.getLeaveTypeId()))
                .toList();

        Map<String, Object> userData = new LinkedHashMap<>();
        userData.put("userId", userId);
//        userData.put("userName", user.getName());

        Map<Long, List<LeaveApplication>> groupedByType = filtered.stream()
                .collect(Collectors.groupingBy(LeaveApplication::getLeaveTypeId));

        for (Map.Entry<Long, List<LeaveApplication>> typeEntry : groupedByType.entrySet()) {
            Long leaveTypeId = typeEntry.getKey();
            int totalApplied = typeEntry.getValue().stream().mapToInt(LeaveApplication::getAppliedDays).sum();
            int balanceDays = LeaveTypeUtil.getMaxDays(leaveTypeId) - totalApplied;

            userData.put(LeaveTypeUtil.getLeaveTypeName(leaveTypeId),
                    Map.of(
                            "leaveTypeId", leaveTypeId,
                            "totalApplied", totalApplied,
                            "balanceDays", balanceDays
                    ));
        }

        return List.of(userData);
    }


    @Override
    public Map<String, Object> getLeaveBalanceByUserIdAndLeaveType(Long userId, Long leaveTypeId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }

        List<LeaveApplication> leaves = leaveApplicationRepository
                .findByUserIdAndLeaveTypeIdAndIsDeletedFalse(userId, leaveTypeId);

        if (leaves.isEmpty()) {
            throw new IllegalArgumentException("No leave data found for user ID " + userId + " and leaveTypeId " + leaveTypeId);
        }

        int totalApplied = leaves.stream().mapToInt(LeaveApplication::getAppliedDays).sum();
        int balanceDays = LeaveTypeUtil.getMaxDays(leaveTypeId) - totalApplied;

        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("userId", userId);
//        response.put("leaveTypeId", leaveTypeId);
        response.put("leaveTypeName", LeaveTypeUtil.getLeaveTypeName(leaveTypeId));
        response.put("totalApplied", totalApplied);
        response.put("balanceDays", balanceDays);

        return response;
    }




}
