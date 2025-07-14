package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.model.LeaveApproval;
import net.tetradtech.hrms_leave_service.Enum.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.repository.LeaveApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service

public class LeaveApprovalServiceImpl implements LeaveApprovalService{

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveTypeClient leaveTypeClient;

    @Autowired
    private LeaveApprovalRepository leaveApprovalRepository;

    private static final String SYSTEM_USER = "system";


    @Override
    public LeaveApproval performAction(LeaveApproval dto) {

        LeaveApplication leave = leaveApplicationRepository.findById(dto.getLeaveId())
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + dto.getLeaveId()));

        if (leave.isDeleted()) {
            throw new IllegalStateException("Cannot update approval for deleted leave (Leave ID: " + leave.getId() + ")");
        }
        if (leave.getStatus() == LeaveStatus.CANCELLED || dto.getAction().equalsIgnoreCase("CANCELLED")) {
            throw new IllegalStateException("Approval actions cannot be performed on or set to CANCELLED status.");
        }

        LeaveStatus newStatus = LeaveStatus.valueOf(dto.getAction().toUpperCase());
        if (newStatus == LeaveStatus.APPROVED) {
            // Use approvedFrom/To if given, otherwise fallback to applied dates
            LocalDate approvedFrom = dto.getApprovedFrom() != null ? dto.getApprovedFrom() : leave.getStartDate();
            LocalDate approvedTo = dto.getApprovedTo() != null ? dto.getApprovedTo() : leave.getEndDate();

            // Validate approved range is within applied range
            if (approvedFrom.isBefore(leave.getStartDate()) || approvedTo.isAfter(leave.getEndDate())) {
                throw new IllegalArgumentException("Approved date range must be within the applied leave dates.");
            }

            leave.setApprovedFrom(approvedFrom);
            leave.setApprovedTo(approvedTo);

            long approvedDays = ChronoUnit.DAYS.between(approvedFrom, approvedTo) + 1;
            LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(leave.getLeaveTypeId());
            if (leaveType == null) {
                throw new IllegalArgumentException("Leave type not found for ID: " + leave.getLeaveTypeId());
            }

            int maxAllowed = leaveType.getMaxDays();

            // 5. Set remaining days
            int remaining = maxAllowed - (int) approvedDays;
            leave.setRemainingDays(remaining);
        } else {
            // For REJECTED or other statuses, clear approved range
            leave.setApprovedFrom(null);
            leave.setApprovedTo(null);
            leave.setRemainingDays(null);
        }

        // Save approval
        LeaveApproval history = LeaveApproval.builder()
                .leaveId(dto.getLeaveId())
                .action(dto.getAction())
                .performedBy(dto.getPerformedBy())
                .approvedFrom(leave.getApprovedFrom())
                .approvedTo(leave.getApprovedTo())
                .comment(dto.getComment())
                .createdBy(SYSTEM_USER)
                .isDeleted(false)
                .build();

        leaveApprovalRepository.save(history);

        // Update leave application with approval info
        leave.setStatus(LeaveStatus.valueOf(dto.getAction().toUpperCase()));
        leave.setApprovalComment(dto.getComment());
        leave.setApprovedBy(dto.getPerformedBy());
        leave.setApprovalTimestamp(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(dto.getPerformedBy());

        leaveApplicationRepository.save(leave);

        return history;
    }

    @Override
    public LeaveApproval updateApproval(Long id, LeaveApproval dto) {
        LeaveApproval approval = leaveApprovalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found with ID: " + id));

        Long leaveId = approval.getLeaveId();
        if (leaveId == null) {
            throw new IllegalStateException("Leave ID is missing in the approval record");
        }

        LeaveApplication leave = leaveApplicationRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + leaveId));

        if (leave.isDeleted()) {
            throw new IllegalStateException("Cannot update approval for deleted leave (Leave ID: " + leaveId + ")");
        }

        LeaveStatus newStatus = LeaveStatus.valueOf(dto.getAction().toUpperCase());

        if (newStatus == LeaveStatus.APPROVED) {

            LocalDate approvedFrom = dto.getApprovedFrom() != null ? dto.getApprovedFrom() : leave.getStartDate();
            LocalDate approvedTo = dto.getApprovedTo() != null ? dto.getApprovedTo() : leave.getEndDate();

            if (approvedFrom.isBefore(leave.getStartDate()) || approvedTo.isAfter(leave.getEndDate())) {
                throw new IllegalArgumentException("Approved date range must be within applied leave dates.");
            }

            leave.setApprovedFrom(approvedFrom);
            leave.setApprovedTo(approvedTo);
            approval.setApprovedFrom(approvedFrom);
            approval.setApprovedTo(approvedTo);

            // 3. Calculate approvedDays
            long approvedDays = ChronoUnit.DAYS.between(approvedFrom, approvedTo) + 1;

            // 4. Fetch max allowed days from leave type
            LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(leave.getLeaveTypeId());
            if (leaveType == null) {
                throw new IllegalArgumentException("Leave type not found for ID: " + leave.getLeaveTypeId());
            }

            int maxAllowed = leaveType.getMaxDays();
            int remaining = maxAllowed - (int) approvedDays;

            leave.setRemainingDays(remaining);
        } else {
            // Clear approval details if status is not APPROVED
            leave.setApprovedFrom(null);
            leave.setApprovedTo(null);
            leave.setRemainingDays(null);
            approval.setApprovedFrom(null);
            approval.setApprovedTo(null);
        }

        // Update LeaveApproval fields
        approval.setAction(dto.getAction());
        approval.setComment(dto.getComment());
        approval.setPerformedBy(dto.getPerformedBy());
        approval.setUpdatedAt(LocalDateTime.now());
        approval.setUpdatedBy(SYSTEM_USER);

        // Update LeaveApplication fields
        leave.setStatus(newStatus);
        leave.setApprovalComment(dto.getComment());
        leave.setApprovedBy(dto.getPerformedBy());
        leave.setApprovalTimestamp(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());
        leave.setUpdatedBy(SYSTEM_USER);

        // Save both
        leaveApplicationRepository.save(leave);
        return leaveApprovalRepository.save(approval);
    }

    @Override
    public List<LeaveApproval> getAll() {
        return leaveApprovalRepository.findAll();
    }

    @Override
    public LeaveApproval getById(Long id) {
        return leaveApprovalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found with ID: " + id));
    }
    @Override
    public void deleteById(Long id) {
        LeaveApproval approval = leaveApprovalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found with ID: " + id));

        approval.setDeleted(true);
        approval.setDeletedAt(LocalDateTime.now());
        approval.setDeletedBy(SYSTEM_USER);

        leaveApprovalRepository.save(approval);
    }


}
