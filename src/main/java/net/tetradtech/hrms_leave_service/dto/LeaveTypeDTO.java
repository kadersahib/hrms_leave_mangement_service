package net.tetradtech.hrms_leave_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeaveTypeDTO {
    private Long id;
    private String name;
    private int maxDays;
    private boolean active;

}