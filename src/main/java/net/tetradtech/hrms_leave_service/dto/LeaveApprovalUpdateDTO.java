package net.tetradtech.hrms_leave_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveApprovalUpdateDTO {

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "PerformedBy is required")
    private String performedBy;

    private String comment;
}
