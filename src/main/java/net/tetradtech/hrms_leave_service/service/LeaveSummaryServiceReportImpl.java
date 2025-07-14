package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveSummaryReportDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveSummaryServiceReportImpl implements LeaveSummaryServiceReport {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private LeaveTypeClient leaveTypeClient;

    @Override
    public List<LeaveSummaryReportDTO> getSummaryByUser(Long userId) {

        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        List<LeaveApplication> applications = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);
        List<LeaveTypeDTO> leaveTypes = leaveTypeClient.getAllLeaveTypes();

        return leaveTypes.stream().map(type -> {
            List<LeaveApplication> filtered = applications.stream()
                    .filter(app -> app.getLeaveTypeId().equals(type.getId()))
                    .collect(Collectors.toList());

            int total = filtered.size();
            int approved = (int) filtered.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
            int rejected = (int) filtered.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count();
            int pending = (int) filtered.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
            int cancelled = (int) filtered.stream().filter(l -> l.getStatus() == LeaveStatus.CANCELLED).count();

            return new LeaveSummaryReportDTO(
                    userId,
                    type.getId(),
                    type.getName(),
                    total,
                    approved,
                    rejected,
                    pending,
                    cancelled
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<LeaveSummaryReportDTO> getSummaryForAllUsers() {
        List<UserDTO> allUsers = userServiceClient.getAllUsers();

        return allUsers.stream()
                .flatMap(user -> getSummaryByUser(user.getId()).stream())
                .collect(Collectors.toList());
    }



}
