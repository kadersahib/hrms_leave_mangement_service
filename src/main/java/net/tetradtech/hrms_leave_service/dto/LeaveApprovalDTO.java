package net.tetradtech.hrms_leave_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalDTO {
    @NotNull(message = "Action is Required ")
    private String action;
    @NotNull(message = "Comments  is Required ")
    private String approverComment;
    @NotNull(message = "approverId  is Required ")
    private Long approverId;
}
