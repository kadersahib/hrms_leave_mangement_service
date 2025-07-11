
package net.tetradtech.hrms_leave_service.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class LeaveTypeClient {

    private static final String BASE_URL = "http://localhost:8081/api/leave-type";

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeaveTypeDTO getLeaveTypeById(Long leaveTypeId) {
        try {
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                    BASE_URL + "/" + leaveTypeId,
                    ApiResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getData() != null) {
                return objectMapper.convertValue(response.getBody().getData(), LeaveTypeDTO.class);
            }

        } catch (Exception ex) {
            System.out.println("Error calling leave-type service: " + ex.getMessage());
        }

        return null;
    }

    // Add this method to fetch all leave types
    public List<LeaveTypeDTO> getAllLeaveTypes() {
        try {
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(BASE_URL, ApiResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getData() != null) {
                return objectMapper.convertValue(response.getBody().getData(), new TypeReference<List<LeaveTypeDTO>>() {});
            }
        } catch (Exception ex) {
            System.out.println("Error fetching all leave types: " + ex.getMessage());
        }

        return List.of(); // Return empty list in case of failure
    }
}
