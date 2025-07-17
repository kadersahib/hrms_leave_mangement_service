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

    public AttendanceDTO mapToDTO(AttendanceRecord r) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUserId());
        dto.setDate(r.getDate());
        dto.setClockInTime(r.getClockInTime());
        dto.setClockOutTime(r.getClockOutTime());
        dto.setLate(r.isLate());


        // Safely handle enum to string conversion
        dto.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        dto.setDayOffType(r.getDayOffType() != null ? r.getDayOffType().name() : null);

        // Avoid NullPointerException if user is not found
        UserDTO user = userServiceClient.getUserById(r.getUserId());
        dto.setName(user != null ? user.getName() : "Unknown");

        return dto;
    }
}
