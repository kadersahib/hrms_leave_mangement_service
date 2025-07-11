package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveApprovalUpdateDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.model.LeaveApproval;
import net.tetradtech.hrms_leave_service.model.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import net.tetradtech.hrms_leave_service.repository.LeaveApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service

public class LeaveApprovalServiceImpl implements LeaveApprovalService{

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

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
        // Save approval
        LeaveApproval history = LeaveApproval.builder()
                .leaveId(dto.getLeaveId())
                .action(dto.getAction())
                .performedBy(dto.getPerformedBy())
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
    public LeaveApproval updateApproval(Long id, LeaveApprovalUpdateDTO dto ) {
        LeaveApproval approval = leaveApprovalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found with ID: " + id));


        Long leaveId = approval.getLeaveId();
        if (leaveId == null) {
            throw new IllegalStateException("Leave ID is missing in the approval record");
        }

        LeaveApplication leave = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found with ID: " + leaveId));

        if (leave.isDeleted()) {
            throw new IllegalStateException("Cannot update approval for deleted leave (Leave ID: " + leave.getId() + ")");
        }


        // Update LeaveApproval
        approval.setAction(dto.getAction());
        approval.setComment(dto.getComment());
        approval.setPerformedBy(dto.getPerformedBy());
        approval.setUpdatedAt(LocalDateTime.now());
        approval.setUpdatedBy(SYSTEM_USER);

        // Update LeaveApplication status + audit fields
        leave.setStatus(LeaveStatus.valueOf(dto.getAction().toUpperCase()));
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
