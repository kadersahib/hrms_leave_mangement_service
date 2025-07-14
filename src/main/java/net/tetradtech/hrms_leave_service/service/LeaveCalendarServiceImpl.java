package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;
import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveCalendarDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveCalendarServiceImpl implements LeaveCalendarService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private LeaveTypeClient leaveTypeClient;

    @Override
    public List<LeaveCalendarDTO> getCalendarData() {
        List<LeaveApplication> leaves = leaveApplicationRepository.findByIsDeletedFalse()
                .stream()
                .filter(leave -> leave.getStatus().name().equalsIgnoreCase("APPROVED"))
                .toList();

        return leaves.stream().map(leave -> {
            UserDTO user = userServiceClient.getUserById(leave.getUserId());
            String name = (user != null) ? user.getName() : "Unknown";

            long duration = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            String status = leave.getStatus().name(); // Enum -> String

            return new LeaveCalendarDTO(
                    leave.getUserId(),
                    name,
                    leave.getLeaveTypeId(),
                    leave.getStartDate(),
                    leave.getEndDate(),
                    duration,
                    status
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<LeaveCalendarDTO> getCalendarDataByUser(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        List<LeaveApplication> leaves = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .toList();

        if (leaves.isEmpty()) {
            throw new IllegalArgumentException("Leave data not found for user with ID: " + userId);
        }

        return leaves.stream().map(leave -> {
            long duration = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            return new LeaveCalendarDTO(
                    leave.getUserId(),
                    user.getName(),
                    leave.getLeaveTypeId(),
                    leave.getStartDate(),
                    leave.getEndDate(),
                    duration,
                    leave.getStatus().name()
            );
        }).collect(Collectors.toList());
    }

}
