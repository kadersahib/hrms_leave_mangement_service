package net.tetradtech.hrms_leave_service.mapper;

import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    @Autowired
    private UserServiceClient userServiceClient;

    public AttendanceDTO mapToDTO(AttendanceRecord record) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setDate(record.getDate());
        dto.setClockInTime(record.getClockInTime());
        dto.setClockOutTime(record.getClockOutTime());
        dto.setLate(record.isLate());

        dto.setStatus(record.getStatus() != null ? record.getStatus().name() : null);
        // Avoid NullPointerException if user is not found
        UserDTO user = userServiceClient.getUserById(record.getUserId());
        dto.setName(user != null ? user.getName() : "Unknown");
        dto.setDesignation(record.getDesignation());

        return dto;
    }
}
