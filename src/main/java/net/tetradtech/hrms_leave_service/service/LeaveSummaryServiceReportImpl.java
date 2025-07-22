package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveSummaryReportDTO;
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
    public LeaveSummaryReportDTO getSummaryByUser(Long userId) {
        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        List<LeaveApplication> applications = leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);

        int total = applications.size();
        int approved = (int) applications.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
        int rejected = (int) applications.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count();
        int pending = (int) applications.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        int cancelled = (int) applications.stream().filter(l -> l.getStatus() == LeaveStatus.CANCELLED).count();

        return new LeaveSummaryReportDTO(userId, total, approved, rejected, pending, cancelled);
    }

    @Override
    public List<LeaveSummaryReportDTO> getSummaryForAllUsers() {
        List<UserDTO> users = userServiceClient.getAllUsers();
        return users.stream()
                .map(user -> getSummaryByUser(user.getId()))
                .collect(Collectors.toList());
    }


//    @Override
//    public LeaveSummaryReportDTO getLeaveSummaryTotalOnly() {
//        List<LeaveSummaryReportDTO> userSummaries = leaveApplicationRepository.getLeaveSummaryPerUser();
//
//        int totalApplied = 0;
//        int approvedCount = 0;
//        int rejectedCount = 0;
//        int pendingCount = 0;
//        int cancelledCount = 0;
//
//        for (LeaveSummaryReportDTO dto : userSummaries) {
//            totalApplied += dto.getTotalApplied();
//            approvedCount += dto.getApprovedCount();
//            rejectedCount += dto.getRejectedCount();
//            pendingCount += dto.getPendingCount();
//            cancelledCount += dto.getCancelledCount();
//        }
//
//        return new LeaveSummaryReportDTO(
//                0L, // or null, just to show it's a total
//                totalApplied,
//                approvedCount,
//                rejectedCount,
//                pendingCount,
//                cancelledCount
//        );
//    }

}
