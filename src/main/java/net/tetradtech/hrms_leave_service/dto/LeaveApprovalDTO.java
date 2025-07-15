package net.tetradtech.hrms_leave_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalDTO {
    private String action; // "APPROVED", "REJECTED"
    private String performedBy;
    private String comment;
}
