package net.tetradtech.hrms_leave_service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LeaveTypeClient {

    private static final String BASE_URL = "http://localhost:8081/api/leave-type";

    @Autowired
    private RestTemplate restTemplate;

    public LeaveTypeDTO getLeaveTypeById(Long leaveTypeId) {
        try {
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                    BASE_URL + "/" + leaveTypeId,
                    ApiResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getData() != null) {
                ObjectMapper mapper = new ObjectMapper();
                LeaveTypeDTO dto = mapper.convertValue(response.getBody().getData(), LeaveTypeDTO.class);
                return dto;
            }

        } catch (Exception ex) {
            System.out.println("Error calling leave-type service: " + ex.getMessage());
        }

        return null;
    }


}
