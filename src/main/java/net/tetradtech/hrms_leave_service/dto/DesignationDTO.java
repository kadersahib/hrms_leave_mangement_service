package net.tetradtech.hrms_leave_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DesignationDTO {
    private Long id;
    private String title;
    private boolean active;
}
